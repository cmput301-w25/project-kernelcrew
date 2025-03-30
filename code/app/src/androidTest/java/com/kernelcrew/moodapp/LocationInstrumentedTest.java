package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.not;

import android.Manifest;
import android.os.SystemClock;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for location functionality in the app.
 * These tests verify the UI and data persistence related to location features.
 * Created by Anthropic, Claude 3.7 Sonnet, "Develop LocationInstrumentedTest for Android", accessed 03-30-2025
 */
@RunWith(AndroidJUnit4.class)
public class LocationInstrumentedTest extends FirebaseEmulatorMixin {

    private static final String TEST_EMAIL = "test@kernelcrew.com";
    private static final String TEST_PASSWORD = "Password@1234";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule locationPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    );

    /**
     * Sets up the test database with required test data before any tests run.
     * This includes:
     * - Disabling animations for more reliable testing
     * - Creating a test user
     * - Ensuring the user is signed in
     * - Creating an initial test mood event
     *
     * @throws ExecutionException if a task fails
     * @throws InterruptedException if tasks are interrupted
     */
    @BeforeClass
    public static void setupDatabase() throws ExecutionException, InterruptedException {
        // Disable animations via UI Automator
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            device.executeShellCommand("settings put global window_animation_scale 0");
            device.executeShellCommand("settings put global transition_animation_scale 0");
            device.executeShellCommand("settings put global animator_duration_scale 0");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create user first
        staticCreateUser();

        // Ensure user is signed in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Sign in the test user
            Tasks.await(auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
        }

        // Verify user is signed in
        assertNotNull("User should be signed in", auth.getCurrentUser());

        // Create test mood event
        MoodEvent moodEvent = new MoodEvent(
                auth.getCurrentUser().getUid(),
                "Username",
                Emotion.HAPPINESS,
                "Test Social Situation",
                "Test Reason",
                null,
                null
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(moodEvent));
    }

    /**
     * Setup method that runs before each test.
     * Ensures user is logged in and navigates to the app if needed.
     * Sets the activity to the RESUMED state to make sure it's ready for testing.
     *
     * @throws ExecutionException if a task fails
     * @throws InterruptedException if tasks are interrupted
     */
    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        // Ensure user is logged in before each test
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Navigate through sign in flow
            onView(withText("Sign In")).perform(click());
            onView(withId(R.id.emailSignIn)).perform(replaceText(TEST_EMAIL));
            onView(withId(R.id.passwordSignIn)).perform(replaceText(TEST_PASSWORD));
            onView(withId(R.id.signInButtonAuthToHome)).perform(click());

            // Wait for home screen to load
            SystemClock.sleep(2000);
        }

        // Ensure activity is in RESUMED state
        activityScenarioRule.getScenario().moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
    }

    /**
     * Utility method to sign in the test user.
     * Used as a helper method for setting up test authentication.
     *
     * @throws ExecutionException if the sign-in task fails
     * @throws InterruptedException if the sign-in task is interrupted
     */
    private static void staticSignInUser() throws ExecutionException, InterruptedException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Tasks.await(auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
    }

    /**
     * Tests that a location can be added to a mood event successfully.
     * Verifies:
     * - The add location button is clickable
     * - After adding a location, the card and remove button are visible
     */
    @Test
    public void testLocationUIStateAfterAddingLocation() {
        // Navigate to create mood event page
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(3000); // Longer wait time for layout

        // More direct approach to find and click the button
        try {
            // Find all views that match and try to click the first visible one
            onView(withId(R.id.add_location_button))
                    .perform(scrollTo(), click());

            // Wait for the map to load
            SystemClock.sleep(3000);

            // Verify cardLocation and remove button are now visible
            onView(withId(R.id.cardLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.remove_location_button)).check(matches(isDisplayed()));
        } catch (Exception e) {
            fail("Could not interact with add_location_button: " + e.getMessage());
        }
    }

    /**
     * Tests that a mood event with a location can be submitted successfully.
     * Verifies:
     * - A mood with location can be created via UI
     * - The location data (latitude and longitude) is persisted to Firestore
     */
    @Test
    public void testSubmitMoodEventWithLocation() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String testReason = "Test location mood";

        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(testReason));

        onView(withId(R.id.add_location_button)).perform(scrollTo(), click());
        SystemClock.sleep(2000);

        onView(withId(R.id.submit_button)).perform(scrollTo(), click());
        SystemClock.sleep(2000);

        // Java 11 compatible version of the verification
        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                        List<DocumentSnapshot> moodEvents = results.getDocuments();

                        for (DocumentSnapshot doc : moodEvents) {
                            if (testReason.equals(doc.getString("reason"))) {
                                Object lat = doc.get("latitude");
                                Object lon = doc.get("longitude");
                                return (lat != null && lon != null);
                            }
                        }
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }
}