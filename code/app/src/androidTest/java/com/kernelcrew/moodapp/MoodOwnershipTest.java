package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.kernelcrew.moodapp.MoodDetailsNavigationTest.clickChildViewWithId;
import static org.hamcrest.Matchers.not;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MoodOwnershipTest extends FirebaseEmulatorMixin {

    // User 1 credentials
    private static final String USER1_USERNAME = "moodownershipautomatedtests1";
    private static final String USER1_EMAIL = "moodownership_automatedtests1@kernelcrew.com";
    private static final String USER1_PASSWORD = "MOAT1@1234";

    // User 2 credentials
    private static final String USER2_USERNAME = "moodownershipautomatedtests2";
    private static final String USER2_EMAIL = "moodownership_automatedtests2@kernelcrew.com";
    private static final String USER2_PASSWORD = "MOAT2@1234";

    // Mood details for test creation
    private static final String MOOD1_REASON = "Feeling Energized";

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
        SystemClock.sleep(4000);
    }

    /**
     * Combined test:
     * - Sign up as User1, create a mood via GUI and verify that Edit/Delete buttons are displayed.
     * - Sign out User1.
     * - Sign up as User2, view User1’s mood details, and verify that Edit/Delete buttons are not displayed.
     */
    @Test
    public void test_MoodOwnershipVerification() throws InterruptedException, ExecutionException {
        // --- User1 Flow ---
        ActivityScenario<MainActivity> scenario1 = ActivityScenario.launch(MainActivity.class);
        SystemClock.sleep(3000);

        // Sign up as User1
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER1_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER1_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER1_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Create a mood as User1 via GUI
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), replaceText(MOOD1_REASON), closeSoftKeyboard());
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());
        SystemClock.sleep(3000);

        // Open the newly created mood's details (position 0)
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(3000);

        // Verify that Edit/Delete buttons are displayed for User1's own mood
        onView(withId(R.id.btnEditMood)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteMood)).check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Sign out User1
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(3000);
        scenario1.close();

        // --- User2 Flow ---
        ActivityScenario<MainActivity> scenario2 = ActivityScenario.launch(MainActivity.class);
        SystemClock.sleep(3000);

        // Sign up as User2
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText(USER2_USERNAME));
        onView(withId(R.id.emailSignUp)).perform(replaceText(USER2_EMAIL));
        onView(withId(R.id.passwordSignUp)).perform(replaceText(USER2_PASSWORD));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());

        // Make user2 follow user1
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userAUid = Tasks.await(db.collection("usernames").document(USER1_USERNAME).get()).getString("uid");
        String userBUid = Tasks.await(db.collection("usernames").document(USER2_USERNAME).get()).getString("uid");
        db.collection("users").document(userAUid)
                .collection("followers")
                .document(userBUid)
                .set(Collections.emptyMap());
        db.collection("users").document(userBUid)
                .collection("following")
                .document(userAUid)
                .set(Collections.emptyMap());
        SystemClock.sleep(1000);
        onView(withId(R.id.page_myProfile)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.page_home)).perform(click());
        SystemClock.sleep(3000);

        // Open the mood details of the mood created by User1 (should appear at position 0)
        onView(withText("View Details")).perform(click());

        // Verify that Edit/Delete buttons are NOT displayed for a mood not owned by User2
        onView(withId(R.id.btnEditMood)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btnDeleteMood)).check(matches(not(isDisplayed())));
        SystemClock.sleep(3000);

        // Sign out User2
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(3000);
        scenario2.close();
    }
}
