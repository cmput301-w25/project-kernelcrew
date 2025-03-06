package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static org.awaitility.Awaitility.await;

import android.os.SystemClock;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class MyProfileTest extends FirebaseEmulatorMixin {
    // Changeable strings
    private static final String TEST_EMAIL =  "test@kernelcrew.com";
    private static final String TEST_PASSWORD = "Password@1234";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() throws InterruptedException, ExecutionException {
        createUser();
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    @Test
    public void testProfilePageLoadsAndSignOutWorks() {
        // --- Sign In Flow ---
        FirebaseAuth.getInstance().signOut();

        // On AuthHome screen: Click the "Sign In" button.
        Espresso.onView(ViewMatchers.withId(R.id.buttonInitialToSignIn))
                .perform(ViewActions.click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        onView(withId(R.id.emailSignIn))
                .check(matches(isDisplayed()));

        // Fill in the email and password fields with the correct credentials
        onView(withId(R.id.emailSignIn))
                .perform(replaceText(TEST_EMAIL), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn))
                .perform(replaceText(TEST_PASSWORD), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        Espresso.onView(ViewMatchers.withId(R.id.signInButtonAuthToHome))
                .perform(ViewActions.click());

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
        onView(withId(R.id.buttonInitialToSignIn)).check(matches(isDisplayed()));
    }

    private void createUser() throws InterruptedException, ExecutionException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> createUserTask = auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        Tasks.await(createUserTask);
        auth.signOut();
    }
}
