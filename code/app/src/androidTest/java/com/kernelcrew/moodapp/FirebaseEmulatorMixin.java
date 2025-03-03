package com.kernelcrew.moodapp;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FirebaseEmulatorMixin {
    private static final String androidLocalhost = "10.0.2.2";

    @BeforeClass
    public static void setup() {
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, 8080);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, 9099);
    }

    @After
    public void teardown() throws MalformedURLException, IOException {
        // Clean up the Firestore "moodEvent" collection by sending an HTTP DELETE to the emulator.
        String projectId = FirebaseApp.getInstance().getOptions().getProjectId();

        URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId +
                    "/databases/(default)/documents/moodEvent");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        int response = urlConnection.getResponseCode();
        Log.i("Response Code", "Response Code: " + response);
        urlConnection.disconnect();

        url = new URL("http://" + androidLocalhost + ":9099/emulator/v1/projects/" + projectId +
                    "/accounts");
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        response = urlConnection.getResponseCode();
        Log.i("Response Code", "Response Code: " + response);
        urlConnection.disconnect();
    }
}
