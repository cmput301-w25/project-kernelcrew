package com.kernelcrew.moodapp;

import android.app.Application;
import android.util.Log;

/**
 * Test application class to handle initialization during Robolectric tests.
 * This helps provide a clean environment for testing.
 */
public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TestApplication", "TestApplication initialized for Robolectric tests");
    }
} 