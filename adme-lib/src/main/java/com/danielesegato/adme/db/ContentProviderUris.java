package com.danielesegato.adme.db;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

/**
 * Helper class to define {@link android.net.Uri}s for a {@link android.content.ContentProvider} backed by a database
 */
public class ContentProviderUris {
    public static final String URI_FORMAT = "content://%s/%s";
    public static final String MIMETYPE_DIR_FORMAT = "vnd.android.cursor.dir/vnd.%s.%s";

    /**
     * Build an {@link android.net.Uri} and return it
     *
     * @param authority  the {@link android.content.ContentProvider} authority
     * @param entityName the entity (table) name
     * @return the Uri
     */
    public static final Uri buildUri(final String authority, final String entityName) {
        return Uri.parse(String.format(URI_FORMAT, authority, entityName));
    }

    /**
     * Build an {@link android.net.Uri}, add it to an {@link android.content.UriMatcher} and return the Uri.
     *
     * @param authority  the {@link android.content.ContentProvider} authority
     * @param entityName the entity (table) name
     * @param matcher    the Uri matcher
     * @param match      the match for this Uri
     * @return the Uri
     */
    public static final Uri buildUriAndAddMatch(final String authority, final String entityName, final UriMatcher matcher, final int match) {
        final Uri uri = buildUri(authority, entityName);
        matcher.addURI(authority, entityName, match);
        return uri;
    }

    /**
     * Build an Uri and add it to a {@link ContentProviderUris}, return the Uri
     *
     * @param cProviderUris the ContentProviderUris helper class
     * @param entityName    the entity (table) name
     * @param match         the match for this Uri
     * @return the Uri
     */
    public static final Uri buildUriAndAdd(final ContentProviderUris cProviderUris, final String entityName, final int match) {
        return cProviderUris.buildAndAddUri(entityName, match);
    }

    private final String mAuthority;
    private final UriMatcher mUriMatcher;
    private final SparseArray<String> mEntityNameMap;
    private final SparseArray<Uri> mUriMap;

    public ContentProviderUris(final String authority) {
        mAuthority = authority;
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mEntityNameMap = new SparseArray<String>();
        mUriMap = new SparseArray<Uri>();
    }

    public String getAuthority() {
        return mAuthority;
    }

    public UriMatcher getUriMatcher() {
        return mUriMatcher;
    }

    public String getEntityName(int match) {
        return mEntityNameMap.get(match);
    }

    public String getEntityName(Uri uri) {
        return getEntityName(mUriMatcher.match(uri));
    }

    public Uri buildAndAddUri(final String entityName, final int match) {
        Uri uri = buildUriAndAddMatch(mAuthority, entityName, mUriMatcher, match);
        mEntityNameMap.put(match, entityName);
        mUriMap.put(match, uri);
        return uri;
    }

    public String getMimetype(Uri uri) {
        return String.format(MIMETYPE_DIR_FORMAT, mAuthority, getEntityName(uri));
    }
}
