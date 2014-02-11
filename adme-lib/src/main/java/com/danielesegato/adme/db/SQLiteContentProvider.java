package com.danielesegato.adme.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * General purpose {@link android.content.ContentProvider} base class that uses SQLiteDatabase for storage.
 */
public abstract class SQLiteContentProvider extends ContentProvider
        implements SQLiteTransactionListener {

    private static final String TAG = "SQLiteContentProvider";
    private static final long SLEEP_AFTER_YIELD_DELAY = 300L;
    /**
     * Maximum number of operations allowed in a batch between yield points.
     */
    private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;
    private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<Boolean>();
    private final ThreadLocal<Boolean> mNotifyChange = new ThreadLocal<Boolean>();
    private final ThreadLocal<Set<Uri>> mNotifyUris = new ThreadLocal<Set<Uri>>();
    protected SQLiteDatabase mDb;
    private SQLiteOpenHelper mOpenHelper;

    /**
     * @return Number of operations that can be applied at once without a yield point.
     */
    public int getMaxOperationsPerYield() {
        return MAX_OPERATIONS_PER_YIELD_POINT;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = getDatabaseHelper(context);
        return true;
    }

    protected abstract SQLiteOpenHelper getDatabaseHelper(Context context);

    /**
     * The equivalent of the {@link #insert} method, but invoked within a transaction.
     */
    protected abstract Uri insertInTransaction(Uri uri, ContentValues values);

    /**
     * The equivalent of the {@link #update} method, but invoked within a transaction.
     */
    protected abstract int updateInTransaction(Uri uri, ContentValues values, String selection,
                                               String[] selectionArgs);

    /**
     * The equivalent of the {@link #delete} method, but invoked within a transaction.
     */
    protected abstract int deleteInTransaction(Uri uri, String selection, String[] selectionArgs);

    protected abstract void notifyChange(Set<Uri> notificationUris);

    public SQLiteOpenHelper getDatabaseHelper() {
        return mOpenHelper;
    }

    private boolean applyingBatch() {
        return mApplyingBatch.get() != null && mApplyingBatch.get();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransactionWithListener(this);
            try {
                result = insertInTransaction(uri, values);
                if (result != null) {
                    mNotifyChange.set(true);
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }

            onEndTransaction();
        } else {
            result = insertInTransaction(uri, values);
            if (result != null) {
                mNotifyChange.set(true);
            }
        }
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = values.length;
        mDb = mOpenHelper.getWritableDatabase();
        mDb.beginTransactionWithListener(this);
        try {
            for (int i = 0; i < numValues; i++) {
                Uri result = insertInTransaction(uri, values[i]);
                if (result != null) {
                    mNotifyChange.set(true);
                }
                SQLiteDatabase savedDb = mDb;
                mDb.yieldIfContendedSafely();
                mDb = savedDb;
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }

        onEndTransaction();
        return numValues;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransactionWithListener(this);
            try {
                count = updateInTransaction(uri, values, selection, selectionArgs);
                if (count > 0) {
                    mNotifyChange.set(true);
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }

            onEndTransaction();
        } else {
            count = updateInTransaction(uri, values, selection, selectionArgs);
            if (count > 0) {
                mNotifyChange.set(true);
            }
        }

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransactionWithListener(this);
            try {
                count = deleteInTransaction(uri, selection, selectionArgs);
                if (count > 0) {
                    mNotifyChange.set(true);
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }

            onEndTransaction();
        } else {
            count = deleteInTransaction(uri, selection, selectionArgs);
            if (count > 0) {
                mNotifyChange.set(true);
            }
        }
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        int ypCount = 0;
        int opCount = 0;
        mDb = mOpenHelper.getWritableDatabase();
        mDb.beginTransactionWithListener(this);
        try {
            mApplyingBatch.set(true);
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (++opCount > getMaxOperationsPerYield()) {
                    throw new OperationApplicationException(
                            "Too many content provider operations between yield points. "
                                    + "The maximum number of operations per yield point is "
                                    + MAX_OPERATIONS_PER_YIELD_POINT, ypCount);
                }
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    opCount = 0;
                    if (mDb.yieldIfContendedSafely(SLEEP_AFTER_YIELD_DELAY)) {
                        mDb = mOpenHelper.getWritableDatabase();
                        ypCount++;
                    }
                }

                results[i] = operation.apply(this, results, i);
            }
            mDb.setTransactionSuccessful();
            return results;
        } finally {
            mApplyingBatch.set(false);
            mDb.endTransaction();
            onEndTransaction();
        }
    }

    /**
     * Add an Uri to notify later when the transaction end
     *
     * @param uri the Uri to notify
     * @return <em>true</em> if the user wasn't already in the notification uri set, <em>false</em> otherwise
     */
    protected boolean addNotificationUri(Uri uri) {
        Set<Uri> notificationUris = getNotificationUris();
        return notificationUris.add(uri);
    }

    /**
     * @param uri the Uri to check
     * @return <em>true</em> if the uri is already contained in the set of uris to be notified, <em>false</em> otherwise
     */
    protected boolean containsNotificationUri(Uri uri) {
        return getNotificationUris().contains(uri);
    }

    /**
     * @return the current thread notification uris set
     */
    protected Set<Uri> getNotificationUris() {
        Set<Uri> notificationUris = mNotifyUris.get();
        if (notificationUris == null) {
            notificationUris = new HashSet<Uri>();
            mNotifyUris.set(notificationUris);
        }
        return notificationUris;
    }

    @Override
    public void onBegin() {
        onBeginTransaction();
    }

    @Override
    public void onCommit() {
        beforeTransactionCommit();
    }

    @Override
    public void onRollback() {
        // not used
    }

    protected void onBeginTransaction() {
    }

    protected void beforeTransactionCommit() {
    }

    protected void onEndTransaction() {
        notifyChangeNowIfNeeded();
    }

    protected void notifyChangeNowIfNeeded() {
        if (mNotifyChange.get() != null && mNotifyChange.get()) {
            mNotifyChange.set(false);
            notifyChange(getNotificationUris());
            getNotificationUris().clear();
        }
    }
}
