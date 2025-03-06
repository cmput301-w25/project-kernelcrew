package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.Mood;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class MoodDetailsNavigationTest extends FirebaseEmulatorMixin {
    private static final String USER_EMAIL = "test@kernelcrew.com";
    private static final String USER_PASSWORD = "Password@1234";
    private static final Emotion DATA_EMOTION = Emotion.valueOf("HAPPINESS");
    private static final String DATA_TRIGGER = "Morning Coffee";
    private static final String DATA_SOCIALSITUATION = "With Friends";
    private static final String DATA_REASON = "Celebration";
    private static final String DATA_PHOTOURL = "https://example.com/photo.jpg";
    private static final double DATA_LATITUDE = 34.052235;
    private static final double DATA_LONGITUDE = -118.243683;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        // Seed Firestore with a test Mood and corresponding MoodEvent.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Tasks.await(auth.createUserWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));

        // Seed a basic Mood document used in the HomeFeed RecyclerView.
        CollectionReference moodsRef = db.collection("moods");
        Mood testMood = new Mood("testMoodId", "dummyUser", "Happy", System.currentTimeMillis());
        moodsRef.document("testMoodId").set(testMood);

        // Seed a detailed MoodEvent document used in the MoodDetails screen.
        CollectionReference moodEventsRef = db.collection("moodEvent");
        MoodEvent testEvent = new MoodEvent(
                "dummyUser",
                Emotion.HAPPINESS,
                DATA_TRIGGER,       // trigger
                DATA_SOCIALSITUATION,         // socialSituation
                DATA_REASON,          // reason
                DATA_PHOTOURL, // photoUrl
                DATA_LATITUDE,              // latitude
                DATA_LONGITUDE             // longitude
        );
        // Set the id to match the test document id.
        testEvent.setId("testMoodId");
        Tasks.await(moodEventsRef.document("testMoodId").set(testEvent));

        auth.signOut();
    }

    @Test
    public void testNavigationToMoodDetails() throws InterruptedException {
        // On AuthHome screen: Click the "Sign In" button.
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

        // Wait for the RecyclerView to load the seeded mood document.
        SystemClock.sleep(1000);

        // Click on the first mood item in the RecyclerView to view its details.
        // (Ensure that your HomeFeed layout contains a RecyclerView with id moodRecyclerView.)
        onView(withId(R.id.moodRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait for the MoodDetails screen to load.
        SystemClock.sleep(3000);

        // Click on view details button to go to view details screen
        onView(withId(R.id.viewDetailsButton))
                .perform(click());

        // Verify that key elements on the MoodDetails screen are displayed and have the correct text.
        onView(withId(R.id.tvMoodState))
                .check(matches(isDisplayed()));
//        onView(withId(R.id.tvMoodState))
//                .check(matches(withText(DATA_EMOTION.toString())));
        onView(withId(R.id.tvTriggerValue))
                .check(matches(withText(DATA_TRIGGER)));
        onView(withId(R.id.tvSocialSituationValue))
                .check(matches(withText(DATA_SOCIALSITUATION)));
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText(DATA_REASON)));
    }

    @After
    public void signOutTheUser() {
        // Not necessary right now, but will be if we add more tests
        FirebaseAuth.getInstance().signOut();
    }
}
