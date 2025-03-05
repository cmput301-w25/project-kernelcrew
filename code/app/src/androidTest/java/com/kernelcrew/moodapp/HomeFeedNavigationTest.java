package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import android.os.SystemClock;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.Mood;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void seedDatabase() {
        // This ensures that when HomeFeed loads, it has at least one mood document.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("mood");

        // Create a test mood using the correct constructor parameters:
        // id, userName, moodText, timestamp.
        Mood testMood = new Mood("testMoodId", "dummyUser", "Test mood", System.currentTimeMillis());
        moodsRef.document("testMoodId").set(testMood);

        // Wait for the data to be written
        SystemClock.sleep(2000);
    }

    @Test
    public void testNavigationToHomeFeed() {
        // On AuthHome screen: Click the "Sign In" button.
        // Adjust the matcher below if your AuthHome layout uses a different text or id.
        onView(withText("Sign In"))
                .perform(click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        onView(withId(R.id.email))
                .check(matches(isDisplayed()));

        // Fill in the email and password fields.
        onView(withId(R.id.email))
                .perform(replaceText("test@kernelcrew.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText("Password@1234"), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        onView(withId(R.id.signInButton))
                .perform(click());

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        onView(withId(R.id.homeTextView))
                .check(matches(isDisplayed()));
    }
}