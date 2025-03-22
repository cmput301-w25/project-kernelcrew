package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
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
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.DeleteDialogFragment;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

// The following code is from Anthropic, Claude, "Create tests for
// DeleteDialogFragment that verify its functionality", 2025-03-10
@RunWith(AndroidJUnit4.class)
public class DeleteDialogFragmentUITests extends FirebaseEmulatorMixin {

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
                "Test Social Situation",
                "Test Reason",
                0.0,
                0.0
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(moodEvent));
    }

    // Create a test listener implementation
    private final DeleteDialogFragment.DeleteDialogListener testListener =
            new DeleteDialogFragment.DeleteDialogListener() {
                @Override
                public void onDeleteConfirmed() {
                    // Empty implementation for testing
                }
            };

    @Test
    public void testDeleteDialogWithDeleteButton() {
        // Create a unique test mood ID so tests don't interfere with each other
        final String TEST_MOOD_ID = "test-delete-" + System.currentTimeMillis();
        createTestMood(TEST_MOOD_ID);

        // Show the dialog on the UI thread
        showDeleteDialog(TEST_MOOD_ID);

        // Give the dialog time to appear
        SystemClock.sleep(1000);

        // Click the delete button (use text matcher instead of ID to avoid issues)
        try {
            onView(withText("Yes, Delete It")).perform(click());
        } catch (Exception e) {
            // If the view isn't found, fail with useful information
            fail("Could not find the 'Yes, Delete It' button. Error: " + e.getMessage());
        }

        // Check that the document was deleted from Firestore
        verifyMoodWasDeleted(TEST_MOOD_ID);
    }

    @Test
    public void testDeleteDialogWithKeepButton() {
        // Create a unique test mood ID so tests don't interfere with each other
        final String TEST_MOOD_ID = "test-keep-" + System.currentTimeMillis();
        createTestMood(TEST_MOOD_ID);

        // Show the dialog on the UI thread
        showDeleteDialog(TEST_MOOD_ID);

        // Give the dialog time to appear
        SystemClock.sleep(1000);

        // Click the keep button (use text matcher instead of ID to avoid issues)
        try {
            onView(withText("No, Keep It")).perform(click());
        } catch (Exception e) {
            // If the view isn't found, fail with useful information
            fail("Could not find the 'No, Keep It' button. Error: " + e.getMessage());
        }

        // Check that the document still exists in Firestore
        verifyMoodWasKept(TEST_MOOD_ID);
    }

    // Helper method to create a test mood in the database
    private void createTestMood(String moodId) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            MoodEvent moodEvent = new MoodEvent(
                    auth.getCurrentUser().getUid(),
                    Emotion.ANGER,
                    "Test Social Situation",
                    "Test Reason",
                    0.0,
                    0.0
            );
            moodEvent.setId(moodId);
            Tasks.await(FirebaseFirestore.getInstance()
                    .collection("moodEvents")
                    .document(moodId)
                    .set(moodEvent));

            // Wait for document to be confirmed in database
            SystemClock.sleep(500);
        } catch (Exception e) {
            fail("Failed to create test mood: " + e.getMessage());
        }
    }

    // Helper method to show the delete dialog
    private void showDeleteDialog(String moodId) {
        activityScenarioRule.getScenario().onActivity(activity -> {
            DeleteDialogFragment dialogFragment = new DeleteDialogFragment();

            // Set the moodEventId in arguments
            Bundle args = new Bundle();
            args.putString("moodEventId", moodId);
            dialogFragment.setArguments(args);

            // Set a listener to prevent ClassCastException
            dialogFragment.setDeleteDialogListener(testListener);

            // Show the dialog using the fragment manager
            dialogFragment.show(activity.getSupportFragmentManager(), "test_dialog");
        });
    }

    // Helper method to verify a mood was deleted
    private void verifyMoodWasDeleted(String moodId) {
        try {
            // Give time for deletion to complete
            SystemClock.sleep(1000);

            DocumentSnapshot document = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("moodEvents")
                            .document(moodId)
                            .get());

            assertFalse("Document should be deleted", document.exists());
        } catch (Exception e) {
            fail("Error checking if document was deleted: " + e.getMessage());
        }
    }

    // Helper method to verify a mood was not deleted
    private void verifyMoodWasKept(String moodId) {
        try {
            // Give time for potential deletion to complete
            SystemClock.sleep(1000);

            DocumentSnapshot document = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("moodEvents")
                            .document(moodId)
                            .get());

            assertTrue("Document should still exist", document.exists());
        } catch (Exception e) {
            fail("Error checking if document exists: " + e.getMessage());
        }
    }
}