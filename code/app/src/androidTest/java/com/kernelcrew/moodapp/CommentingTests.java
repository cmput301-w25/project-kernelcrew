package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertEquals;

import android.os.SystemClock;
import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Comment;
import com.kernelcrew.moodapp.data.CommentProvider;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CommentingTests extends FirebaseEmulatorMixin {

    private static final String TEST_USER_ID = "test_user_id";
    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_COMMENT_TEXT = "This is a test comment";
    private static final String USER_EMAIL = "test@kernelcrew.com";
    private static final String USERNAME = "Test User";
    private static final String USER_PASSWORD = "Password@1234";
    private static final Emotion DATA_EMOTION = Emotion.HAPPINESS;
    private static final String DATA_SOCIALSITUATION = "With Friends";
    private static final String DATA_REASON = "Celebration";
    private static final double DATA_LATITUDE = 34.052235;
    private static final double DATA_LONGITUDE = -118.243683;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Custom ViewAction to click a child view with a given id.
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

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        // Seed Firestore with a test Mood and corresponding MoodEvent.
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create (or ensure) the test user exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }

        // Sign in so Firestore security rules allow write operations.
        Tasks.await(auth.signInWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));

        // Get the current user uid.
        String uid = auth.getCurrentUser().getUid();

        // Seed a detailed MoodEvent document used in the MoodDetails screen.
        MoodEvent testEvent = new MoodEvent(
                uid,
                USERNAME,
                DATA_EMOTION,
                DATA_SOCIALSITUATION,
                DATA_REASON,
                DATA_LATITUDE,
                DATA_LONGITUDE
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(testEvent));

        Comment newComment = new Comment(TEST_USER_ID, TEST_USERNAME, testEvent.getId(), TEST_COMMENT_TEXT);

        Tasks.await(CommentProvider.getInstance().insertComment(newComment));

        auth.signOut();
    }

    @Before
    public void setupAuth() throws InterruptedException, ExecutionException {
        loginUser();
    }

    @Test
    public void testNavigationToCommentsPage() throws InterruptedException {
        onView(withText("Sign In")).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.filterBarFragment)).check(matches(isDisplayed()));

        SystemClock.sleep(1000);

        onView(withId(R.id.moodRecyclerView)).perform(actionOnItemAtPosition(0,
                clickChildViewWithId(R.id.commentLayout)));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> onView(withId(R.id.moodContainer)).check(matches(isDisplayed())));

    }

    @Test
    public void testCreateComment() throws InterruptedException {
        onView(withText("Sign In")).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.filterBarFragment)).check(matches(isDisplayed()));

        SystemClock.sleep(1000);

        onView(withId(R.id.moodRecyclerView)).perform(actionOnItemAtPosition(0,
                clickChildViewWithId(R.id.commentLayout)));

        onView(withId(R.id.searchInput)).perform(replaceText("A second comment"), closeSoftKeyboard());

        onView(allOf(
                withId(com.google.android.material.R.id.text_input_end_icon),
                isDescendantOfA(withId(R.id.searchInputLayout))
        )).perform(click());

        SystemClock.sleep(3000);

        onView(withText("A second comment")).check(matches(isDisplayed()));
    }

}