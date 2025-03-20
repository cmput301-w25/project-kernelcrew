package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MoodDetailsOwnershipTest extends FirebaseEmulatorMixin {

    // User 1 credentials
    private static final String USER1_USERNAME = "automatedtests1";
    private static final String USER1_EMAIL = "automatedtests1@kernelcrew.com";
    private static final String USER1_PASSWORD = "AT1@1234";

    // User 2 credentials
    private static final String USER2_USERNAME = "automatedtests2";
    private static final String USER2_EMAIL = "automatedtests2@kernelcrew.com";
    private static final String USER2_PASSWORD = "AT2@1234";

    // Mood details for test creation
    private static final String MOOD1_TRIGGER = "Morning Run";
    private static final String MOOD1_REASON = "Feeling Energized";
    private static final String MOOD2_TRIGGER = "Late Night Study";
    private static final String MOOD2_REASON = "Focused";

    /**
     * Helper method to delete a test user if it exists.
     */
    private static void deleteTestUser(String email, String password, String username) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            Tasks.await(auth.signInWithEmailAndPassword(email, password));
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                Tasks.await(user.delete());
            }
        } catch (Exception e) {
            // User likely doesn't exist or sign-in failed
        } finally {
            auth.signOut();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            // Delete the username doc
            Tasks.await(db.collection("usernames").document(username).delete());
        } catch (Exception ignored) { }
        try {
            // Delete the user doc
            List<DocumentSnapshot> docs = Tasks.await(
                    db.collection("users").whereEqualTo("email", email).get()
            ).getDocuments();
            for (DocumentSnapshot doc : docs) {
                Tasks.await(db.collection("users").document(doc.getId()).delete());
            }
        } catch (Exception ignored) { }
    }

    @BeforeClass
    public static void cleanUpExistingUsers() throws Exception {
        System.out.println("Deleting test user 1 if exists...");
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        System.out.println("Deleting test user 2 if exists...");
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
        // Allow deletion to propagate.
        SystemClock.sleep(4000);
    }

    @AfterClass
    public static void cleanUpAfterAllTests() throws Exception {
        // Final cleanup
        FirebaseAuth.getInstance().signOut();
        deleteTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME);
        deleteTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME);
    }

    /**
     * Test #1: User1 signs up, creates a mood, verifies Edit/Delete are visible, then signs out.
     */
    @Test
    public void test01_User1CreatesMood() throws InterruptedException {
        // Launch app fresh
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        SystemClock.sleep(3000);

        // Sign up as User1
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER1_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER1_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER1_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Create a mood as User1
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD1_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD1_REASON), closeSoftKeyboard());
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // Open the newly created mood's details (position 0)
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify Edit/Delete are visible
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));

        // Sign out User1
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(1000);

        // Close scenario
        scenario.close();
    }

    /**
     * Test #2: User2 signs up, creates a mood, verifies Edit/Delete are visible for own mood, then signs out.
     */
    @Test
    public void test02_User2CreatesMood() throws InterruptedException {
        // Launch fresh
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        SystemClock.sleep(3000);

        // Sign up as User2
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER2_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER2_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER2_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Create a mood as User2
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.emotion_trigger))
                .perform(scrollTo(), replaceText(MOOD2_TRIGGER), closeSoftKeyboard());
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD2_REASON), closeSoftKeyboard());
        onView(withId(R.id.toggle_anger)).perform(click());
        onView(withId(R.id.submit_button))
                .perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // Open the newly created mood's details (position 0)
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify Edit/Delete are visible for User2's own mood
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));

        // Sign out User2
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(1000);

        scenario.close();
    }

    /**
     * Test #3: User2 re-signs in, opens User1's mood, verifies Edit/Delete are NOT visible.
     * This test depends on the mood that User1 created in test01_User1CreatesMood().
     */
    @Test
    public void test03_User2ViewsUser1Mood() throws InterruptedException {
        // Launch fresh
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        SystemClock.sleep(3000);

        // Sign in as User2
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER2_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER2_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Now we expect that the RecyclerView has:
        //   - position 0: user2's own mood
        //   - position 1: user1's mood from test01
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(1, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify Edit/Delete are GONE for user1's mood when viewed by user2
        onView(withId(R.id.btnEditMood))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnDeleteMood))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        scenario.close();
    }

    /**
     * Helper method: Click a child view with a given ID inside a RecyclerView item.
     */
    public static androidx.test.espresso.ViewAction clickChildViewWithId(final int id) {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public org.hamcrest.Matcher<View> getConstraints() {
                return isDisplayed();
            }
            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }
            @Override
            public void perform(androidx.test.espresso.UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null && childView.isClickable()) {
                    childView.performClick();
                }
            }
        };
    }
}
