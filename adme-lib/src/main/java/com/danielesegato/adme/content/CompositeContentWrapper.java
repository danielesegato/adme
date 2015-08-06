package com.danielesegato.adme.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.danielesegato.adme.InternalADMEConsts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A content wrapper for multiple {@link Cursor}s. The {@link ContentObserver} mechanism rely on the
 * standard Android way of handling it.
 * <p/>
 * This allow you to perform multiple calls and create a composite result on different Cursors while
 * still getting the auto-refresh feature from the underling cursors.
 * <p/>
 * You are supposed to set on your Cursors a notification {@link android.net.Uri} when you build your
 * Cursors (usually in a {@link android.content.ContentProvider}). And then you need to call
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
public class CompositeContentWrapper<T> extends BaseContentWrapper<T> {

    private List<Cursor> mCursors = new ArrayList<>();

    /**
     * Add a wrapped Cursor.
     * <p/>
     * Be aware this does NOT cause a notification to the Observers! Nor it carry the previous
     * registered observers to the new cursor.
     *
     * @param cursor the new Cursor
     */
    public void addCursor(@NonNull Cursor cursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Uri notificationUri = cursor.getNotificationUri();
            if (notificationUri == null) {
                Log.w(InternalADMEConsts.LOGTAG, "wrapping Cursor without notification Uri, did you set one in the ContentProvider?");
            }
        }
        cursor.registerContentObserver(getInternalObserver());
        this.mCursors.add(cursor);
    }

    /**
     * Remove a wrapped Cursor.
     * @param cursor
     */
    public void removeCursor(@NonNull Cursor cursor) {
        if (this.mCursors.remove(cursor)) {
            cursor.unregisterContentObserver(getInternalObserver());
        }
    }

    /**
     * @return the wrapped Cursors list
     */
    public List<Cursor> getCursors() {
        return mCursors;
    }

    @Override
    public void close() {
        Iterator<Cursor> ite = mCursors.iterator();
        while (ite.hasNext()) {
            Cursor cursor = ite.next();
            if (!cursor.isClosed()) {
                cursor.close();
            }
            ite.remove();
        }
    }
}
