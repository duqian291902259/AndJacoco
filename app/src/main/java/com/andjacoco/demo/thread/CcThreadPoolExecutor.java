package com.andjacoco.demo.thread;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CcThreadPoolExecutor extends ThreadPoolExecutor {

    private static final String TAG = "CcThreadPoolExecutor";
    private static final boolean DEBUG = false;

    public CcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public CcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        if (DEBUG) {
            //读int值
            Log.i(TAG, "已经提交但未被执行的任务 : " + getQueue().size());
            //遍历操作
            Log.i(TAG, ":【线程活跃数】" + getActiveCount());
            //遍历操作
            Log.i(TAG, ":【总任务数】" + getTaskCount());
            //遍历操作
            Log.i(TAG, ":【已完成任务数】" + getCompletedTaskCount());
        }

        try {
            super.execute(command);
        } catch (Exception e) {
            Log.e(TAG, "execute err : " + e.toString());
        }
    }
}
