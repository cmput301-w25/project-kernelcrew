package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
// Taha used ChatCPT to fix this test...(multiple prompts/errors)
public class OtherUserProfileMoodTest extends FirebaseEmulatorMixin {
    // Test user details for automatedtests1 (the viewer) and automatedtests2 (the profile owner)
    private static final String USER1_EMAIL = "auto1@test.com";
    private static final String USER1_USERNAME = "automatedtests1";
    private static final String USER1_PASSWORD = "TestPass1";

    private static final String USER2_EMAIL = "auto2@test.com";
    private static final String USER2_USERNAME = "automatedtests2";
    private static final String USER2_PASSWORD = "TestPass2";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Seed the database with two user profiles and several mood events for USER2.
     *
     * We make sure to delete any existing documents so that we perform “create” operations
     * (which are required by our rules) rather than updates.
     */
    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException, IOException {
        final int firestorePort = 8080;
        final String androidLocalhost = "10.0.2.2";

        // Get the projectId from FirebaseApp configuration.
        String projectId = FirebaseApp.getInstance().getOptions().getProjectId();

        // First, remove any preexisting username document for USER2 by calling the emulator REST endpoint.
        String usernameUrl = "http://" + androidLocalhost + ":" + firestorePort +
                "/emulator/v1/projects/" + projectId +
                "/databases/(default)/documents/usernames/" + USER2_USERNAME;
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(usernameUrl).openConnection();
        urlConnection.setRequestMethod("DELETE");
        int responseCode = urlConnection.getResponseCode();
        // Optionally log the response:
        System.out.println("DELETE /usernames/" + USER2_USERNAME + " response: " + responseCode);
        urlConnection.disconnect();

        // Get Firestore and Auth instances.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create or ensure USER1 exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER1_EMAIL, USER1_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }
        // Create or ensure USER2 exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER2_EMAIL, USER2_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }

        // Sign in as USER2 so that the auth context is correct.
        Tasks.await(auth.signInWithEmailAndPassword(USER2_EMAIL, USER2_PASSWORD));
        String uid2 = auth.getCurrentUser().getUid();

        // Create /users/{uid} document for USER2.
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("uid", uid2);
        user2Data.put("email", USER2_EMAIL);
        user2Data.put("username", USER2_USERNAME);
        // Since the document doesn’t exist, this is a create operation that will check:
        // request.auth.uid == uid2 and isUniqueUsernameAndOwner(request.resource.data.username)
        // (which will be true now because we just deleted the username document).
        Tasks.await(db.collection("users").document(uid2).set(user2Data));

        // Now create the username document for USER2.
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uid2);
        Tasks.await(db.collection("usernames").document(USER2_USERNAME).set(usernameData));

        // Clear any existing mood events for USER2.
        QuerySnapshot snapshot = Tasks.await(
                db.collection("moodEvents").whereEqualTo("uid", uid2).get());
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Tasks.await(doc.getReference().delete());
        }

        // Seed several public MoodEvent documents for USER2.
        for (int i = 1; i <= 3; i++) {
            MoodEvent mood = new MoodEvent(
                    uid2,
                    USER2_USERNAME,
                    Emotion.HAPPINESS,
                    "Social Situation " + i,
                    "Reason " + i,
                    34.052235 + i * 0.001,    // Slight variation in latitude
                    -118.243683 - i * 0.001    // Slight variation in longitude
            );
            mood.setVisibility(MoodEventVisibility.PUBLIC);
            // Use add() to perform a pure create operation.
            Tasks.await(db.collection("moodEvents").add(mood));
        }

        auth.signOut();
    }

    @Before
    public void setupAuth() throws ExecutionException, InterruptedException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Simulate navigating to the sign in screen and filling out the form.
        onView(withText("Sign In")).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(USER1_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn)).perform(replaceText(USER1_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        // Wait for HomeFeed to load.
        SystemClock.sleep(2000);
    }

    @Test
    public void testOtherUserProfileDisplaysCorrectMoods() throws InterruptedException {
        // Ensure HomeFeed's RecyclerView is displayed.
        onView(withId(R.id.moodRecyclerView)).check(matches(isDisplayed()));

        // In HomeFeed, simulate clicking on a mood item created by USER2.
        // (Assumes the RecyclerView item has a "View Details" button with id viewDetailsButton.)
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for MoodDetails to load.
        SystemClock.sleep(1500);

        onView(withText("Users")).perform(click());
        SystemClock.sleep(1500);

        // In the MoodDetails screen, click on the username TextView (id: tvUsernameDisplay) to navigate to OtherUserProfile.
        onView(withIndex(withText("automatedtests2"), 0)).perform(click());

        // Wait for OtherUserProfile screen to load.
        SystemClock.sleep(1500);

        // Verify that OtherUserProfile displays the correct username and email for USER2.
        onView(withId(R.id.username_text))
                .check(matches(withText(containsString(USER2_USERNAME))));
        onView(withId(R.id.email_text))
                .check(matches(withText(USER2_EMAIL)));

        // Check that the RecyclerView for public moods displays at least 2 items.
        onView(withId(R.id.public_moods_recycler_view))
                .check((view, noViewFoundException) -> {
                    assertNotNull("RecyclerView is null", view);
                    RecyclerView recyclerView = (RecyclerView) view;
                    int itemCount = recyclerView.getAdapter().getItemCount();
                    if (itemCount < 2) {
                        throw new AssertionError("Expected at least 2 mood items, found " + itemCount);
                    }
                });

        // Verify that the first two mood items display the USER2 username.
        onView(withId(R.id.public_moods_recycler_view))
                .perform(actionOnItemAtPosition(0, checkItemHasDescendantWithText(USER2_USERNAME)));
        onView(withId(R.id.public_moods_recycler_view))
                .perform(actionOnItemAtPosition(1, checkItemHasDescendantWithText(USER2_USERNAME)));
    }

    // Custom ViewAction to check that a RecyclerView item has a descendant with the expected text.
    public static ViewAction checkItemHasDescendantWithText(final String expectedText) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed(); // Ensures the view is visible.
            }

            @Override
            public String getDescription() {
                return "Check that the item has a descendant with text: " + expectedText;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!hasDescendantWithText(view, expectedText)) {
                    throw new AssertionError("Expected descendant with text \"" + expectedText + "\" not found.");
                }
            }

            private boolean hasDescendantWithText(View view, String expectedText) {
                if (view instanceof android.widget.TextView) {
                    CharSequence text = ((android.widget.TextView) view).getText();
                    if (text != null && text.toString().contains(expectedText)) {
                        return true;
                    }
                }
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        if (hasDescendantWithText(group.getChildAt(i), expectedText)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

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

    @After
    public void signOutTheUser() throws ExecutionException, InterruptedException {
        FirebaseAuth.getInstance().signOut();
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;
            @Override
            public void describeTo(Description description) {
                description.appendText("with index: " + index);
                matcher.describeTo(description);
            }
            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

}