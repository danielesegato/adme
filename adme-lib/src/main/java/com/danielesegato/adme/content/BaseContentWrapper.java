package com.danielesegato.adme.content;

import android.annotation.TargetApi;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Base implemmentation of a {@link ContentWrapper} that just handle the content set/get
 */
public abstract class BaseContentWrapper<T> implements ContentWrapper<T> {
    protected final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private final ContentObserver INTERNAL_OBSERVER = new InternalContentObserver();
    private final ContentObservable mContentObservable = new ContentObservable();
    private int mObservedCount;
    protected T mContent;

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
    public void setContent(@Nullable T content) {
        mContent = content;
    }

    @Override
    @Nullable
    public T getContent() {
        return mContent;
    }

    @Override
    public final void registerContentObserver(@NonNull ContentObserver observer) {
        synchronized (mContentObservable) {
            if (mObservedCount == 0) {
                mContentObservable.registerObserver(INTERNAL_OBSERVER);
            }
            mContentObservable.registerObserver(observer);
            mObservedCount++;
        }
    }

    @Override
    public final void unregisterContentObserver(@NonNull ContentObserver observer) {
        synchronized (mContentObservable) {
            mContentObservable.unregisterObserver(observer);
            mObservedCount--;
            if (mObservedCount == 0) {
                mContentObservable.unregisterObserver(INTERNAL_OBSERVER);
            }
        }
    }

    @Override
    public void close() {
        synchronized (mContentObservable) {
            mContentObservable.unregisterAll();
            mObservedCount = 0;
        }
    }

    protected final ContentObserver getInternalObserver() {
        return INTERNAL_OBSERVER;
    }

    protected final class InternalContentObserver extends ContentObserver {
        public InternalContentObserver() {
            super(MAIN_HANDLER);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            mContentObservable.dispatchChange(selfChange);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentObservable.dispatchChange(selfChange, uri);
        }
    }
}
