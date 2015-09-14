package com.danielesegato.adme.content;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.danielesegato.adme.InternalADMEConsts;

/**
 * An abstract {@link BaseContentWrapper} loader that automatically start loading on initialization.
 * <p/>
 * It cache the value returned by {@link #loadContentInBackground()} and automatically refresh reload
 * in background when the data behind the ContentWrapper changes.
 * <p/>
 * It automatically handle the wrapper close method and observer registering.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class ADMEContentLoader<D> extends AsyncTaskLoader<ContentWrapper<D>> {

    private final ForceLoadContentObserver observer;
    protected ContentWrapper<D> mContent;
    private ContentWrapper<D> mContentObserved;

    public ADMEContentLoader(Context context) {
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
                unobserve(data);
                data.close();
            }
            return;
        }

        ContentWrapper<D> oldContent = mContent;

        boolean dataChanged = data != oldContent;
        boolean needCloseOld = oldContent != null && dataChanged;
        if (needCloseOld) {
            unobserve(oldContent);
        }

        observe(data);
        mContent = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
        if (needCloseOld) {
            oldContent.close();
        }
    }

    @Override
    public void onContentChanged() {
        // avoid recursive calls
        unobserve(mContentObserved);
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
            data.close();
        }
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mContent != null) {
            unobserve(mContent);
            mContent.close();
        }
        mContent = null;
    }

    private synchronized void unobserve(ContentWrapper<D> content) {
        if (content != mContentObserved || content == null) {
            return;
        }
        try {
            content.unregisterContentObserver(observer);
        } catch (IllegalStateException e) {
            Log.w(InternalADMEConsts.LOGTAG, String.format("unregisterContentObserver(): %s - NOT NEEDED, wasn't registered", content), e);
        }
        mContentObserved = null;
    }

    private synchronized void observe(ContentWrapper<D> content) {
        if (content == mContentObserved) {
            return;
        }
        if (mContentObserved != null) {
            Log.w(InternalADMEConsts.LOGTAG, String.format("registering observer when another content is already observed: %s -> %s", mContentObserved, content));
            unobserve(mContentObserved);
        }
        if (content == null) {
            return;
        }
        try {
            content.registerContentObserver(observer);
            mContentObserved = content;
        } catch (IllegalStateException e) {
            Log.w(InternalADMEConsts.LOGTAG, String.format("registerContentObserver(): %s - NOT NEEDED, already registered", content), e);
        }
    }
}
