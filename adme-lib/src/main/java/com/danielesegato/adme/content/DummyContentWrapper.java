package com.danielesegato.adme.content;

/**
 * A dummy content wrapper with a never changing content
 */
public class DummyContentWrapper<T> extends BaseContentWrapper<T> {

    public DummyContentWrapper(T data) {
        super();
        setContent(data);
    }

    @Override
    public void close() {
    }
}
