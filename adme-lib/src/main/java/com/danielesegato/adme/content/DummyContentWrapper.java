package com.danielesegato.adme.content;

import android.database.ContentObserver;

/**
 * A dummy content wrapper with a never changing content
 */
public class DummyContentWrapper<T> extends ContentWrapper<T> {

    public DummyContentWrapper(T data) {
        super();
        setContent(data);
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
    }

    @Override
    public void close() {
    }
}
