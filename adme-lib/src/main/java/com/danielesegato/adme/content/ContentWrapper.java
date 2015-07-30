package com.danielesegato.adme.content;

import android.database.ContentObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * An abstract observable container that allow registering for changes and closing freeing resources
 * when not needed anymore.
 * <p/>
 * The content can be of any type.
 * <p/>
 * The original purpose of this class is to abstract the use of {@link android.database.Cursor} for
 * allowing the use of a java class without losing the ability to register on a Cursor to know when
 * the data changes. Usually a {@link android.content.Loader} is used to load the data and it should
 * be able to automatically refresh itself if data changes.
 *
 * @see CursorContentWrapper
 * @see ADMEWrapperUtil
 * @see ADMEContentLoader
 * @see ADMEContentLoaderCompat
 */
public abstract class ContentWrapper<T> {
    protected T mContent;

    /**
     * Set a new content, its up to the implementation to chose whatever this should trig a content
     * change notification to observers or not.
     *
     * @param content the new content
     */
    public void setContent(@Nullable T content) {
        mContent = content;
    }

    /**
     * @return the current content wrapped
     */
    @Nullable
    public T getContent() {
        return mContent;
    }

    /**
     * Register an observer that is called when changes happen to the content backing this wrapper.
     *
     * @param observer the object that gets notified when the content backing the wrapper changes.
     */
    public abstract void registerContentObserver(@NonNull ContentObserver observer);

    /**
     * Unregister an observer that has previously been registered with this wrapper via
     * {@link #registerContentObserver(ContentObserver)}.
     *
     * @param observer the object to unregister.
     */
    public abstract void unregisterContentObserver(@NonNull ContentObserver observer);

    /**
     * Closes the Wrapper, releasing all of its resources and making it completely invalid.
     */
    public abstract void close();
}
