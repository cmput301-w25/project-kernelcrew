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
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.ui.Mood;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MoodDetailsNavigationTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void seedDatabase() {
        // Seed Firestore with a test Mood and corresponding MoodEvent.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Seed a basic Mood document used in the HomeFeed RecyclerView.
        CollectionReference moodsRef = db.collection("moods");
        Mood testMood = new Mood("testMoodId", "dummyUser", "Happy", System.currentTimeMillis());
        moodsRef.document("testMoodId").set(testMood);

        // Seed a detailed MoodEvent document used in the MoodDetails screen.
        CollectionReference moodEventsRef = db.collection("moodEvent");
        MoodEvent testEvent = new MoodEvent(
                "dummyUser",
                Emotion.HAPPINESS,
                "Morning Coffee",       // trigger
                "With Friends",         // socialSituation
                "Celebration",          // reason
                "https://example.com/photo.jpg", // photoUrl
                34.052235,              // latitude
                -118.243683             // longitude
        );
        // Set the id to match the test document id.
        testEvent.setId("testMoodId");
        moodEventsRef.document("testMoodId").set(testEvent);

        // Wait a moment for data to be written.
        SystemClock.sleep(2000);
    }

    @Test
    public void testNavigationToMoodDetails() throws InterruptedException {
        // On AuthHome screen: Click the "Sign In" button.
        onView(withText("Sign In")).perform(click());

        // On AuthSignIn screen: Fill in email and password.
        onView(withId(R.id.email))
                .check(matches(isDisplayed()));
        onView(withId(R.id.email))
                .perform(replaceText("test@kernelcrew.com"), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText("Password@1234"), closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());

        // On HomeFeed screen: Verify that homeTextView is displayed.
        onView(withId(R.id.homeTextView))
                .check(matches(isDisplayed()));

        // Wait for the RecyclerView to load the seeded mood document.
        SystemClock.sleep(2000);

        // Click on the first mood item in the RecyclerView to view its details.
        // (Ensure that your HomeFeed layout contains a RecyclerView with id moodRecyclerView.)
        onView(withId(R.id.moodRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait for the MoodDetails screen to load.
        SystemClock.sleep(2000);

        // Verify that key elements on the MoodDetails screen are displayed and have the correct text.
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
