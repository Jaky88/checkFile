package com.onyx.listfile.request;

/**
 * Created by 12345 on 2017/4/7.
 */

public abstract class RequestCallback<I,O> {

    public void onStart(I i) {
    }

    public abstract O onDoInBackground(I i);

    public void onResult(O o) {
    }
}
