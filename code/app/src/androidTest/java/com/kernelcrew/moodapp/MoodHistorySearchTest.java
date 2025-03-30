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
public class MoodHistorySearchTest extends FirebaseEmulatorMixin {

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
     * Test 3: Mood History Reason Search Test
     * - If available, creates a mood event via UI with reason "Project meeting update".
     * - Navigates to the Mood History page, enters "meeting" as a search term,
     *   taps the "Mood Reason" button, and verifies that the Mood History RecyclerView
     *   displays an item whose text contains "meeting".
     */
    @Test
    public void testMoodHistorySearchFeature() {
        // Create mood event
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);
        String historyReason = "Project meeting update";
        onView(withId(R.id.emotion_reason))
                .perform(replaceText(historyReason));
        onView(withId(R.id.emotion_reason))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.toggle_sadness)).perform(click());
        onView(withId(R.id.submit_button)).perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // Navigate to Mood History page.
        onView(withId(R.id.page_myHistory)).perform(click());
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Enter a search term.
        String historySearchTerm = "meeting";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(historySearchTerm));
        onView(withId(R.id.filterSearchEditText))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());

        // Click the first item in the Mood History RecyclerView.
        onView(withId(R.id.recyclerViewMoodHistory))
                .perform(actionOnItemAtPosition(0, click()));

        // Wait for Mood Details screen to load.
        SystemClock.sleep(3000);

        // Verify that the Mood Details screen's reason TextView contains the search term.
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText(containsString(historySearchTerm))));

    }
}
