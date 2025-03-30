package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
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
        // Seed your test data using your staticCreateUser() method.
        staticCreateUser();
    }

    @Before
    public void signInUser() throws Exception {
        // Wait for HomeFeed to load; assumes user is already signed in via seed data.
        SystemClock.sleep(2000);
    }

    /**
     * Helper method that checks if a view with the given id is displayed.
     */
    private boolean isViewDisplayed(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Test 1: Home Feed Reason Search Test
     * - If available, creates a mood event via UI with reason "Team lunch meeting".
     * - Then searches for "lunch" using the filter search field and taps the "Mood Reason" button.
     * - Verifies that the HomeFeed RecyclerView displays an item whose text contains "lunch".
     */
    @Test
    public void testHomeFeedSearchReason() {
        // Create a mood event if the creation view is available.
        if (isViewDisplayed(R.id.page_createMoodEvent)) {
            onView(withId(R.id.page_createMoodEvent)).perform(click());
            SystemClock.sleep(2000);
            String reasonText = "Team lunch meeting";
            onView(withId(R.id.emotion_reason))
                    .perform(replaceText(reasonText));
            onView(withId(R.id.emotion_reason))
                    .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
            onView(withId(R.id.toggle_happy)).perform(click());
            onView(withId(R.id.submit_button)).perform(click());
            SystemClock.sleep(3000);
        }
        // Now search by reason.
        String searchTerm = "lunch";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(searchTerm));
        onView(withId(R.id.filterSearchEditText))
                .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);
        // Verify that the HomeFeed RecyclerView displays an item containing the search term.
        onView(withId(R.id.moodRecyclerView))
                .check(matches(hasDescendant(withText(containsString(searchTerm)))));
    }

    /**
     * Test 2: Home Feed User Search & Navigation Test
     * - Enters a user query (e.g. "Bob") in the search field, taps the "Users" button,
     *   clicks on the first user result, and verifies that the OtherUserProfile screen displays
     *   the expected data (username containing "Bob").
     */
    @Test
    public void testHomeFeedSearchUserNavigation() {
        String userQuery = "Bob";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(userQuery));
        onView(withId(R.id.filterSearchEditText))
                .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(2000);
        // Verify that user items appear in the RecyclerView.
        onView(withId(R.id.moodRecyclerView))
                .check(matches(hasDescendant(withId(R.id.usernameTextView))));
        // Click on the first user item.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.usernameTextView)));
        SystemClock.sleep(2000);
        // Verify that OtherUserProfile screen is displayed with expected username containing "Bob".
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.username_text))
                .check(matches(withText(containsString("Bob"))));
    }

    /**
     * Test 3: Mood History Reason Search Test
     * - If available, creates a mood event via UI with reason "Project meeting update".
     * - Navigates to the Mood History page, enters "meeting" as a search term,
     *   taps the "Mood Reason" button, and verifies that the Mood History RecyclerView
     *   displays an item whose text contains "meeting".
     */
    @Test
    public void testMoodHistorySearchFeature() {
        // Create a mood event if the creation view is available.
        if (isViewDisplayed(R.id.page_createMoodEvent)) {
            onView(withId(R.id.page_createMoodEvent)).perform(click());
            SystemClock.sleep(2000);
            String historyReason = "Project meeting update";
            onView(withId(R.id.emotion_reason))
                    .perform(replaceText(historyReason));
            onView(withId(R.id.emotion_reason))
                    .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
            onView(withId(R.id.toggle_sadness)).perform(click());
            onView(withId(R.id.submit_button)).perform(click());
            SystemClock.sleep(3000);
        }
        // Navigate to Mood History page.
        onView(withId(R.id.bottom_navigation)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));

        // Enter a search term.
        String historySearchTerm = "meeting";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(historySearchTerm));
        onView(withId(R.id.filterSearchEditText))
                .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);
        // Verify that the Mood History RecyclerView displays an item containing the search term.
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
