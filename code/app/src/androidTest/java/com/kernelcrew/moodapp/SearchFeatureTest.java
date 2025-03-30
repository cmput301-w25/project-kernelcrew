package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchFeatureTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        // Seed your test data using your staticCreateUser() method.
        staticCreateUser();
    }

    @Before
    public void signInUser() throws Exception {
        // Wait for HomeFeed to load; assumes user is already signed in via seed data.
        SystemClock.sleep(2000);
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
     * Test 1: Home Feed Reason Search Test
     * - If available, creates a mood event via UI with reason "Team lunch meeting".
     * - Then searches for "lunch" using the filter search field and taps the "Mood Reason" button.
     * - Verifies that the HomeFeed RecyclerView displays an item whose text contains "lunch".
     */
    @Test
    public void testHomeFeedSearchReason() {
        // Create a mood event if the creation view is available.
        if (isViewDisplayed(R.id.page_createMoodEvent)) {
            onView(withId(R.id.page_createMoodEvent)).perform(click());
            SystemClock.sleep(2000);
            String reasonText = "Team lunch meeting";
            SystemClock.sleep(10000);
            onView(withId(R.id.emotion_reason))
                    .perform(replaceText(reasonText));
            onView(withId(R.id.emotion_reason))
                    .perform(closeSoftKeyboard());
            onView(withId(R.id.toggle_happy)).perform(click());
            onView(withId(R.id.submit_button))
                    .perform(scrollTo(), click());
            SystemClock.sleep(3000);
        }
        // Now search by reason.
        String searchTerm = "lunch";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(searchTerm), closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());
        SystemClock.sleep(2000);
        // Click the first mood item’s "View Details" button (adjust the child view id if needed)
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));
        SystemClock.sleep(5000);
        // Verify that the Mood Details screen displays a reason containing the search term.
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText(containsString(searchTerm))));
    }

    /**
     * Test 2: Home Feed User Search & Navigation Test
     * - Signs out the current user.
     * - Creates a dummy user "Frederick" via the sign-up UI (with email "frederick@test.com" and password "passwordFrederick").
     * - Signs out "Frederick" and then signs in as the default test user ("Jonathan").
     * - Enters "Fred" in the search field, taps the "Users" button,
     *   clicks on the first user result, and verifies that the OtherUserProfile screen displays
     *   the expected data (username containing "Frederick" and email "frederick@test.com").
     */
    @Test
    public void testHomeFeedSearchUserNavigation() throws Exception {
        // Ensure no user is currently signed in.
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(3000);
        assert FirebaseAuth.getInstance().getCurrentUser() == null : "User should be signed out";

        // If the current page is not AuthHome, navigate to MyProfile and then sign out.
        if (!isViewDisplayed(R.id.authHome)) {
            onView(withId(R.id.page_myProfile)).perform(click());
            SystemClock.sleep(2000);
            onView(withId(R.id.signOutButton)).perform(click());
            SystemClock.sleep(2000);
        }

        activityScenarioRule.getScenario().recreate();
        SystemClock.sleep(3000);

        // Create dummy user "Frederick" via UI.
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText("Frederick"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("frederick@test.com"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("passwordFrederick"));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(3000);

        // Sign out dummy "Frederick".
        FirebaseAuth.getInstance().signOut();
        SystemClock.sleep(3000);

        // If the current page is not AuthHome, navigate to MyProfile and then sign out.
        if (!isViewDisplayed(R.id.authHome)) {
            onView(withId(R.id.page_myProfile)).perform(click());
            SystemClock.sleep(2000);
            onView(withId(R.id.signOutButton)).perform(click());
            SystemClock.sleep(2000);
        }

        // Sign in as default test user "Jonathan".
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText("Jonathan"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("jonathan@test.com"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("passwordJonathan"));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        SystemClock.sleep(5000);

        // Now, in Home Feed, enter a substring "eder" in the search field.
        String userQuery = "eder";
        onView(withId(R.id.filterSearchEditText)).perform(replaceText(userQuery));
        onView(withId(R.id.filterSearchEditText)).perform(closeSoftKeyboard());
        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(3000);

        // Verify that user items appear in the RecyclerView.
        onView(withId(R.id.moodRecyclerView))
                .check(matches(hasDescendant(withId(R.id.usernameTextView))));
        SystemClock.sleep(3000);

        // Then, click on the first user item's child view (usernameTextView).
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, click()));
        SystemClock.sleep(3000);

        // Verify that the OtherUserProfile screen displays the expected data.
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.username_text)).check(matches(withText(containsString("Frederick"))));
        onView(withId(R.id.email_text)).check(matches(isDisplayed()));
        onView(withId(R.id.email_text)).check(matches(withText(containsString("frederick@test.com"))));
    }

    /**
     * Test 3: Mood History Reason Search Test
     * - If available, creates a mood event via UI with reason "Project meeting update".
     * - Navigates to the Mood History page, enters "meeting" as a search term,
     *   taps the "Mood Reason" button, and verifies that the Mood History RecyclerView
     *   displays an item whose text contains "meeting".
     */
    @Test
    public void testMoodHistorySearchFeature() {
        // Create mood event
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        SystemClock.sleep(2000);
        String historyReason = "Project meeting update";
        onView(withId(R.id.emotion_reason))
                .perform(replaceText(historyReason));
        onView(withId(R.id.emotion_reason))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.toggle_sadness)).perform(click());
        onView(withId(R.id.submit_button)).perform(scrollTo(), click());
        SystemClock.sleep(3000);

        // Navigate to Mood History page.
        onView(withId(R.id.page_myHistory)).perform(click());
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Enter a search term.
        String historySearchTerm = "meeting";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(historySearchTerm));
        onView(withId(R.id.filterSearchEditText))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.searchReason)).perform(click());

        // Click the first item in the Mood History RecyclerView.
        onView(withId(R.id.recyclerViewMoodHistory))
                .perform(actionOnItemAtPosition(0, click()));

        // Wait for Mood Details screen to load.
        SystemClock.sleep(3000);

        // Verify that the Mood Details screen's reason TextView contains the search term.
        onView(withId(R.id.tvReasonValue))
                .check(matches(withText(containsString(historySearchTerm))));

    }

    // Helper ViewAction: clicks a child view with the specified id within a RecyclerView item.
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
}
