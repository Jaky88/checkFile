package com.onyx.listfile.request;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 12345 on 2017/4/7.
 */

public class AsyncRequest<I, O> {
    public final static int DEFAULT_REQUEST = 0;
    public final static int NATIVE_DATA_REQUEST = 1;
    public final static int REMOTE_DATA_REQUEST = 2;

    private static Handler handler;
    private RequestCallback<I, O> callback;
    private int requestType = 0;
    private I i;
    private static ExecutorService executorService = Executors.newFixedThreadPool(15);

    public AsyncRequest(int requestType, I i, RequestCallback<I, O> callback) {
        this.requestType = requestType;
        this.callback = callback;
        this.i = i;
    }

    public AsyncRequest(int requestType, RequestCallback<I, O> callback) {
        new AsyncRequest(requestType, new Object(), callback);
    }

    public void execute() {
        callback.onStart(i);
        run();
    }

    private void run() {
        if (requestType == 2) {

        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Message message = Message.obtain();
                    message.obj = new ResultData<O>(AsyncRequest.this, callback.onDoInBackground(i));
                    getHandler().sendMessage(message);
                }
            });
        }

    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    private static class MyHandler<O> extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ResultData<O> resultData = (ResultData<O>) msg.obj;
            resultData.asyncRequest.callback.onResult(resultData.data);
        }
    }

    private static Handler getHandler() {
        if (handler == null) {
            synchronized (MyHandler.class) {
                if (handler == null) {
                    handler = new MyHandler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }


    private static class ResultData<O> {
        AsyncRequest asyncRequest;
        O data;

        public ResultData(AsyncRequest asyncRequest, O data) {
            this.asyncRequest = asyncRequest;
            this.data = data;
        }
    }


}

