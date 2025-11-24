package com.example.pelagiahotelapp;

import android.content.Context;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    public static void initCloudinary(Context context) {
        Map config = new HashMap();
        config.put("cloud_name", "dhroeylg2");
        config.put("api_key", "793559997499565");
        config.put("api_secret", "YKyTw_mX1OqS6k1wcuro2lqGbZQ");
        MediaManager.init(context, config);

        Log.d("Cloudinary", "Cloudinary initialized");



    }

}