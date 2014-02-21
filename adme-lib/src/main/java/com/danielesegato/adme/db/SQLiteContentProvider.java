package com.danielesegato.adme.db;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;

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
    protected abstract Uri insertInTransaction(SQLiteDatabase db, Uri uri, ContentValues values);

    /**
     * The equivalent of the {@link #update} method, but invoked within a transaction.
     */
    protected abstract int updateInTransaction(SQLiteDatabase db, Uri uri, ContentValues values, String selection,
                                               String[] selectionArgs);

    /**
     * The equivalent of the {@link #delete} method, but invoked within a transaction.
     */
    protected abstract int deleteInTransaction(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs);

    /**
     * The equivalent of the {@link #query} method, invoked with the already initialized database.
     */
    protected abstract Cursor query(SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

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
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (!applyingBatch) {
            db.beginTransactionWithListener(this);
            try {
                result = insertInTransaction(db, uri, values);
                if (result != null) {
                    mNotifyChange.set(true);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            onEndTransaction();
        } else {
            result = insertInTransaction(db, uri, values);
            if (result != null) {
                mNotifyChange.set(true);
            }
        }
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = values.length;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransactionWithListener(this);
        try {
            for (int i = 0; i < numValues; i++) {
                Uri result = insertInTransaction(db, uri, values[i]);
                if (result != null) {
                    mNotifyChange.set(true);
                }
                SQLiteDatabase savedDb = db;
                db.yieldIfContendedSafely();
                db = savedDb;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        onEndTransaction();
        return numValues;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        boolean applyingBatch = applyingBatch();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (!applyingBatch) {
            db.beginTransactionWithListener(this);
            try {
                count = updateInTransaction(db, uri, values, selection, selectionArgs);
                if (count > 0) {
                    mNotifyChange.set(true);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            onEndTransaction();
        } else {
            count = updateInTransaction(db, uri, values, selection, selectionArgs);
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
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (!applyingBatch) {
            db.beginTransactionWithListener(this);
            try {
                count = deleteInTransaction(db, uri, selection, selectionArgs);
                if (count > 0) {
                    mNotifyChange.set(true);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            onEndTransaction();
        } else {
            count = deleteInTransaction(db, uri, selection, selectionArgs);
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
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransactionWithListener(this);
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
                    if (db.yieldIfContendedSafely(SLEEP_AFTER_YIELD_DELAY)) {
                        db = mOpenHelper.getWritableDatabase();
                        ypCount++;
                    }
                }

                results[i] = operation.apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            mApplyingBatch.set(false);
            db.endTransaction();
            onEndTransaction();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return query(db, uri, projection, selection, selectionArgs, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return query(db, uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    /**
     * The equivalent of the {@link #query} method (with {@link CancellationSignal} variant), invoked with the already initialized database.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected Cursor query(SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        return query(db, uri, projection, selection, selectionArgs, sortOrder);
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
