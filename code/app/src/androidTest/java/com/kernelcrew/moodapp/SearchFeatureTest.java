package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;  // Import from ViewActions
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
     * Test 1: Home Feed Reason Search
     * Enters a search term in the FilterBar, selects "Mood Reason" (filter type MOODS),
     * and verifies that the HomeFeed RecyclerView is updated.
     */
    @Test
    public void testHomeFeedSearchReason() {
        String searchTerm = "lunch";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(searchTerm));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);
        // Verify that the RecyclerView is displayed.
        onView(withId(R.id.moodRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: User Search Navigation
     * Enters a user query (e.g. "Bob") in the search field, taps the "Users" button,
     * clicks on the first user result, and verifies that the OtherUserProfile screen displays
     * the expected data.
     */
    @Test
    public void testHomeFeedSearchUserNavigation() {
        String userQuery = "Bob";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(userQuery));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(2000);
        // Verify that user items appear in the RecyclerView (by checking for the username view).
        onView(withId(R.id.moodRecyclerView)).check(matches(hasDescendant(withId(R.id.usernameTextView))));
        // Click on the first user item.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.usernameTextView)));
        SystemClock.sleep(2000);
        // Verify that OtherUserProfile is displayed with expected username containing "Bob".
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.username_text))
                .check(matches(withText(containsString("Bob"))));
    }

    /**
     * Test 3: Mood History Reason Search
     * Navigates to the Mood History page, enters a reason search term, selects "Mood Reason",
     * and verifies that the Mood History RecyclerView displays items whose reason text
     * contains the search term.
     */
    @Test
    public void testMoodHistorySearchFeature() {
        // Navigate to Mood History.
        onView(withId(R.id.bottom_navigation)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));
        String historySearchTerm = "meeting";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(historySearchTerm));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);
        // Verify that at least one item in the Mood History list displays a reason containing the search term.
        onView(withId(R.id.recyclerViewMoodHistory))
                .check(matches(hasDescendant(withText(containsString(historySearchTerm.toLowerCase())))));
    }

    // Helper ViewAction: clicks a child view with the specified id.
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
