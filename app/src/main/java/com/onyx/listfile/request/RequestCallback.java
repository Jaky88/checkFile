package com.onyx.listfile.request;

/**
 * Created by 12345 on 2017/4/7.
 */

public abstract class RequestCallback<O> {

    public void onStart() {
    }

    public abstract O onDoInBackground();

    public void onResult(O o) {
    }
}
