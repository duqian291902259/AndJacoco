package com.andjacoco.demo;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.jacoco.agent.rt.CodeCoverageManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "BaseActivity onDestroy");
        CodeCoverageManager.generateCoverageFile();

       // CCJacocoHelper.INSTANCE.generateEcFile(getApplicationContext(), true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CCJacocoHelper.INSTANCE.generateEcFile(getApplicationContext(), true);
    }
}
