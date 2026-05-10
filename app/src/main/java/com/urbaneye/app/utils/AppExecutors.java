package com.urbaneye.app.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static volatile AppExecutors instance;
    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    private AppExecutors() {
        diskIO = Executors.newSingleThreadExecutor();
        networkIO = Executors.newFixedThreadPool(4);
        mainThread = command -> new Handler(Looper.getMainLooper()).post(command);
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    public Executor diskIO() { return diskIO; }
    public Executor networkIO() { return networkIO; }
    public Executor mainThread() { return mainThread; }
}
