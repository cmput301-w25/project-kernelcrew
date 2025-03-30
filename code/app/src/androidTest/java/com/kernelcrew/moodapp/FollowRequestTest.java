package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.containsString;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FollowRequestTest extends FirebaseEmulatorMixin {

    // Test user credentials for User A (the follower) and User B (the target)
    private static final String USER_A_EMAIL = "auto1@test.com";
    private static final String USER_A_USERNAME = "automatedtests1";
    private static final String USER_A_PASSWORD = "TestPass1";

    private static final String USER_B_EMAIL = "auto2@test.com";
    private static final String USER_B_USERNAME = "automatedtests2";
    private static final String USER_B_PASSWORD = "TestPass2";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Seed the database:
     * - Create or ensure that USER A and USER B exist.
     * - Sign in as USER B and add one public mood event.
     */
    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create or ensure USER A exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER_A_EMAIL, USER_A_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException))
                throw e;
        }
        // Create or ensure USER B exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER_B_EMAIL, USER_B_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException))
                throw e;
        }

        // Sign in as USER B to seed their data.
        Tasks.await(auth.signInWithEmailAndPassword(USER_B_EMAIL, USER_B_PASSWORD));
        String uidB = auth.getCurrentUser().getUid();

        // Create a user document for USER B.
        Map<String, Object> userBData = new HashMap<>();
        userBData.put("uid", uidB);
        userBData.put("email", USER_B_EMAIL);
        userBData.put("username", USER_B_USERNAME);
        Tasks.await(db.collection("users").document(uidB).set(userBData, SetOptions.merge()));

        // Seed the corresponding usernames document.
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uidB);
        try {
            Tasks.await(db.collection("usernames").document(USER_B_USERNAME).set(usernameData, SetOptions.merge()));
        } catch (Exception e) {
            System.out.println("Emulator permission error on 'usernames' write: " + e.getMessage());
        }

        // Seed one public mood event for USER B.
        MoodEventProvider moodEventProvider = MoodEventProvider.getInstance();
        MoodEvent mood = new MoodEvent(
                uidB,
                USER_B_USERNAME,
                Emotion.HAPPINESS,
                "UserB Social",
                "UserB Reason",
                34.052235,
                -118.243683
        );
        mood.setVisibility(MoodEventVisibility.PUBLIC);
        Tasks.await(moodEventProvider.insertMoodEvent(mood));

        auth.signOut();
    }

    /**
     * Test the complete follow flow:
     * 1. Sign in as User A, navigate from the home feed (via mood details) to User B’s profile,
     *    and click the follow button. Wait for its text to change to "Requested."
     * 2. Sign out, then sign in as User B, navigate to MyProfile and then to Follow Requests,
     *    accept the follow request, and verify that the follower count is updated.
     */
    @Test
    public void testFollowFlow() throws InterruptedException, ExecutionException {
        // PART 1: User A sends a follow request.
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER_A_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER_A_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        SystemClock.sleep(2000); // Wait for home feed to load.

        // In the home feed, click on the first mood event's "View Details" button.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(1500);

        // In the MoodDetails screen, click on the username chip (tvUsernameDisplay) to navigate to OtherUserProfile.
        onView(withId(R.id.tvUsernameDisplay)).perform(click());
        SystemClock.sleep(1500);

        // In OtherUserProfile, click the follow button.
        onView(withId(R.id.followButton)).check(matches(isDisplayed()));
        onView(withId(R.id.followButton)).perform(click());

        // Wait until the follow button text changes to "Requested". Increase timeout if needed.
        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            onView(withId(R.id.followButton)).check(matches(withText(containsString("Requested"))));
        });

        // PART 2: User B accepts the follow request.
        // Navigate to MyProfile (using bottom nav) and sign out as User A.
        onView(withId(R.id.page_myProfile)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.signOutButton)).perform(click());
        SystemClock.sleep(1500);

        // Sign in as User B.
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER_B_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER_B_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        SystemClock.sleep(2000);

        // Navigate to MyProfile for User B.
        onView(withId(R.id.page_myProfile)).perform(click());
        SystemClock.sleep(1000);

        // Click on the Follow Requests button.
        onView(withId(R.id.followRequestsButton)).check(matches(isDisplayed()));
        onView(withId(R.id.followRequestsButton)).perform(click());
        SystemClock.sleep(1500);

        // In FollowRequestsFragment, verify the request from User A is visible.
        onView(withText(containsString(USER_A_USERNAME + " is requesting to follow you")))
                .check(matches(isDisplayed()));
        // Click the "Accept" button (adjust text if needed; here we assume it's labeled "Accept").
        onView(withText("Accept")).perform(click());
        SystemClock.sleep(1500);

        // Finally, navigate back to MyProfile for User B and verify that the followers count shows at least "1".
        onView(withId(R.id.page_myProfile)).perform(click());
        SystemClock.sleep(1500);
        onView(withId(R.id.followers_button)).check(matches(withText(containsString("1"))));
    }

    /**
     * Helper ViewAction to click on a child view within a parent.
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }
            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }
            @Override
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null && childView.isClickable()) {
                    childView.performClick();
                }
            }
        };
    }

    @After
    public void tearDown() {
        FirebaseAuth.getInstance().signOut();
    }
}
