package com.example.pelagiahotelapp;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Cloudinary
        initCloudinary();
    }

    private void initCloudinary() {
        try {
            Map config = new HashMap();
            config.put("cloud_name", "dhroeylg2");
            config.put("api_key", "793559997499565");
            config.put("api_secret", "YKyTw_mX1OqS6k1wcuro2lqGbZQ");
            config.put("secure", true);
            MediaManager.init(this, config);
            Log.d(TAG, "Cloudinary initialized successfully with https");
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary initialization failed: " + e.getMessage());
        }
    }
}