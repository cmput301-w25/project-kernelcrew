package com.kernelcrew.moodapp;
//Code from Claude AI, Anthropic, "Configure Android unit testing with JUnit", accessed 03-05-2025
//Code from Anthropic, Claude 3.7 Sonnet, "Add Firestore emulator testing", accessed 05-13-2024
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