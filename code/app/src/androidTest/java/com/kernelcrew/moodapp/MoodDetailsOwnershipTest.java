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
     * Helper method to delete a test user if exists.
     */
    private static void deleteTestUser(String email, String password, String username) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            Tasks.await(auth.signInWithEmailAndPassword(email, password));
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                Tasks.await(user.delete());
            }
        } catch (Exception e) {
            // User likely doesn't exist
        } finally {
            auth.signOut();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            Tasks.await(db.collection("usernames").document(username).delete());
        } catch (Exception ignored) { }
        try {
            List<DocumentSnapshot> docs = Tasks.await(
                    db.collection("users").whereEqualTo("email", email).get()
            ).getDocuments();
            for (DocumentSnapshot doc : docs) {
                Tasks.await(db.collection("users").document(doc.getId()).delete());
            }
        } catch (Exception ignored) { }
    }

    @BeforeClass
    public static void cleanUpExistingUsers() throws Exception {
        System.out.println("Deleting test user 1 if exists...");
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        System.out.println("Deleting test user 2 if exists...");
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
        // Allow deletion to propagate.
        SystemClock.sleep(5000);
    }

    /**
     * Ensure each test starts fresh: sign out and recreate the activity.
     */
    @Before
    public void setUp() throws InterruptedException {
        // Sign out any existing user.
        FirebaseAuth.getInstance().signOut();
        // Force a full activity restart so that the authentication UI is shown.
        activityScenarioRule.getScenario().recreate();
        // Wait for the UI to update.
        SystemClock.sleep(2000);
    }

    /**
     * Test 1: User 1 creates a mood and should see the Edit and Delete buttons.
     */
    @Test
    public void test01_EditVisibilityForOwnMood_User1() throws InterruptedException {
        // Sign up as User 1 via the UI.
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER1_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER1_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER1_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Create a mood as User 1.
        onView(withId(R.id.page_createMoodEvent))
                .check(matches(isDisplayed()))
                .perform(click());
        SystemClock.sleep(3000);
        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD1_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD1_REASON), closeSoftKeyboard());
        // Select an emotion toggle (e.g., toggle_happy).
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // Open the newly created mood's details (assumed at position 0).
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify that Edit and Delete buttons are visible.
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: User 2 signs up, creates a mood, and verifies that for their own mood,
     * the Edit and Delete buttons are visible.
     */
    @Test
    public void test02_EditVisibilityForOwnMood_User2() throws InterruptedException {
        // Sign up as User 2.
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER2_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER2_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER2_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Create a mood as User 2.
        onView(withId(R.id.page_createMoodEvent))
                .check(matches(isDisplayed()))
                .perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD2_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD2_REASON), closeSoftKeyboard());
        // Use an emotion toggle; here, assume "toggle_anger" exists.
        onView(withId(R.id.toggle_anger)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // Open details for User 2's mood (position 0).
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify that Edit and Delete buttons are visible.
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));

        // Sign out User 2.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(2000);
    }

    /**
     * Test 3: Sign in as User 2 and view a mood created by User 1 to verify that
     * the Edit and Delete buttons are NOT visible.
     */
    @Test
    public void test03_NoEditVisibilityForOtherMood_User2() throws InterruptedException {
        // Sign in as User 2.
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER2_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER2_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Open User 1's mood (assumed to be at position 1 in the RecyclerView).
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(1, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify that Edit and Delete buttons are not visible.
        onView(withId(R.id.btnEditMood))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnDeleteMood))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // Sign out User 2.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(2000);
    }

    /**
     * Helper method: Click a child view with a given ID inside a RecyclerView item.
     */
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

    @AfterClass
    public static void cleanUpAfterAllTests() throws Exception {
        FirebaseAuth.getInstance().signOut();
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
    }
}
