package com.danielesegato.adme.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * A content wrapper for {@link Cursor}s. The {@link ContentObserver} mechanism rely on the standard
 * Android way of handling it.
 * <p/>
 * You are supposed to set on your Cursor a notification {@link android.net.Uri} when you build your
 * Cursor (usually in a {@link android.content.ContentProvider}). And then you need to call
 * {@link android.content.ContentResolver#notifyChange(Uri, ContentObserver)} on that Uri whenever a
 * change is performed on the data that you want to get on your UI. Usually this is done in the
 * insert / update / delete methods of your ContentProvider but you can handle it in any way you want.
 * <p/>
 * If you use {@link com.danielesegato.adme.provider.ADMEContentProvider} it has a build in method
 * for handling related notification uris. For example you may have defined a View in your database
 * and you may want any change on any table that influence that view to trigger a refresh on your UI.
 * That can be done by just setting up related notification uris and properly set up the notification
 * Uri on Cursors in your query methods.
 *
 * @see ADMEWrapperUtil
 * @see com.danielesegato.adme.provider.ADMEContentProvider
 * @see com.danielesegato.adme.provider.ADMEContentProviderComponent
 */
public class CursorContentWrapper<T> extends ContentWrapper<T> {

    private Cursor mCursor;

    /**
     * {@inheritDoc}
     * <p/>
     * <strong>Implementation details:</strong>
     * <p/>
     * Change the wrapped Content. This method is meant to be used in initialization only.
     * <p/>
     * Be aware this does NOT cause a notification to the Observers!
     *
     * @param content the new content
     */
    @Override
    public void setContent(T content) {
        super.setContent(content);
    }

    /**
     * Change the wrapped Cursor. This method is meant to be used in initialization only.
     * <p/>
     * Be aware this does NOT cause a notification to the Observers! Nor it carry the previous
     * registered observers to the new cursor.
     *
     * @param cursor the new Cursor
     */
    public void setCursor(@NonNull Cursor cursor) {
        this.mCursor = cursor;
    }

    /**
     * @return the wrapped Cursor
     */
    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        if (mCursor == null) {
            throw new IllegalStateException("No cursor has been set");
        }
        mCursor.registerContentObserver(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        if (mCursor != null) {
            mCursor.unregisterContentObserver(observer);
        }
    }

    @Override
    public void close() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
