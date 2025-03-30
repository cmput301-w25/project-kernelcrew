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
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;
import static com.kernelcrew.moodapp.MoodDetailsNavigationTest.clickChildViewWithId;

import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
     * Helper method that checks if a view with the given id is displayed.
     */
    private boolean isViewDisplayed(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Test the complete follow flow:
     * 1. Sign in as User A, navigate from the home feed (via mood details) to User B’s profile,
     *    and click the follow button. Wait for its text to change to "Requested."
     * 2. Sign out, then sign in as User B, navigate to MyProfile and then to Follow Requests,
     *    accept the follow request, and verify that the follower count is updated.
     */
    @Test
    public void test_1_testFollowFlow() throws InterruptedException, ExecutionException {
        // PART 1: User A sends a follow request.
        signInUser(USER_A_EMAIL, USER_A_PASSWORD);

        // From home feed, click on first mood event's "View Details" button.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(1500);

        // In MoodDetails screen, click on the username chip to navigate to OtherUserProfile.
        onView(withId(R.id.tvUsernameDisplay)).perform(click());
        SystemClock.sleep(1500);

        // In OtherUserProfile, click the follow button.
        onView(withId(R.id.followButton)).check(matches(isDisplayed()));
        SystemClock.sleep(2000); // Allow UI to settle before clicking.
        onView(withId(R.id.followButton)).perform(click());
        SystemClock.sleep(1500);

        // Wait until the button text updates to "Requested".
        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    onView(withId(R.id.followButton))
                            .check(matches(withText(containsString("Requested"))));
                });

        FirebaseAuth.getInstance().signOut();

        // PART 2: Sign in as User B.
        signInUser(USER_B_EMAIL, USER_B_PASSWORD);
        SystemClock.sleep(2000);

        // Navigate to MyProfile for User B.
        onView(withId(R.id.page_myProfile)).perform(click());
        SystemClock.sleep(1000);

        // Click on the Follow Requests button.
        onView(withId(R.id.followRequestsButton)).check(matches(isDisplayed()));
        onView(withId(R.id.followRequestsButton)).perform(click());
        SystemClock.sleep(1500);


        // In the FollowRequestsFragment, verify that the request from User A is visible.
        onView(withText(containsString(USER_A_USERNAME + " is requesting to follow you")))
                .check(matches(isDisplayed()));
//
//        Awaitility.await()
//                .atMost(30, TimeUnit.SECONDS)
//                .pollInterval(1, TimeUnit.SECONDS)
//                .untilAsserted(() -> {
//                    onView(withText(containsString(USER_A_USERNAME + " is requesting to follow you")))
//                            .check(matches(isDisplayed()));
//                });

        // Click on the "Accept" button (assumes it’s labeled "Accept").
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

    private void signInUser(String email, String password) {
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        // Wait until sign-in is complete.
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
                FirebaseAuth.getInstance().getCurrentUser() != null);
        SystemClock.sleep(1000); // Extra delay for UI to settle
    }

    @After
    public void tearDown() {
        FirebaseAuth.getInstance().signOut();
    }
}
