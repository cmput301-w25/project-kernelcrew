package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.Mood;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest extends FirebaseEmulatorMixin {
    private static final String USER_EMAIL = "test@kernelcrew.com";
    private static final String USER_PASSWORD = "Password@1234";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws InterruptedException, ExecutionException {
        // Seed the Firestore "moods" collection with a sample document.
        // This ensures that when HomeFeed loads, it has at least one mood document.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        CollectionReference moodsRef = db.collection("mood");

        Tasks.await(auth.createUserWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));

        // Create a test mood using the correct constructor parameters:
        // id, userName, moodText, timestamp.
        Mood testMood = new Mood("testMoodId", "dummyUser", "Test mood", System.currentTimeMillis());
        Tasks.await(moodsRef.document("testMoodId").set(testMood));

        auth.signOut();
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
                .perform(replaceText(USER_EMAIL), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText(USER_PASSWORD), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        onView(withId(R.id.signInButton))
                .perform(click());

        SystemClock.sleep(1000);

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        onView(withId(R.id.homeTextView))
                .check(matches(isDisplayed()));
    }

    @After
    public void signOutTheUser() {
        // Not necessary right now, but will be if we add more tests
        FirebaseAuth.getInstance().signOut();
    }
}