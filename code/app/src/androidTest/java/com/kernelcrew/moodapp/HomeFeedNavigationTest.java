package com.kernelcrew.moodapp;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.assertion.ViewAssertions;

import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testNavigationToHomeFeed() {
        // On AuthHome screen: Click the "Sign In" button.
        // Adjust the matcher below if your AuthHome layout uses a different text or id.
        Espresso.onView(ViewMatchers.withText("Sign In"))
                .perform(ViewActions.click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        Espresso.onView(ViewMatchers.withId(R.id.email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Fill in the email and password fields.
        Espresso.onView(ViewMatchers.withId(R.id.email))
                .perform(ViewActions.replaceText("test@kernelcrew.com"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.password))
                .perform(ViewActions.replaceText("Password@1234"), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        Espresso.onView(ViewMatchers.withId(R.id.signInButton))
                .perform(ViewActions.click());

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        Espresso.onView(ViewMatchers.withId(R.id.homeTextView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
