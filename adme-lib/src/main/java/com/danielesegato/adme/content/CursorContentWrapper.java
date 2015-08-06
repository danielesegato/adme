package com.danielesegato.adme.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danielesegato.adme.ADME;
import com.danielesegato.adme.InternalADMEConsts;

import java.util.List;

/**
 * A content wrapper for a {@link Cursor}. The {@link ContentObserver} mechanism rely on the standard
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
 * @see CursorContentWrapper
 * @see com.danielesegato.adme.provider.ADMEContentProvider
 * @see com.danielesegato.adme.provider.ADMEContentProviderComponent
 */
public class CursorContentWrapper<T> extends BaseContentWrapper<T> {

    /**
     * Wraps a cursor and a content.
     *
     * @param cursor  the cursor
     * @param content the content
     * @param <X>     the content data type
     * @return the wrapper
     */
    public static
    @NonNull
    <X> CursorContentWrapper<X> wrap(@NonNull Cursor cursor, @Nullable X content) {
        CursorContentWrapper<X> wrapper = new CursorContentWrapper<>();
        wrapper.changeCursor(cursor);
        wrapper.setContent(content);
        return wrapper;
    }

    /**
     * Wraps a cursor and generate the content from a list of
     * {@link com.danielesegato.adme.annotation.ADMEEntity} annotated class.
     *
     * @param cursor the cursor
     * @param clazz  the content type
     * @param <X>    the content data type
     * @return the wrapper
     */
    public static
    @NonNull
    <X> CursorContentWrapper<X> wrap(@NonNull Cursor cursor, @NonNull Class<X> clazz) {
        CursorContentWrapper<X> wrapper = new CursorContentWrapper<>();
        wrapper.changeCursor(cursor);
        wrapper.setContent(ADME.cursorToEntity(cursor, clazz));
        return wrapper;
    }

    /**
     * Wraps a cursor and generate the content from an
     * {@link com.danielesegato.adme.annotation.ADMEEntity} annotated class.
     *
     * @param cursor the cursor
     * @param clazz  the content type
     * @param <X>    the content data type
     * @return the wrapper
     */
    public static
    @NonNull
    <X> CursorContentWrapper<List<X>> wrapList(@NonNull Cursor cursor, @NonNull Class<X> clazz) {
        CursorContentWrapper<List<X>> wrapper = new CursorContentWrapper<>();
        wrapper.changeCursor(cursor);
        wrapper.setContent(ADME.cursorToEntityList(cursor, clazz));
        return wrapper;
    }

    private Cursor mCursor;

    /**
     * Change the wrapped Cursor.
     * <p/>
     * Be aware this does NOT cause a notification to the Observers!
     *
     * @param cursor the new Cursor
     */
    public Cursor changeCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        this.mCursor = cursor;
        if (oldCursor != null) {
            oldCursor.unregisterContentObserver(getInternalObserver());
        }
        if (cursor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri notificationUri = cursor.getNotificationUri();
                if (notificationUri == null) {
                    Log.w(InternalADMEConsts.LOGTAG, "wrapping Cursor without notification Uri, did you set one in the ContentProvider?");
                }
            }
            cursor.registerContentObserver(getInternalObserver());
        }
        return oldCursor;
    }

    /**
     * @return the wrapped Cursor
     */
    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public void close() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
