package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class MoodDetailsNavigationTest extends FirebaseEmulatorMixin {
    private static final String USER_EMAIL = "test@kernelcrew.com";
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
                DATA_EMOTION,
                DATA_SOCIALSITUATION,
                DATA_REASON,
                DATA_LATITUDE,
                DATA_LONGITUDE
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(testEvent));

        auth.signOut();
    }

    @Before
    public void setupAuth() throws InterruptedException, ExecutionException {
        loginUser();
    }

    @Test
    public void testNavigationToMoodDetails() throws InterruptedException {
        // On AuthHome screen: Click the "Sign In" button.
        onView(withText("Sign In"))
                .perform(click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        onView(withId(R.id.emailSignIn))
                .check(matches(isDisplayed()));

        // Fill in the email and password fields.
        onView(withId(R.id.emailSignIn))
                .perform(replaceText(USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn))
                .perform(replaceText(USER_PASSWORD), closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        onView(withId(R.id.signInButtonAuthToHome))
                .perform(click());

        SystemClock.sleep(1000);

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        onView(withId(R.id.filterBarFragment))
                .check(matches(isDisplayed()));

        // Click on the first mood item in the RecyclerView to view its details.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for the MoodDetails screen to load.
        SystemClock.sleep(3000);

        // Verify that key elements on the MoodDetails screen are displayed and have the correct text.
        onView(withId(R.id.tvMoodState))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvMoodState))
                .check(matches(withText(DATA_EMOTION.toString())));
        onView(withId(R.id.tvSocialSituationValue))
                .check(matches(withText(DATA_SOCIALSITUATION)));
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText(DATA_REASON)));
    }


    // Custom ViewAction to scroll a NestedScrollView to the bottom.
    public static ViewAction scrollNestedScrollViewToBottom() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                // Ensure that this action is only used on a NestedScrollView (or its subclass).
                return isAssignableFrom(androidx.core.widget.NestedScrollView.class);
            }

            @Override
            public String getDescription() {
                return "Scroll NestedScrollView to bottom";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof androidx.core.widget.NestedScrollView) {
                    ((androidx.core.widget.NestedScrollView) view).fullScroll(View.FOCUS_DOWN);
                    uiController.loopMainThreadUntilIdle();
                }
            }
        };
    }

    @After
    public void signOutTheUser() {
        // Not necessary right now, but will be if we add more tests
        FirebaseAuth.getInstance().signOut();
    }
}
