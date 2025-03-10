package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.DeleteDialogFragment;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DeleteDialogFragmentTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        staticCreateUser();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        MoodEvent moodEvent = new MoodEvent(
                auth.getCurrentUser().getUid(),
                Emotion.HAPPINESS,
                "Test Trigger",
                "Test Social Situation",
                "Test Reason",
                "",
                0.0,
                0.0
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(moodEvent));
    }

    @Test
    public void testDeleteDialogDisplay() {
        // Navigate to the MoodDetails screen
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        MoodDetailsNavigationTest.clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for the MoodDetails screen to load
        SystemClock.sleep(2000);

        // Click the delete button
        onView(withId(R.id.btnDeleteMood)).perform(click());

        // Verify dialog title and buttons are displayed
        onView(withId(R.id.delete_dialog_title)).check(matches(isDisplayed()));
        onView(withId(R.id.delete_dialog_message)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_keep)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete)).check(matches(isDisplayed()));

        // Verify text content
        onView(withId(R.id.delete_dialog_title)).check(matches(withText("Delete this mood event?")));
        onView(withId(R.id.btn_keep)).check(matches(withText("No, Keep It")));
        onView(withId(R.id.btn_delete)).check(matches(withText("Yes, Delete It")));

        // Dismiss dialog by clicking the Keep button
        onView(withId(R.id.btn_keep)).perform(click());
    }

    @Test
    public void testKeepButtonDismissesDialog() {
        // Navigate to the MoodDetails screen
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        MoodDetailsNavigationTest.clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for the MoodDetails screen to load
        SystemClock.sleep(2000);

        // Click the delete button
        onView(withId(R.id.btnDeleteMood)).perform(click());

        // Verify dialog appears
        onView(withId(R.id.delete_dialog_title)).check(matches(isDisplayed()));

        // Click the Keep button
        onView(withId(R.id.btn_keep)).perform(click());

        // Verify dialog is dismissed (by checking that the MoodDetails screen is still visible)
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));

        // Verify mood still exists in database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    assertEquals(1, moodEvents.size());
                });
    }

    @Test
    public void testDeleteButtonDeletesMood() {
        // Navigate to the MoodDetails screen
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        MoodDetailsNavigationTest.clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for the MoodDetails screen to load
        SystemClock.sleep(2000);

        // Click the delete button
        onView(withId(R.id.btnDeleteMood)).perform(click());

        // Verify dialog appears
        onView(withId(R.id.delete_dialog_title)).check(matches(isDisplayed()));

        // Click the Delete button
        onView(withId(R.id.btn_delete)).perform(click());

        // Wait for deletion and navigation
        SystemClock.sleep(2000);

        // Verify we are back to the home screen (moodRecyclerView should be visible)
        onView(withId(R.id.moodRecyclerView)).check(matches(isDisplayed()));

        // Verify mood was deleted from database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    assertEquals(0, moodEvents.size());
                });
    }

    @Test
    public void testDeleteDialogAsFragment() {
        // This test creates and tests the DeleteDialogFragment directly

        // Create a fragment scenario with a moodEventId argument
        Bundle args = new Bundle();
        args.putString("moodEventId", "test-mood-id");

        // Make sure there's a mood with this ID in the database
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            MoodEvent moodEvent = new MoodEvent(
                    auth.getCurrentUser().getUid(),
                    Emotion.ANGER,
                    "Direct Test",
                    "Direct Test",
                    "Direct Test",
                    "",
                    0.0,
                    0.0
            );
            moodEvent.setId("test-mood-id");
            Tasks.await(FirebaseFirestore.getInstance()
                    .collection("moodEvents")
                    .document("test-mood-id")
                    .set(moodEvent));
        } catch (Exception e) {
            fail("Failed to set up test mood: " + e.getMessage());
        }

        SystemClock.sleep(1000);

        // Launch the delete dialog fragment directly in main activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            DeleteDialogFragment dialog = new DeleteDialogFragment();
            dialog.setArguments(args);
            dialog.show(activity.getSupportFragmentManager(), "test_dialog");
        });

        // Verify dialog components are displayed
        onView(withId(R.id.delete_dialog_title)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete)).check(matches(isDisplayed()));

        // Click the Delete button
        onView(withId(R.id.btn_delete)).perform(click());

        // Verify deletion in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    DocumentSnapshot document = Tasks.await(
                            db.collection("moodEvents").document("test-mood-id").get());
                    assertFalse(document.exists());
                });
    }
}