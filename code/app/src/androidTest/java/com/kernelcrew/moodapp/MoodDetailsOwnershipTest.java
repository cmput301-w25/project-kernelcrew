package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@LargeTest
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

    @BeforeClass
    public static void seedDatabaseForTests() throws ExecutionException, InterruptedException {
        // Optionally, seed initial test data here if needed.
    }

    @Before
    public void setUpAuth() throws InterruptedException {
        // Renamed from setup() to avoid conflict with a static method.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(1000);
    }

    /**
     * Test 1: User 1 creates a mood and should see the Edit and Delete buttons.
     */
    @Test
    public void testEditVisibilityForOwnMood_User1() throws InterruptedException {
        // --- Sign Up and Sign In as User 1 ---
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
        // Select an emotion toggle (e.g., toggle_happy)
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

        // --- Click Edit and verify navigation to the Edit Mood screen ---
        onView(withId(R.id.btnEditMood)).perform(click());
        SystemClock.sleep(3000);
        onView(withText("Edit Mood")).check(matches(isDisplayed()));
    }

    /**
     * Test 2: User 2 creates a mood and then:
     * - For User 2's own mood, Edit/Delete buttons are visible.
     * - When viewing User 1's mood, these buttons are not visible.
     */
    @Test
    public void testVisibilityForOwnAndOtherMoods_User2() throws InterruptedException, ExecutionException {
        // --- Sign Up and Sign In as User 2 ---
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER2_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER2_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER2_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // --- Create a mood as User 2 (with distinct data) ---
        onView(withId(R.id.page_createMoodEvent))
                .check(matches(isDisplayed()))
                .perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD2_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD2_REASON), closeSoftKeyboard());
        // Use a different emotion toggle; here, "toggle_anger" instead of "toggle_sad"
        onView(withId(R.id.toggle_anger)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // --- For User 2's own mood: open details (assumed at position 0) ---
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));
        // Click Edit and verify navigation.
        onView(withId(R.id.btnEditMood)).perform(click());
        SystemClock.sleep(3000);
        onView(withText("Edit Mood")).check(matches(isDisplayed()));

        // --- Now, view User 1's mood ---
        // Assuming both moods are listed and User 1's mood is at position 1.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(1, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);
        // For a mood not owned by User 2, Edit and Delete buttons should not be visible.
        onView(withId(R.id.btnEditMood)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.btnDeleteMood)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @After
    public void tearDown() throws Exception {
        // Cleanup: Delete test moods if necessary and sign out.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add deletion logic here if your test framework supports it.
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
