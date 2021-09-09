package com.andjacoco.demo.thread;

import android.util.Log;

import com.andjacoco.demo.BuildConfig;
public class SimpleSafetyTask implements Runnable {
    private static final String TAG = "SimpleSafetyTask";

    public static Runnable buildSafetyTask(final Runnable task) {
        Log.i(TAG, "buildSafetyTask ");
        if (BuildConfig.DEBUG || task == null) {
            //内网不做兜底
            return task;
        } else {
            //外网保护一下
            return new SimpleSafetyTask(task);
        }
    }

    private Runnable originalTask;

    SimpleSafetyTask(Runnable originalTask) {
        this.originalTask = originalTask;
    }

    @Override
    public void run() {
        try {
            originalTask.run();
        } catch (Throwable e) {
            Log.w(TAG, "catch exception");
            Log.w(TAG, e);
        }
    }
}
