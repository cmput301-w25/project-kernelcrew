package com.kernelcrew.moodapp;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class FirebaseEmulatorMixin {
    // Shared test constants (changed to protected so subclasses can use them)
    protected static final String TEST_EMAIL = "test@kernelcrew.com";
    protected static final String TEST_PASSWORD = "Password@1234";

    // Emulator configuration
    private static boolean setupEmulator = false;
    private static final String androidLocalhost = "10.0.2.2";
    private static final Integer databasePort = 8080;
    private static final Integer authPort = 9099;

    @BeforeClass
    public static void setup() {
        if (setupEmulator) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                auth.signOut();
            }
            return;
        }
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, databasePort);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);
        setupEmulator = true;
    }

    @After
    public void teardown() throws IOException {
        // Just clear the moodEvents

        String projectId = FirebaseApp.getInstance().getOptions().getProjectId();
        URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId +
                "/databases/(default)/documents/moodEvents");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        int response = urlConnection.getResponseCode();
        Log.i("Response Code", "Response Code: " + response);
        urlConnection.disconnect();
    }

    @AfterClass
    public static void teardownAll() throws IOException {
        // Clear the entire db and users list

        String projectId = FirebaseApp.getInstance().getOptions().getProjectId();
        URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId +
                "/databases/(default)/documents/moodEvents");
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

        url = new URL("http://" + androidLocalhost + ":9099/emulator/v1/projects/" + projectId +
                "/databases/(default)/documents/users");
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        response = urlConnection.getResponseCode();
        Log.i("Response Code", "Response Code: " + response);
        urlConnection.disconnect();

        url = new URL("http://" + androidLocalhost + ":9099/emulator/v1/projects/" + projectId +
                "/databases/(default)/documents/usernames");
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        response = urlConnection.getResponseCode();
        Log.i("Response Code", "Response Code: " + response);
        urlConnection.disconnect();

        // Clear the current user
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().getCurrentUser().delete();
        }
        clearUser();
    }

    // Instance method to create a user for tests.
    protected void createUser() throws InterruptedException, ExecutionException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> createUserTask = auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        Tasks.await(createUserTask);
    }

    // Static helper for use in static seed methods.
    protected static void staticCreateUser() throws InterruptedException, ExecutionException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
            // Ignore collision since the user already exists.
        }
    }

    // Clear current user.
    protected static void clearUser() {
        FirebaseAuth.getInstance().signOut();
    }

    // Login
    protected static void loginUser() throws ExecutionException, InterruptedException {
        Tasks.await(FirebaseAuth.getInstance().signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
    }
}