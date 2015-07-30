package com.danielesegato.adme.content;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

/**
 * An abstract {@link ContentWrapper} loader that automatically start loading on initialization.
 * <p/>
 * It cache the value returned by {@link #loadContentInBackground()} and automatically refresh reload
 * in background when the data behind the ContentWrapper changes.
 * <p/>
 * It automatically handle the wrapper close method and observer registering.
 */
public abstract class ADMEContentLoaderCompat<D> extends AsyncTaskLoader<ContentWrapper<D>> {

    private final ForceLoadContentObserver observer;
    protected ContentWrapper<D> mContent;

    public ADMEContentLoaderCompat(Context context) {
        super(context);
        this.observer = new ForceLoadContentObserver();
    }

    @Override
    protected void onStartLoading() {
        if (mContent != null) {
            deliverResult(mContent);
        }

        if (mContent == null || takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(ContentWrapper<D> data) {
        if (isReset()) {
            if (data != null) {
                data.close();
            }
            return;
        }

        ContentWrapper<D> oldContent = mContent;
        mContent = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
        if (oldContent != null) {
            oldContent.unregisterContentObserver(observer);
            oldContent.close();
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    @Override
    public final ContentWrapper<D> loadInBackground() {
        ContentWrapper<D> content = loadContentInBackground();
        content.registerContentObserver(observer);
        return content;
    }

    public abstract
    @NonNull
    ContentWrapper<D> loadContentInBackground();

    @Override
    public void onCanceled(ContentWrapper<D> data) {
        if (data != null) {
            data.unregisterContentObserver(observer);
            data.close();
        }
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mContent != null) {
            mContent.close();
        }
        mContent = null;
    }
}
