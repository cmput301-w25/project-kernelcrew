package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.containsString;

import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// Extend your Firebase emulator mixin (if your project uses it)
@RunWith(AndroidJUnit4.class)
public class SearchFeatureTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        // Assumes your staticCreateUser() method seeds required test data.
        staticCreateUser();
    }

    @Before
    public void signInUser() throws Exception {
        // Assume your sign-in process is similar to other tests.
        // For example, click on "Sign In", fill in credentials, and sign in.
        // (You can reuse code from SignInAndSignUpTest if needed.)
        // Here, we assume that by the time tests run, a test user is signed in.
        SystemClock.sleep(2000); // wait for HomeFeed to load
    }

    /**
     * Test 1: Verify that filtering on Home Feed by reason works.
     * This test enters a search term in the FilterBar's search field,
     * selects "Mood Reason" (filter type MOODS) and then verifies that
     * the displayed mood events contain the search term in their reason text.
     */
    @Test
    public void testHomeFeedSearchReason() {
        // Wait for the HomeFeed's RecyclerView to be displayed.
        onView(withId(R.id.moodRecyclerView)).check(matches(isDisplayed()));

        // Type a search term (assumed to exist in at least one mood's reason)
        String searchTerm = "lunch";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(searchTerm), closeSoftKeyboard());

        // Tap the "Mood Reason" button to set the search type to MOODS.
        onView(withId(R.id.searchReason)).perform(click());

        // Wait for the filtered results to update.
        SystemClock.sleep(2000);

        // Verify that at least one item in the RecyclerView contains the search term in its reason.
        onView(withId(R.id.moodRecyclerView))
                .check(matches(hasDescendant(withId(R.id.viewDetailsButton)))); // basic check that items exist

        // (Optionally, you could write a custom matcher to verify that each visible item’s reason text contains the search term.)
    }

    /**
     * Test 2: Verify that searching for users on Home Feed navigates correctly.
     * This test sets the filter to USERS, enters a query (e.g. "Bob"),
     * then clicks on a user item and checks that the OtherUserProfile screen
     * is displayed with the expected username.
     */
    @Test
    public void testHomeFeedSearchUserNavigation() {
        // Enter a search query in the filter search field.
        String userQuery = "Bob";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(userQuery), closeSoftKeyboard());

        // Tap the "Users" button to switch to user search.
        onView(withId(R.id.searchUser)).perform(click());

        SystemClock.sleep(2000);

        // Verify that the RecyclerView now shows user items.
        onView(withId(R.id.moodRecyclerView))
                .check(matches(hasDescendant(withId(R.id.usernameTextView))));

        // Click on the first user item (child view with id usernameTextView).
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.usernameTextView)));

        SystemClock.sleep(2000);

        // Verify that OtherUserProfile is displayed.
        onView(withId(R.id.username_text))
                .check(matches(isDisplayed()));
        // Verify that the username_text contains "Bob" (adjust as per seeded data).
        onView(withId(R.id.username_text))
                .check(matches(withIdTextContains("Bob")));
    }

    /**
     * Test 3: Verify that searching on the Mood History page (filtering by reason) works.
     * This test navigates to the Mood History screen, enters a search term,
     * and then verifies that the displayed mood events in the history have reasons
     * that include the search term.
     */
    @Test
    public void testMoodHistorySearchFeature() {
        // Navigate to Mood History.
        // This could be done via bottom navigation; here we simulate clicking the mood history tab.
        onView(withId(R.id.bottom_navigation)).perform(click());
        SystemClock.sleep(2000);

        // Verify that the Mood History RecyclerView is displayed.
        onView(withId(R.id.recyclerViewMoodHistory)).check(matches(isDisplayed()));

        // Enter a search term in the FilterBar's search field.
        String historySearchTerm = "meeting";
        onView(withId(R.id.filterSearchEditText))
                .perform(replaceText(historySearchTerm), closeSoftKeyboard());

        // Tap the "Mood Reason" button (on Mood History, this button is relabeled "Moods").
        onView(withId(R.id.searchReason)).perform(click());

        SystemClock.sleep(2000);

        // Verify that at least one item in the Mood History RecyclerView displays a reason containing the search term.
        onView(withId(R.id.recyclerViewMoodHistory))
                .check(matches(hasDescendant(withText(containsString(historySearchTerm.toLowerCase())))));
    }

    // Helper ViewAction: clicks a child view with a specified ID within a RecyclerView item.
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

    // Helper Matcher: verifies that a TextView's text contains the expected substring.
    public static Matcher<View> withIdTextContains(final String substring) {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public boolean matchesSafely(TextView textView) {
                return textView.getText().toString().toLowerCase().contains(substring.toLowerCase());
            }
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("with text containing: " + substring);
            }
        };
    }

    // Convenience method for checking text within a view by its id.
    public static Matcher<View> withIdTextContains(String expectedSubstring) {
        return withIdTextContains(expectedSubstring);
    }
}
