package com.kernelcrew.moodapp;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.util.Log;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.kernelcrew.moodapp.ui.HomeFeed;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.MoodHistory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryTests {

    @Test
    public void moodEventsListIsDisplayed() {
        // Launch the MoodHistory fragment
        FragmentScenario<MoodHistory> scenario = FragmentScenario.launchInContainer(MoodHistory.class);

        // Check if the ListView is displayed
        onView(withId(R.id.listViewMoodHistory))
                .check(matches(isDisplayed()));

        // Check if first and second hard-coded items are displayed with correct values
        onView(withText("Feb 16, 2025"))
                .check(matches(isDisplayed()));

        onView(withText("Mood Event 15"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void pressingBackButtonShouldNavigateToHomeFeed() throws InterruptedException {
        FragmentScenario<MoodHistory> scenario = launchInContainer(MoodHistory.class, null, R.style.Theme_MoodApp);

        NavController mockNavController = mock(NavController.class);

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        Thread.sleep(500);

        onView(withId(R.id.topAppBar)).perform(click());
        verify(mockNavController).navigate(R.id.homeFeed);
    }

}
