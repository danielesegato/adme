package com.danielesegato.adme.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Ease up the implementation of Content Providers by letting the provider split the different
 * implementations for different contents in different {@link ADMEContentProviderComponent}.
 * <p/>
 * This class is openly inspired by the great implementation of the despicable / component content
 * provider implementation from Sergi Martínez: https://gist.github.com/sergiandreplace/8165986
 * <p/>
 * But it also add a lot of functionality commonly needed by content providers, like:
 * <ul>
 * <li>Properly handling transactions</li>
 * <li>Batch operations support</li>
 * <li>related URI notification handling (very useful when you have a view or a join uri)</li>
 * </ul>
 * <p/>
 * TODO features:
 * <ul>
 * <li>Support for syncToNetwork notifications</li>
 * <li>Support for non-DB operations (File stream etc..)</li>
 * </ul>
 */
public abstract class ADMEContentProvider extends ContentProvider {
    public static final String URI_SCHEME = "content";
    /**
     * Maximum number of operations allowed in a batch between yield points.
     */
    private static final int YIELD_MAX_OPERATIONS_PER_YIELD_POINT = 500;
    private static final long YIELD_SLEEP_AFTER_YIELD_DELAY = 300L;
    private final ThreadLocal<Boolean> tNotifyChange = new ThreadLocal<Boolean>();
    private final ThreadLocal<Set<Uri>> tNotifyUris = new ThreadLocal<Set<Uri>>();
    private final ThreadLocal<Boolean> tApplyingBatch = new ThreadLocal<Boolean>();
    private static final SQLiteTransactionListener DUMMY_TRANSACTION_LISTENER = new SQLiteTransactionListener() {
        @Override
        public void onBegin() {
        }

        @Override
        public void onCommit() {
        }

        @Override
        public void onRollback() {
        }
    };

