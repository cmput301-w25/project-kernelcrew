package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;

import android.os.SystemClock;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class OtherUserProfileMoodTest extends FirebaseEmulatorMixin {
    // Test user details for automatedtests1 (the viewer) and automatedtests2 (the profile owner)
    private static final String USER1_EMAIL = "auto1@test.com";
    private static final String USER1_USERNAME = "automatedtests1";
    private static final String USER1_PASSWORD = "TestPass1";

    private static final String USER2_EMAIL = "auto2@test.com";
    private static final String USER2_USERNAME = "automatedtests2";
    private static final String USER2_PASSWORD = "TestPass2";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Seed the database with two user profiles and several mood events for USER2.
     */
    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create or ensure USER1 exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER1_EMAIL, USER1_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }
        // Create or ensure USER2 exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER2_EMAIL, USER2_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }

        // Sign in as USER2 to seed USER2's data.
        Tasks.await(auth.signInWithEmailAndPassword(USER2_EMAIL, USER2_PASSWORD));
        String uid2 = auth.getCurrentUser().getUid();
        // Create a user document for USER2.
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("uid", uid2);
        user2Data.put("email", USER2_EMAIL);
        user2Data.put("username", USER2_USERNAME);
        Tasks.await(db.collection("users").document(uid2).set(user2Data, SetOptions.merge()));
        // Also seed a username mapping (if your app uses it)
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uid2);
        Tasks.await(db.collection("usernames").document(USER2_USERNAME).set(usernameData, SetOptions.merge()));

        // Seed several public MoodEvent documents for USER2.
        MoodEventProvider moodEventProvider = MoodEventProvider.getInstance();
        // Create three distinct mood events.
        for (int i = 1; i <= 3; i++) {
            MoodEvent mood = new MoodEvent(
                    uid2,
                    USER2_USERNAME,
                    Emotion.HAPPINESS,
                    "Social Situation " + i,
                    "Reason " + i,
                    34.052235 + i * 0.001,    // Slight variation in latitude
                    -118.243683 - i * 0.001    // Slight variation in longitude
            );
            // Set visibility to PUBLIC in your MoodEvent object (if not set by default)
            mood.setVisibility(MoodEventVisibility.PUBLIC);
            Tasks.await(moodEventProvider.insertMoodEvent(mood));
        }

        auth.signOut();
    }

    @Before
    public void setupAuth() throws ExecutionException, InterruptedException {
        // Sign in as USER1 (the viewer) for testing
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Tasks.await(auth.signInWithEmailAndPassword(USER1_EMAIL, USER1_PASSWORD));
    }

    @Test
    public void testOtherUserProfileDisplaysCorrectMoods() throws InterruptedException {
        // Navigate from HomeFeed to OtherUserProfile of USER2.
        // Assuming that in HomeFeed, mood items belonging to other users display a "View Details" button,
        // and clicking on the username navigates to that user's profile.

        // Wait for HomeFeed to load.
        SystemClock.sleep(2000);

        // In HomeFeed, simulate clicking on a mood item created by USER2.
        // For this test, we assume the RecyclerView is displaying a mood event from USER2.
        // Here we click the first item in the RecyclerView.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for MoodDetails to load.
        SystemClock.sleep(1500);

        // In the MoodDetails screen, assume there is a "View Profile" button (or clickable username)
        // that navigates to OtherUserProfile.
        onView(withId(R.id.tvUsernameDisplay))
                .perform(click());

        // Wait for OtherUserProfile screen to load.
        SystemClock.sleep(1500);

        // Verify that OtherUserProfile displays the correct username and email for USER2.
        onView(withId(R.id.username_text))
                .check(matches(withText(containsString(USER2_USERNAME))));
        onView(withId(R.id.email_text))
                .check(matches(withText(USER2_EMAIL)));

        // Now, check that the RecyclerView for public moods displays at least 2 items.
        onView(withId(R.id.public_moods_recycler_view))
                .check((view, noViewFoundException) -> {
                    assertNotNull("RecyclerView is null", view);
                    RecyclerView recyclerView = (RecyclerView) view;
                    int itemCount = recyclerView.getAdapter().getItemCount();
                    if (itemCount < 2) {
                        throw new AssertionError("Expected at least 2 mood items, found " + itemCount);
                    }
                });

        // Finally, verify that at least one of the mood items contains a text element indicating it belongs to USER2.
        // Here we assume that each mood item layout contains a TextView with id "moodItemUsername" that shows the creator's username.
        onView(withId(R.id.public_moods_recycler_view))
                .perform(actionOnItemAtPosition(0, checkItemHasDescendantWithText(USER2_USERNAME)));
        onView(withId(R.id.public_moods_recycler_view))
                .perform(actionOnItemAtPosition(1, checkItemHasDescendantWithText(USER2_USERNAME)));
    }

    // Helper ViewAction to check that a RecyclerView item has a descendant with given text.
    public static ViewAction checkItemHasDescendantWithText(String expectedText) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return withId(android.R.id.content);
            }
            @Override
            public String getDescription() {
                return "Check that the item has a descendant with text: " + expectedText;
            }
            @Override
            public void perform(androidx.test.espresso.UiController uiController, View view) {
                // Simply assert that one of the child views has the expected text.
                // This uses Espresso's built-in matching.
                onView(withText(expectedText)).check(matches(isDisplayed()));
            }
        };
    }

    // Custom ViewAction to click a child view with a given id.
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }
            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }
            @Override
            public void perform(androidx.test.espresso.UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null && childView.isClickable()) {
                    childView.performClick();
                }
            }
        };
    }

    @After
    public void signOutTheUser() throws ExecutionException, InterruptedException {
        FirebaseAuth.getInstance().signOut();
    }
}
