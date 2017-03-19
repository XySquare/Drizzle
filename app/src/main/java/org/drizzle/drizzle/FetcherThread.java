package org.drizzle.drizzle;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public abstract class FetcherThread extends HandlerThread {
    private static final String TAG = "FetcherThread";

    private Boolean mHasQuit = false;
    private Handler mRequestHandler;

    public FetcherThread() {
        super(TAG);
    }

    @Override
    protected void onLooperPrepared() {
        //FIXME:Men Leak?
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleRequest(msg);
            }
        };
    }

    protected abstract void handleRequest(Message msg);

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueFetch(int what, Object id) {
        mRequestHandler.obtainMessage(what, id).sendToTarget();
    }

    public void clearQueue(int what) {
        mRequestHandler.removeMessages(what);
    }

    public void clearQueue(int what, Object obj) {
        mRequestHandler.removeMessages(what, obj);
    }

    public Boolean hasQuit() {
        return mHasQuit;
    }

}
