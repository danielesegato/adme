package com.danielesegato.adme.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A component for the {@link com.danielesegato.adme.provider.ADMEContentProvider}.
 */
public abstract class ADMEContentProviderComponent {

    protected static final String URI_SCHEME = ADMEContentProvider.URI_SCHEME;
    protected static final String CONTENT_TYPE_DIR_PREFIX = "vnd.android.cursor.dir/vnd.";
    protected static final String CONTENT_TYPE_ITEM_PREFIX = "vnd.android.cursor.item/vnd.";

    private Context context;
    private String authority;
    private Uri uri;

    public static final Uri buildUri(String scheme, String authority, String path) {
        return ADMEContentProvider.buildUri(scheme, authority, path);
    }

    public static final Uri buildUri(String authority, String path) {
        return ADMEContentProvider.buildUri(authority, path);
    }

    /**
     * Returns the impl path of this entity. For example "books". Will be added to the authority of the {@link com.danielesegato.adme.provider.ADMEContentProvider}
     * in the shape content://{authority}/{getBaseUriPath()}
     *
     * @return the base Uri path for the content
     */
    public abstract String getBaseUriPath();

    /**
     * Subclasses can override this method to build their custom {@link android.net.Uri}
     *
     * @return the custom Uri
     */
    protected Uri buildUri() {
        return buildUri(URI_SCHEME, getAuthority(), getBaseUriPath());
    }

    Uri getUri() {
        if (uri == null) {
            Uri uri = buildUri();
            onUriReady(uri);
            this.uri = uri;
        }
        return uri;
    }

    /**
     * Performs a query on the entity managed by this component
     *
     * @param db            the current db
     * @param uri           The URI to query. This will be the full URI sent by the client; if the client is requesting a specific record, the URI will end in a record number that the implementation should parse and add to a WHERE or HAVING clause, specifying that _id value.
     * @param projection    The list of columns to put into the cursor. If null all columns are included.
     * @param selection     A selection criteria to apply when filtering rows. If null then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     * @return a Cursor or null.
     * @paramsortOrder How the rows in the cursor should be sorted. If null then the provider is free to define the sort order.
     */
    public abstract Cursor query(SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    /**
     * Inserts value in the entity managed by this component.
     * <p/>
     * Suggestion, use {@link android.content.ContentUris#withAppendedId(android.net.Uri, long)} to build the insert Uri from the ID.
     *
     * @param db            the current db
     * @param uri           The content:// URI of the insertion request.
     * @param contentValues A set of column_name/value pairs to add to the database.
     * @return The URI for the newly inserted item.
     */
    public abstract Uri insert(SQLiteDatabase db, Uri uri, ContentValues contentValues);

    /**
     * Deletes one or more rows for the  entity managed by this component
     *
     * @param db
     * @param uri           The full URI to query, including a row ID (if a specific record is requested).
     * @param where         An optional restriction to apply to rows when deleting.
     * @param selectionArgs the values for the arguments
     * @return The number of rows affected.
     */
    public abstract int delete(SQLiteDatabase db, Uri uri, String where, String[] selectionArgs);

    /**
     * Updates one or more rows
     *
     * @param uri    The URI to query. This can potentially have a record ID if this is an update request for a specific record.
     * @param values A Bundle mapping from column names to new column values (NULL is a
     *               valid value).
     * @param where  An optional filter to match rows to update.
     * @return
     */
    public abstract int update(SQLiteDatabase db, Uri uri, ContentValues values, String where, String[] selectionArgs);

    /**
     * @return the data type handled by this component, override to customize
     * @See #CONTENT_TYPE_ITEM_PREFIX
     * @see #CONTENT_TYPE_DIR_PREFIX
     */
    public String getType() {
        return String.format("%s%s.%s", CONTENT_TYPE_DIR_PREFIX, getAuthority(), getBaseUriPath());
    }

    /**
     * Update the list of Uris to be notified of a change when the transaction is over.
     *
     * @param currentNotificationUris set of {@link android.net.Uri}s that are already to be notified in this transaction.
     * @param modifiedUri             the {@link android.net.Uri} the Uri which has been modified
     */
    protected void updateNotificationUris(Set<Uri> currentNotificationUris, Uri modifiedUri) {
        if (!currentNotificationUris.contains(modifiedUri)) {
            currentNotificationUris.add(modifiedUri);
            currentNotificationUris.addAll(getRelatedUris(modifiedUri));
        }
    }

    /**
     * Provide a collection of related {@link android.net.Uri}s related to the uri being modified.
     * This is the set of Uri that are to be considered modified when the given modifiedUri is changed (insert/update/delete)
     * <p/>
     * This method is called, by default from the {@link #updateNotificationUris(java.util.Set, android.net.Uri)} method.
     * <p/>
     * Example to override:
     * <pre>
     *     Set&lt;Uri&gt; relatedUris = new HashSet&lt;Uri&gt;(super.getRelatedUris(modifiedUri));
     *     relatedUris.add(MY_RELATED_URI);
     *     return relatedUris;
     * </pre>
     *
     * @param modifiedUri the {@link android.net.Uri} which has been modified
     * @return the collection of Uris that have been modified.
     */
    protected Collection<? extends Uri> getRelatedUris(Uri modifiedUri) {
        return Collections.emptySet();
    }

    /**
     * Called when the Uri for this component is ready to be set in some static field.
     *
     * @param uri the Uri
     */
    protected void onUriReady(Uri uri) {
    }

    void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    protected boolean isAutoSetNotificationUriOnQuery() {
        return true;
    }
}
