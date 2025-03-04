package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.Mood;
import com.kernelcrew.moodapp.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void seedDatabase() {
        // Seed the Firestore "moods" and "moodEvents" collections with sample documents.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference for the basic Mood document used in the HomeFeed's RecyclerView.
        CollectionReference moodsRef = db.collection("moods");
        // Create a test Mood (id, userName, moodText, timestamp).
        Mood testMood = new Mood("testMoodId", "dummyUser", "Happy", System.currentTimeMillis());
        moodsRef.document("testMoodId").set(testMood);

        // Reference for the detailed MoodEvent document used in MoodDetails.
        CollectionReference moodEventsRef = db.collection("moodEvents");
        // Create a test MoodEvent with all required fields.
        // Ensure your Emotion enum defines a constant HAPPY.
        MoodEvent testEvent = new MoodEvent(
                "dummyUser",
                Emotion.HAPPY,
                "Morning Coffee",       // trigger
                "With Friends",         // socialSituation
                "Celebration",          // reason
                "https://example.com/photo.jpg", // photoUrl
                34.052235,              // latitude
                -118.243683             // longitude
        );
        // Overwrite the auto-generated id to match our test document id.
        testEvent.setId("testMoodId");
        moodEventsRef.document("testMoodId").set(testEvent);

        // Wait for the data to be written (replace with an IdlingResource in production)
        SystemClock.sleep(2000);
    }

    @Test
    public void testNavigationToHomeFeedAndMoodDetails() throws InterruptedException {
        // On AuthHome screen: Click the "Sign In" button.
        onView(withText("Sign In"))
                .perform(click());

        // On AuthSignIn screen: Verify the email field is displayed.
        onView(withId(R.id.email))
                .check(matches(isDisplayed()));

        // Fill in email and password fields.
        onView(withId(R.id.email))
                .perform(replaceText("test@kernelcrew.com"), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText("Password@1234"), closeSoftKeyboard());

        // Click the sign in button.
        onView(withId(R.id.signInButton))
                .perform(click());

        // On HomeFeed screen: Verify homeTextView is displayed.
        onView(withId(R.id.homeTextView))
                .check(matches(isDisplayed()));

        // Wait for the RecyclerView to load the seeded mood document.
        SystemClock.sleep(2000);

        // Click the "View Details" button on the first item in the RecyclerView.
        // (Ensure your item_mood.xml contains a view with ID viewDetailsButton.)
        onView(withId(R.id.moodRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait for MoodDetails screen to load.
        SystemClock.sleep(2000);

        // Verify that the MoodDetails screen is displayed and fields match seeded data.
        onView(withId(R.id.tvMoodState))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvMoodState))
                .check(matches(withText("Happy")));
        onView(withId(R.id.tvTriggerValue))
                .check(matches(withText("Morning Coffee")));
        onView(withId(R.id.tvSocialSituationValue))
                .check(matches(withText("With Friends")));
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText("Celebration")));
    }
}
