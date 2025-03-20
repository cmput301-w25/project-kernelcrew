package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MoodDetailsOwnershipTest extends FirebaseEmulatorMixin {

    // User 1 credentials
    private static final String USER1_USERNAME = "automatedtests1";
    private static final String USER1_EMAIL = "automatedtests1@kernelcrew.com";
    private static final String USER1_PASSWORD = "AT1@1234";

    // User 2 credentials
    private static final String USER2_USERNAME = "automatedtests2";
    private static final String USER2_EMAIL = "automatedtests2@kernelcrew.com";
    private static final String USER2_PASSWORD = "AT2@1234";

    // Mood details for test creation
    private static final String MOOD1_TRIGGER = "Morning Run";
    private static final String MOOD1_REASON = "Feeling Energized";
    private static final String MOOD2_TRIGGER = "Late Night Study";
    private static final String MOOD2_REASON = "Focused";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Helper method that deletes a test user (if exists) from Firebase Auth and Firestore.
     */
    private static void deleteTestUser(String email, String password, String username) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            // Attempt to sign in; if the user exists, this will succeed.
            Tasks.await(auth.signInWithEmailAndPassword(email, password));
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                System.out.println("Deleting FirebaseAuth user: " + user.getUid());
                Tasks.await(user.delete());
            }
        } catch (Exception e) {
            System.out.println("User sign in failed (likely does not exist): " + e.getMessage());
        } finally {
            auth.signOut();
        }
        // Delete Firestore documents
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            System.out.println("Deleting Firestore document in 'usernames' for username: " + username);
            Tasks.await(db.collection("usernames").document(username).delete());
        } catch (Exception ignored) { }
        try {
            List<DocumentSnapshot> docs = Tasks.await(
                    db.collection("users").whereEqualTo("email", email).get()
            ).getDocuments();
            for (DocumentSnapshot doc : docs) {
                System.out.println("Deleting Firestore user document: " + doc.getId());
                Tasks.await(db.collection("users").document(doc.getId()).delete());
            }
        } catch (Exception ignored) { }
    }

    /**
     * 1) Before all tests, delete any existing user data so sign-ups won't fail with "username already taken."
     * 2) We do NOT create users here; the tests sign up via the UI.
     */
    @BeforeClass
    public static void cleanUpExistingUsers() throws Exception {
        // Delete User 1
        System.out.println("Deleting test user 1 if exists...");
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        // Delete User 2
        System.out.println("Deleting test user 2 if exists...");
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
        // Wait a few seconds to ensure deletion propagates.
        SystemClock.sleep(5000);
    }

    /**
     * This runs before each test. We sign out the current user if any.
     */
    @Before
    public void setUp() throws InterruptedException, ExecutionException {
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(1000);
    }

    /**
     * Test 1: User 1 creates a mood and should see the Edit and Delete buttons.
     */
    @Test
    public void test01_EditVisibilityForOwnMood_User1() throws InterruptedException {
        // --- Sign Up as User 1 via GUI ---
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER1_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER1_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER1_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // --- Create a mood as User 1 ---
        onView(withId(R.id.page_createMoodEvent))
                .check(matches(isDisplayed()))
                .perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD1_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD1_REASON), closeSoftKeyboard());

        // Assume there's a toggle_happy to pick an emotion
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(2000);

        // --- Open the newly created mood details (position 0) ---
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(2000);

        // Verify Edit & Delete are visible
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));
    }

    /**
     * Test 2:
     * User 2 signs up, creates a mood, verifies that for their own mood
     * the Edit and Delete buttons are visible.
     */
    @Test
    public void test02_EditVisibilityForOwnMood_User2() throws InterruptedException {
        // --- Sign Up as User 2 ---
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER2_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER2_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER2_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(2000);

        // --- Create a mood as User 2 ---
        onView(withId(R.id.page_createMoodEvent))
                .check(matches(isDisplayed()))
                .perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD2_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD2_REASON), closeSoftKeyboard());

        // Suppose there's a toggle_anger for user 2
        onView(withId(R.id.toggle_anger)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(1000);

        // --- Open details for user 2's newly created mood (position 0) ---
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(1000);

        // Edit & Delete should be visible
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));
    }

    /**
     * Test 3:
     * Still as User 2, view a mood created by User 1 (assumed to be position 1),
     * verify that Edit & Delete are NOT visible.
     */
    @Test
    public void test03_NoEditVisibilityForOtherMood_User2() throws InterruptedException {
        // Already signed in as user 2 from test02 (since we haven't signed out).
        SystemClock.sleep(2000);

        // Now, the RecyclerView should have two moods: user1's at position 1
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(1, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(2000);

        // Verify Edit and Delete are GONE for user 1's mood
        onView(withId(R.id.btnEditMood))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnDeleteMood))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * After all tests, we delete user1 and user2 again so future runs don't fail with "username taken."
     */
    @AfterClass
    public static void cleanUpAfterAllTests() throws Exception {
        // Sign out any leftover user
        FirebaseAuth.getInstance().signOut();
        // Delete test users
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
    }

    // Helper method: Click a child view with a given ID inside a RecyclerView item.
    public static androidx.test.espresso.ViewAction clickChildViewWithId(final int id) {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public org.hamcrest.Matcher<View> getConstraints() {
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
}
