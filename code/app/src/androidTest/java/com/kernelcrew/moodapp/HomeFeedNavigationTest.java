package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import static org.awaitility.Awaitility.await;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.Espresso;
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
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class HomeFeedNavigationTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws InterruptedException, ExecutionException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseEmulatorMixin.staticCreateUser();
        FirebaseEmulatorMixin.clearUser();
//        CollectionReference moodsRef = db.collection("moodEvents");
//        Mood testMood = new Mood("testMoodId", "dummyUser", "Test mood", System.currentTimeMillis());
//        moodsRef.document("testMoodId").set(testMood);
    }


    @Test
    public void testNavigationToHomeFeed() {
        onView(withText("Sign In")).perform(click());
        onView(withText("Sign In")).check(matches(isDisplayed()));

        onView(withId(R.id.emailSignIn)).perform(typeText(TEST_EMAIL));
        onView(withId(R.id.passwordSignIn)).perform(typeText(TEST_PASSWORD));

        Espresso.closeSoftKeyboard();

        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> onView(withId(R.id.homeTextView)).check(matches(isDisplayed())));
    }
}