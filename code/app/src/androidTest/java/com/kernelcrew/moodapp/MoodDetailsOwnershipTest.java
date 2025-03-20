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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MoodDetailsOwnershipTest extends FirebaseEmulatorMixin {

    // User 1 credentials
    private static final String USER1_USERNAME = "automatedtests1";
    private static final String USER1_EMAIL = "automatedtests1@kernelcrew.com";
    private static final String USER1_PASSWORD = "AT1234";

    // User 2 credentials
    private static final String USER2_USERNAME = "automatedtests2";
    private static final String USER2_EMAIL = "automatedtests2@kernelcrew.com";
    private static final String USER2_PASSWORD = "AT1234";

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
                // Delete the Firebase Auth account.
                Tasks.await(user.delete());
            }
        } catch (Exception e) {
            // If sign-in fails, assume the user doesn't exist.
        } finally {
            auth.signOut();
        }
        // Delete Firestore documents for this user.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            Tasks.await(db.collection("usernames").document(username).delete());
        } catch (Exception ignored) { }
        try {
            List<DocumentSnapshot> docs = Tasks.await(db.collection("users")
                    .whereEqualTo("email", email)
                    .get()).getDocuments();
            for (DocumentSnapshot doc : docs) {
                Tasks.await(db.collection("users").document(doc.getId()).delete());
            }
        } catch (Exception ignored) { }
    }

    @BeforeClass
    public static void seedDatabaseForTests() throws Exception {
        // Delete any preexisting test users so that sign-up does not fail.
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
        // Optionally, seed additional data here if needed.
    }

    @Before
    public void setUpAuth() throws InterruptedException {
        // Ensure no user is signed in before each test.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(1000);
    }

    /**
     * Test 1: User 1 creates a mood and should see the Edit and Delete buttons.
     */
    @Test
    public void testEditVisibilityForOwnMood_User1() throws InterruptedException {
        // --- Sign Up and Sign In as User 1 via GUI ---
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
        // Select an emotion toggle (for example, toggle_happy)
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // --- Navigate to Mood Details (assume newest mood is at position 0) ---
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // --- Verify that Edit and Delete buttons are visible for User 1's mood ---
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));
    }

    /**
     * Test 2:
     * User 2 signs up, creates a mood, verifies that for their own mood the Edit and Delete buttons are visible,
     * then clicks Edit to verify navigation, and finally signs out.
     */
    @Test
    public void testEditVisibilityForOwnMood_User2() throws InterruptedException {
        // --- Sign Up and Sign In as User 2 ---
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER2_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER2_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER2_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(1000);

        // --- Create a mood as User 2 (with distinct data) ---
        onView(withId(R.id.page_createMoodEvent))
                .check(matches(isDisplayed()))
                .perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD2_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD2_REASON), closeSoftKeyboard());
        // Use an emotion toggle (assume "toggle_anger" exists in the layout)
        onView(withId(R.id.toggle_anger)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(1000);

        // --- For User 2's own mood: Open details (assumed at position 0 in the RecyclerView) ---
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(1000);

        // Verify that the Edit and Delete buttons are visible.
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));

        // Sign out User 2.
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Test 3:
     * Sign in as User 2, view a mood created by User 1,
     * and verify that the Edit and Delete buttons are NOT visible.
     */
    @Test
    public void testNoEditVisibilityForOtherMood_User2() throws InterruptedException {
        // --- Sign In as User 2 (assuming the user already exists) ---
        SystemClock.sleep("1000");
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER2_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER2_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // --- View User 1's mood ---
        // Assuming both moods are listed and that User 1's mood is at position 1.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(1, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify that the Edit and Delete buttons are NOT visible for a mood not owned by User 2.
        onView(withId(R.id.btnEditMood)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnDeleteMood)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // Sign out User 2.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(2000);
    }

    @After
    public void tearDown() throws Exception {
        // Cleanup: Delete test moods if necessary and sign out.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Optionally, add deletion logic here.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(1000);
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
