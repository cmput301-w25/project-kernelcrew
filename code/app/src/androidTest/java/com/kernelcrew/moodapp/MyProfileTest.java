package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class MyProfileTest extends FirebaseEmulatorMixin {

    private static final String TEST_EMAIL = "test@kernelcrew.com";
    private static final String TEST_PASSWORD = "Password@1234";
    private static final String TEST_USERNAME = "dummyUser";

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        // Seed Firestore with test user profile data.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create the test user, ignore if already exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }

        // Sign in so that request.auth is non-null.
        Tasks.await(auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
        String uid = auth.getCurrentUser().getUid();

        // Create profile data with the uid included.
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("uid", uid);
        profileData.put("username", TEST_USERNAME);
        profileData.put("bio", "This is a test bio");

        // Use uid as the document id.
        Tasks.await(db.collection("users").document(uid).set(profileData));
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() throws InterruptedException, ExecutionException {
        // The test user is already created in seedDatabase(), so we don't need to call createUser() again.
        FirebaseAuth.getInstance().signOut();

        // Wait for sign out to complete before proceeding.
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> FirebaseAuth.getInstance().getCurrentUser() == null);
    }


    @Test
    public void testProfilePageLoadsAndSignOutWorks() throws InterruptedException {
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

        Thread.sleep(1000);
        // --- Verify HomeFeed is loaded ---
        onView(withId(R.id.filterBarFragment)).check(matches(isDisplayed()));

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
        onView(withId(R.id.buttonInitialToSignIn)).check(matches(isDisplayed()));
    }
}
