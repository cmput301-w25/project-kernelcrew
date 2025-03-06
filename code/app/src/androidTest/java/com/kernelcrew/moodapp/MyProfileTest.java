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

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class MyProfileTest extends FirebaseEmulatorMixin {

    private static final String USER_EMAIL = "test@kernelcrew.com";
    private static final String USER_PASSWORD = "Password@1234";
    private static final String TEST_USERNAME = "dummyUser";

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        // Seed Firestore with test user profile data.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create the test user.
        Tasks.await(auth.createUserWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("username", TEST_USERNAME);
        profileData.put("bio", "This is a test bio");

        Tasks.await(db.collection("users").document(TEST_USERNAME).set(profileData));
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testProfilePageLoadsAndSignOutWorks() {
        // --- Sign In Flow ---
        onView(withText("Sign In")).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.email))
                .perform(replaceText(USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(replaceText(USER_PASSWORD), closeSoftKeyboard());
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
        onView(withId(R.id.signOutButton)).perform(click());
        SystemClock.sleep(3000);

        // --- Verify that we are back on AuthHome ---
        onView(withId(R.id.signInButton)).check(matches(isDisplayed()));
    }

    @After
    public void signOutTheUser() {
        // Ensure the user is signed out after the test runs.
        FirebaseAuth.getInstance().signOut();
    }
}
