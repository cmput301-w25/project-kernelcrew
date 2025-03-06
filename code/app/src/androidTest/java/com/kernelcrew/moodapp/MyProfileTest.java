package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MyProfileTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testProfilePageLoadsAndSignOutWorks() {
        // --- Sign In Flow ---
        onView(withText("Sign In")).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.email))
                .perform(replaceText("test@kernelcrew.com"), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText("Password@1234"), closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
        SystemClock.sleep(3000);

        // --- Verify HomeFeed is loaded ---
        onView(withId(R.id.homeTextView)).check(matches(isDisplayed()));

        // --- Navigate to Profile via Bottom Navigation ---
        onView(withId(R.id.page_myProfile)).perform(click());
        SystemClock.sleep(2000);

        // --- Verify Profile UI Elements ---
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signOutButton)).check(matches(isDisplayed()));
        onView(withId(R.id.followers_button)).check(matches(isDisplayed()));
        onView(withId(R.id.following_button)).check(matches(isDisplayed()));

        // --- Perform Sign Out ---
        // Directly click signOutButton (removing scrollTo())
        onView(withId(R.id.signOutButton)).perform(click());
        SystemClock.sleep(3000);

        // --- Verify that we are back on AuthHome ---
        onView(withId(R.id.signInButton)).check(matches(isDisplayed()));
    }
}
