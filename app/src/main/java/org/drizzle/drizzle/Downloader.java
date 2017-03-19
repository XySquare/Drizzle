package org.drizzle.drizzle;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ttc on 2017/3/13.
 */

public abstract class Downloader extends HandlerThread{
    private static final String TAG = "Downloader";

    private Handler mRequestHandler;
    private Handler mResponseHandler;

    private boolean hasQuit = false;

    private ConcurrentMap<Integer, List<Object>> mRequestMap = new ConcurrentHashMap<>();

    private OnLooperPreparedListener mOnLooperPreparedListener;

    public interface OnLooperPreparedListener {
        void onLooperPrepared();
    }

    public Downloader(Handler responseHandler, int priority ,
                      OnLooperPreparedListener onLooperPreparedListener) {
        super(TAG,priority);
        mResponseHandler = responseHandler;
        mOnLooperPreparedListener = onLooperPreparedListener;
    }

    public void queue(Object target, int what) {

        List<Object> objects = mRequestMap.get(what);

        if (objects == null) {
            objects = new ArrayList<>();
            mRequestMap.put(what, objects);
        }


        if (objects.contains(target)) {
            return;
        }

        objects.add(target);

        mRequestHandler.obtainMessage(what, target)
                        .sendToTarget();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (hasQuit) {
                    return;

                }

                handleRequest(msg);

                if (mRequestMap.get(msg.what) == null) {
                    return;
                }

                mRequestMap.get(msg.what).remove(msg.obj);

                final int what = msg.what;

                 mResponseHandler.post(new Runnable() {
                     @Override
                     public void run() {

                         handleResponse(what);
                     }
                 });
            }

        };

        if (mOnLooperPreparedListener != null) {
            mOnLooperPreparedListener.onLooperPrepared();
        }
    }

    protected abstract void handleRequest(Message msg);
    protected abstract void handleResponse(int what);

    public void clearQueue(int what) {
        mRequestHandler.removeMessages(what);
        mRequestMap.remove(what);
    }

    public void clearQueue(int what, Object target) {
        mRequestHandler.removeMessages(what, target);

        if (mRequestMap.get(what) != null) {
            mRequestMap.get(what).remove(target);
        }

    }

    @Override
    public boolean quit() {
        hasQuit = true;
        return super.quit();
    }
}
