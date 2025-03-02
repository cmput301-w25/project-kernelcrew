package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupClass() {
        // Setup Firestore emulator (if you are using the emulator for testing)
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() {
        // Seed the Firestore "moods" collection with a sample document.
        // This ensures that when HomeFeed loads, it has at least one mood document.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("moods");

        // Create a test mood using the correct constructor parameters:
        // id, userName, moodText, timestamp.
        Mood testMood = new Mood("testMoodId", "dummyUser", "Test mood", System.currentTimeMillis());
        moodsRef.document("testMoodId").set(testMood);

        // Wait for the data to be written
        SystemClock.sleep(2000);
    }

    @Test
    public void testNavigationToHomeFeed() {
        // On AuthHome screen: Click the "Sign In" button.
        // Adjust the matcher below if your AuthHome layout uses a different text or id.
        Espresso.onView(ViewMatchers.withText("Sign In"))
                .perform(ViewActions.click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        Espresso.onView(ViewMatchers.withId(R.id.email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Fill in the email and password fields.
        Espresso.onView(ViewMatchers.withId(R.id.email))
                .perform(ViewActions.replaceText("test@kernelcrew.com"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.password))
                .perform(ViewActions.replaceText("Password@1234"), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        Espresso.onView(ViewMatchers.withId(R.id.signInButton))
                .perform(ViewActions.click());

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        Espresso.onView(ViewMatchers.withId(R.id.homeTextView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @After
    public void tearDown() {
        // Clean up the Firestore "moods" collection by sending an HTTP DELETE to the emulator.
        // Note: Firestore doesn't provide a direct API to delete a collection,
        // so this approach works with the emulator.
        String projectId = "kernelcrew-database";  // Replace with your actual Firebase project ID
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId +
                    "/databases/(default)/documents/moods");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            if (url != null) {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                int response = urlConnection.getResponseCode();
                Log.i("Response Code", "Response Code: " + response);
            }
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