    public static final Uri buildUri(String scheme, String authority, String path) {
        return new Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path)
                .build();
    }

    public static final Uri buildUri(String authority, String path) {
        return buildUri(URI_SCHEME, authority, path);
    }

    SparseArray<ADMEContentProviderComponent> components;
    int componentId = 0;
    private UriMatcher uriMatcher;
    private boolean componentsRegistered = false;
    private SQLiteTransactionListener transactionListener = DUMMY_TRANSACTION_LISTENER;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        components = new SparseArray<ADMEContentProviderComponent>();
        return false;
    }

    /**
     * Invoked, lazily, once the first time the ContentProvider is used, it has the responsibility of
     * registering all the components of this content provider.
     *
     * @see #registerComponent(ADMEContentProviderComponent)
     */
    public abstract void registerComponents();

    /**
     * @return the Authority string, must match the one registered in the android manifest
     */
    public abstract String getAuthority();


    /**
     * Register a component to the list of components that will answer to data requests
     *
     * @param component the component to register
     * @return the identifier of the component
     */
    protected int registerComponent(ADMEContentProviderComponent component) {
        componentId++;
        component.setContext(getContext());
        component.setAuthority(getAuthority());
        final Uri componentUri = component.getUri();
        uriMatcher.addURI(componentUri.getAuthority(), component.getBaseUriPath(), componentId);
        components.put(componentId, component);
        return componentId;
    }

    /**
     * Retrieves the component that answers to the provided uri.
     *
     * @param uri the uri you want to match to a component
     * @return The component who requested the type of uri provided
     * @throws java.lang.IllegalArgumentException if the uri doesn't match any registered component
     */
    private ADMEContentProviderComponent getComponent(Uri uri) throws IllegalArgumentException {
        if (!componentsRegistered) {
            registerComponents();
            componentsRegistered = true;
        }
        int uriType = uriMatcher.match(uri);
        if (uriType == -1) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return components.get(uriType);
    }

    /**
     * Getter for the current database
     *
     * @return returns the database used by the content provider
     */
    public abstract SQLiteDatabase getDb();

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ADMEContentProviderComponent component = getComponent(uri);
        Cursor cursor = component.query(getDb(), uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null && component.isAutoSetNotificationUriOnQuery()) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return getComponent(uri).getType();
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        ADMEContentProviderComponent component = getComponent(uri);
        SQLiteDatabase db = getDb();
        Uri result = null;
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            db.beginTransactionWithListener(transactionListener);
            try {
                result = component.insert(db, uri, contentValues);
                if (result != null) {
                    component.updateNotificationUris(getNotificationUris(), uri);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            notifyChangeNow();
        } else {
            result = component.insert(db, uri, contentValues);
            if (result != null) {
                component.updateNotificationUris(getNotificationUris(), uri);
                tNotifyChange.set(true);
            }
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String where, String[] selectionArgs) {
        ADMEContentProviderComponent component = getComponent(uri);
        SQLiteDatabase db = getDb();
        int count = 0;
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            db.beginTransactionWithListener(transactionListener);
            try {
                count = component.delete(db, uri, where, selectionArgs);
                if (count > 0) {
                    component.updateNotificationUris(getNotificationUris(), uri);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            notifyChangeNow();
        } else {
            count = component.delete(db, uri, where, selectionArgs);
            if (count > 0) {
                component.updateNotificationUris(getNotificationUris(), uri);
                tNotifyChange.set(true);
            }
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
        ADMEContentProviderComponent component = getComponent(uri);
        SQLiteDatabase db = getDb();
        int count = 0;
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            db.beginTransactionWithListener(transactionListener);
            try {
                count = component.update(db, uri, values, where, selectionArgs);
                if (count > 0) {
                    component.updateNotificationUris(getNotificationUris(), uri);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            notifyChangeNow();
        } else {
            count = component.update(db, uri, values, where, selectionArgs);
            if (count > 0) {
                component.updateNotificationUris(getNotificationUris(), uri);
            }
        }
        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesTable) {
        ADMEContentProviderComponent component = getComponent(uri);
        SQLiteDatabase db = getDb();
        int numValues = valuesTable.length;
        db.beginTransactionWithListener(transactionListener);
        int ypCount = 0;
        boolean success = false;
        try {
            for (int i = 0; i < numValues; i++) {
                Uri result = component.insert(db, uri, valuesTable[i]);
                if (result != null) {
                    component.updateNotificationUris(getNotificationUris(), uri);
                    tNotifyChange.set(true);
                }
                if (db.yieldIfContendedSafely()) {
                    ypCount++;
                }
            }
            db.setTransactionSuccessful();
            success = true;
        } finally {
            db.endTransaction();
            if (success || ypCount > 0) {
                notifyChangeNowIfNeeded();
            }
        }
        return numValues;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        int ypCount = 0;
        int opCount = 0;
        SQLiteDatabase db = getDb();
        db.beginTransactionWithListener(transactionListener);
        boolean success = false;
        try {
            tApplyingBatch.set(true);
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (++opCount > getMaxOperationsPerYield()) {
                    throw new OperationApplicationException(
                            String.format("Too many content provider operations between yield points. The maximum number of operations per yield point is %d",
                                    getMaxOperationsPerYield())
                            , ypCount
                    );
                }
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    opCount = 0;
                    if (db.yieldIfContendedSafely(YIELD_SLEEP_AFTER_YIELD_DELAY)) {
                        ypCount++;
                    }
                }
                results[i] = operation.apply(this, results, i);
            }
            db.setTransactionSuccessful();
            success = true;
            return results;
        } finally {
            tApplyingBatch.set(false);
            db.endTransaction();
            if (success || ypCount > 0) {
                notifyChangeNowIfNeeded();
            }
        }
    }

    protected void setTransactionListener(SQLiteTransactionListener transactionListener) {
        this.transactionListener = transactionListener != null ? transactionListener : DUMMY_TRANSACTION_LISTENER;
    }

    protected void notifyChangeNowIfNeeded() {
        if (tNotifyChange.get() != null && tNotifyChange.get()) {
            tNotifyChange.set(false);
            notifyChangeNow();
        }
    }

    private void notifyChangeNow() {
        for (Uri uri : getNotificationUris()) {
            // TODO syncToNetwork should be decided by the component when inserting / updating / deleting
            // The idea is that you change the DB locally then set syncToNetwork = true to wake up the
            // SyncAdapter which will use the content provider again to actually update the remote content
            // when the content is modified locally and should be synced remotely we should pass true here
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        getNotificationUris().clear();
    }

    /**
     * @return the current thread notification uris map
     */
    protected Set<Uri> getNotificationUris() {
        Set<Uri> notificationUris = tNotifyUris.get();
        if (notificationUris == null) {
            notificationUris = new HashSet<Uri>();
            tNotifyUris.set(notificationUris);
        }
        return notificationUris;
    }

    private boolean applyingBatch() {
        return tApplyingBatch.get() != null && tApplyingBatch.get();
    }

    /**
     * @return Number of operations that can be applied at once without a yield point.
     */
    protected int getMaxOperationsPerYield() {
        return YIELD_MAX_OPERATIONS_PER_YIELD_POINT;
    }
}
