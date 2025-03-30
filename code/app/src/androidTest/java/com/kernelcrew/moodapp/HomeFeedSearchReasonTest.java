package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
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

import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeFeedSearchReasonTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        // Seed your test data using your staticCreateUser() method.
        staticCreateUser();
    }

    @Before
    public void signInUser() {
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
            SystemClock.sleep(10000);
            onView(withId(R.id.emotion_reason))
                    .perform(replaceText(reasonText));
            onView(withId(R.id.emotion_reason))
                    .perform(closeSoftKeyboard());
            onView(withId(R.id.toggle_happy)).perform(click());
            onView(withId(R.id.submit_button))
                    .perform(scrollTo(), click());
            SystemClock.sleep(3000);
        }
        // Now search by reason.
        String searchTerm = "lunch";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(searchTerm), closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);
        // Click the first mood item’s "View Details" button (adjust the child view id if needed)
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(5000);
        // Verify that the Mood Details screen displays a reason containing the search term.
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText(containsString(searchTerm))));
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
