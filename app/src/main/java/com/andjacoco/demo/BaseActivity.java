package com.andjacoco.demo;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "BaseActivity onDestroy");
        //CodeCoverageManager.generateCoverageFile();

        JacocoHelper.INSTANCE.generateEcFile(getApplicationContext(), false);
        CodeCoverageManager.uploadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //JacocoHelper.INSTANCE.generateEcFile(getApplicationContext(), false);
    }
}
