package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.Mood;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest {
    // Required for this test to work
    private static final String projectId = "PROJECT_ID"; // replace with your project ID
    private static final String androidLocalhost = "10.0.2.2"; // Ensure this is correct
    private static final int fireStorePort = 8080; // Change to the correct port (if yours varies from default)
    private static final int authPort = 9099; // Change to the correct port (if yours varies from default)

    // Changeable strings
    private static final String TEST_EMAIL = "test@kernelcrew.com";
    private static final String TEST_PASSWORD = "Password@1234";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupClass() {
        // Setup Firestore emulator (if you are using the emulator for testing)
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, fireStorePort);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);
    }

    @Before
    public void setUp() throws InterruptedException {
        createUser(TEST_EMAIL, TEST_PASSWORD);
        SystemClock.sleep(1000);
    }

    @Test
    public void testNavigationToHomeFeed() throws InterruptedException {
        // On AuthHome screen: Click the "Sign In" button.
        // Adjust the matcher below if your AuthHome layout uses a different text or id.
        Espresso.onView(ViewMatchers.withId(R.id.signInButtonInitial))
                .perform(ViewActions.click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        onView(withId(R.id.email))
                .check(matches(isDisplayed()));

        // Fill in the email and password fields.
        onView(withId(R.id.email))
                .perform(replaceText("test@kernelcrew.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText("Password@1234"), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        Espresso.onView(ViewMatchers.withId(R.id.signInButtonAuth))
                .perform(ViewActions.click());

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        onView(withId(R.id.homeTextView))
                .check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        // 1. DELETE all Firestore documents
        HttpURLConnection firestoreConnection = null;
        try {
            URL firestoreUrl = new URL("http://10.0.2.2:8080/emulator/v1/projects/"
                    + projectId + "/databases/(default)/documents");
            firestoreConnection = (HttpURLConnection) firestoreUrl.openConnection();
            firestoreConnection.setRequestMethod("DELETE");
            int firestoreResponse = firestoreConnection.getResponseCode();
            Log.i("Firestore Wipe", "Deleted Firestore docs. Response code: " + firestoreResponse);
        } catch (IOException e) {
            Log.e("Firestore Wipe Error", e.getMessage());
        } finally {
            if (firestoreConnection != null) {
                firestoreConnection.disconnect();
            }
        }

        // 2. DELETE all Auth emulator users
        HttpURLConnection authConnection = null;
        try {
            // Use the Auth emulator port (9099 below, or whatever you set up)
            URL authUrl = new URL("http://10.0.2.2:9099/emulator/v1/projects/"
                    + projectId + "/accounts");
            authConnection = (HttpURLConnection) authUrl.openConnection();
            authConnection.setRequestMethod("DELETE");
            int authResponse = authConnection.getResponseCode();
            Log.i("Auth Wipe", "Deleted all Auth users. Response code: " + authResponse);
        } catch (IOException e) {
            Log.e("Auth Wipe Error", e.getMessage());
        } finally {
            if (authConnection != null) {
                authConnection.disconnect();
            }
        }
    }

    private void createUser(String email, String password) throws InterruptedException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> createUserTask = auth.createUserWithEmailAndPassword(email, password);
        while (!createUserTask.isComplete()) {
            Thread.sleep(200);
        }
    }
}
