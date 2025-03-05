package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.widget.ImageButton;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This class tests if the user can go to the Sign In/Up Screens from the Initial Screen
 * */
@RunWith(AndroidJUnit4.class)
public class AuthHomeNavigationTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void goFromInitialToSignInToInitial() {
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());

        onView(withText(R.string.welcome_back)).check(matches(isDisplayed()));

        onView(allOf(isDescendantOfA(withId(R.id.topAppBar)), isAssignableFrom(ImageButton.class)))
                .perform(click());

        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

    @Test
    public void goFromInitialToSignUpToInitial() {
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());

        onView(withText(R.string.create_an_account)).check(matches(isDisplayed()));

        onView(allOf(isDescendantOfA(withId(R.id.topAppBar)), isAssignableFrom(ImageButton.class)))
                .perform(click());

        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }
}
