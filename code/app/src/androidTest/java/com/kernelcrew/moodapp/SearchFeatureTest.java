package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchFeatureTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        // Seed your test data.
        staticCreateUser();
        // Ensure that dummy user "Bob" exists (seed your database accordingly).
    }

    @Before
    public void signInUser() throws Exception {
        // Simulate signing in by clicking on "Sign In", filling in credentials, and tapping the sign-in button.
        // Adjust IDs and texts as needed based on your SignInAndSignUpTest.
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText("dummy1@test.com"));
        onView(withId(R.id.passwordSignIn)).perform(replaceText("password1"));
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);
    }

    /**
     * Test 1: Home Feed Reason Search Test
     * - Create a mood event with reason "Team lunch meeting".
     * - On Home Feed, enter "lunch" in the search field and tap the "Mood Reason" button.
     * - Verify that the created mood event is visible.
     */
    @Test
    public void testHomeFeedSearchReason() {
        // Create a mood event via UI.
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);
        String reasonText = "Team lunch meeting";
        onView(withId(R.id.emotion_reason)).perform(replaceText(reasonText));
        onView(withId(R.id.emotion_reason)).perform(closeSoftKeyboard());
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.submit_button)).perform(click());
        SystemClock.sleep(3000); // Wait for mood event to appear in Home Feed

        // Go back to Home Feed if needed (depends on your navigation).
        // Now, search by reason.
        String searchTerm = "lunch";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(searchTerm));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);

        // Verify that the Home Feed RecyclerView displays an item whose text contains "lunch".
        // (Assumes that the reason is visible in the item view.)
        onView(withId(R.id.moodRecyclerView))
                .check(matches(hasDescendant(withText(containsString(searchTerm)))));
    }

    /**
     * Test 2: Home Feed User Search & Navigation Test
     * - Switch to user search by entering "Bob" and tapping the "Users" button.
     * - Click on the first user item and verify that OtherUserProfile displays expected info.
     */
    @Test
    public void testHomeFeedSearchUserNavigation() {
        String userQuery = "Bob";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(userQuery));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(2000);
        // Verify that user items are shown in the RecyclerView.
        onView(withId(R.id.moodRecyclerView)).check(matches(hasDescendant(withId(R.id.usernameTextView))));
        // Click on the first user item.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.usernameTextView)));
        SystemClock.sleep(2000);
        // Verify that OtherUserProfile screen is displayed with username containing "Bob".
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.username_text)).check(matches(withText(containsString("Bob"))));
    }

    /**
     * Test 3: Mood History Reason Search Test
     * - Create a mood event with reason "Project meeting update".
     * - Navigate to Mood History page.
     * - Enter "meeting" in the search field and tap the "Mood Reason" button.
     * - Verify that the mood history list displays the mood event.
     */
    @Test
    public void testMoodHistorySearchFeature() {
        // Create a mood event via UI.
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);
        String historyReason = "Project meeting update";
        onView(withId(R.id.emotion_reason)).perform(replaceText(historyReason));
        onView(withId(R.id.emotion_reason)).perform(closeSoftKeyboard());
        onView(withId(R.id.toggle_sadness)).perform(click());
        onView(withId(R.id.submit_button)).perform(click());
        SystemClock.sleep(3000);

        // Navigate to Mood History page via bottom navigation.
        onView(withId(R.id.bottom_navigation)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));

        // Enter a search term (partial string from the reason).
        String historySearchTerm = "meeting";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(historySearchTerm));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);

        // Verify that the Mood History RecyclerView displays an item with the reason containing "meeting".
        onView(withId(R.id.recyclerViewMoodHistory))
                .check(matches(hasDescendant(withText(containsString(historySearchTerm)))));
    }

    // Helper ViewAction: clicks a child view with the specified id within a RecyclerView item.
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
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null && childView.isClickable()) {
                    childView.performClick();
                }
            }
        };
    }
}
