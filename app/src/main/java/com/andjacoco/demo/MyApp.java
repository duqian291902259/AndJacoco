package com.andjacoco.demo;

import android.app.Application;
import android.util.Log;

public class MyApp extends Application {
    public static Application app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        Log.d("TAG", "MyApp onCreate");

        CodeCoverageManager.init(app, "http://192.168.56.1:8090");
        CodeCoverageManager.uploadData();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            CodeCoverageManager.generateCoverageFile();
        }
    }
}
