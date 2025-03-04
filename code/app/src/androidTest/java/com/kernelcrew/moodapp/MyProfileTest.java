package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyProfileTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testProfilePageLoadsCorrectly() {
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signOutButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSignOutButtonNavigatesToAuthHome() {
        onView(withId(R.id.signOutButton)).perform(click());
        onView(withId(R.id.signInButton)).check(matches(isDisplayed()));
    }


    @Test
    public void testUserDataDisplay() {
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));

        SystemClock.sleep(2000);
        onView(withId(R.id.followers_button)).check(matches(isDisplayed()));
        onView(withId(R.id.following_button)).check(matches(isDisplayed()));
    }
}
