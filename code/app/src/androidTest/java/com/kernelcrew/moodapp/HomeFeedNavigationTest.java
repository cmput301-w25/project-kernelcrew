package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import android.os.SystemClock;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.Mood;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest {
    private static final String TEST_EMAIL = "test@kernelcrew.com";
    private static final String TEST_PASSWORD = "Password@1234";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupClass() {
        // Setup Firestore emulator (if you are using the emulator for testing)
        String androidLocalhost = "10.0.2.2";
        int fireStorePort = 8080;
        int authPort = 9099;

        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, fireStorePort);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, 9099);
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
        // Clean up the Firestore "moods" collection by sending an HTTP DELETE to the emulator.
        // Note: Firestore doesn't provide a direct API to delete a collection,
        // so this approach works with the emulator.
        String projectId = "YOUR_PROJECT_ID";  // TODO: Replace with your actual Firebase project ID
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

    private void createUser(String email, String password) throws InterruptedException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> createUserTask = auth.createUserWithEmailAndPassword(email, password);
        while (!createUserTask.isComplete()) {
            Thread.sleep(200);
        }
    }
}
