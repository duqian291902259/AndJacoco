package com.andjacoco.demo.thread;

import android.util.SparseArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2;

    private static SparseArray<Future<?>> taskMap = new SparseArray<>();

    private static ExecutorService fixedThreadPool = new CcThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public static void runOnThread(final Runnable task) {
        fixedThreadPool.execute(SimpleSafetyTask.buildSafetyTask(task));
    }

    public static void submitTask(Runnable task) {
        Future future = fixedThreadPool.submit(task);
        taskMap.put(task.hashCode(), future);
    }

    public static void stopTask(Runnable task) {
        int callableHashCode = task.hashCode();
        Future future = taskMap.get(callableHashCode);
        if (future != null) {
            if (!future.isDone()) {
                future.cancel(false);
            }
            taskMap.remove(callableHashCode);
        }
    }
}
