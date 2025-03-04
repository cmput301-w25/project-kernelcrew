package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MoodDetailsNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testViewDetailsNavigationAndData() throws InterruptedException {
        // Wait for HomeFeed to load data.
        Thread.sleep(3000);

        // Click the "View Details" button of the first item in the RecyclerView.
        onView(withId(R.id.moodRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait for the MoodDetails screen to load.
        Thread.sleep(3000);

        // Verify that key elements on MoodDetails screen are displayed.
        onView(withId(R.id.tvMoodState)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTriggerValue)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSocialSituationValue)).check(matches(isDisplayed()));
        onView(withId(R.id.tvReasonValue)).check(matches(isDisplayed()));
        onView(withId(R.id.ivMoodPhoto)).check(matches(isDisplayed()));
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // Verify that each field matches the seeded test data.
        onView(withId(R.id.tvMoodState)).check(matches(withText("Happy")));
        onView(withId(R.id.tvTriggerValue)).check(matches(withText("Morning Coffee")));
        onView(withId(R.id.tvSocialSituationValue)).check(matches(withText("With Friends")));
        onView(withId(R.id.tvReasonValue)).check(matches(withText("Celebration")));
    }
}
