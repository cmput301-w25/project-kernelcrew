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
import static com.kernelcrew.moodapp.MoodDetailsNavigationTest.clickChildViewWithId;
import static com.kernelcrew.moodapp.MoodDetailsNavigationTest.scrollNestedScrollViewToBottom;

import android.os.SystemClock;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.MainActivity;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class OtherProfilePageNavigationTest extends FirebaseEmulatorMixin {
    private static final String USER_EMAIL = "test@kernelcrew.com";
    private static final String USER_PASSWORD = "Password@1234";
    private static final String EXPECTED_USERNAME = "testUser";
    private static final Emotion DATA_EMOTION = Emotion.HAPPINESS;
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create (or ensure) the test user exists.
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)) {
                throw e;
            }
        }

        // Sign in so that Firestore security rules allow write operations.
        Tasks.await(auth.signInWithEmailAndPassword(USER_EMAIL, USER_PASSWORD));

        // Get the current user's UID.
        String uid = auth.getCurrentUser().getUid();

        // Use a fixed username for testing.
        String fixedUsername = EXPECTED_USERNAME;

        // Clean up any preexisting documents for this test user and username.
        try {
            Tasks.await(db.collection("users").document(uid).delete());
        } catch (Exception ignored) { }
        try {
            Tasks.await(db.collection("usernames").document(fixedUsername).delete());
        } catch (Exception ignored) { }

        // Seed a user document in the "users" collection using merge to update if it exists.
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", USER_EMAIL);
        userData.put("username", fixedUsername);
        Tasks.await(db.collection("users").document(uid).set(userData, SetOptions.merge()));

        // Seed the corresponding username document in the "usernames" collection.
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uid);
        try {
            // We catch errors here in case the document already exists and the security rule disallows updates.
            Tasks.await(db.collection("usernames").document(fixedUsername).set(usernameData));
        } catch (Exception e) {
            // Ignoring error; document may already exist.
        }

        // Seed a MoodEvent document with the same UID.
        MoodEvent testEvent = new MoodEvent(
                uid,
                DATA_EMOTION,
                DATA_TRIGGER,         // trigger
                DATA_SOCIALSITUATION,   // socialSituation
                DATA_REASON,          // reason
                DATA_PHOTOURL,        // photoUrl
                DATA_LATITUDE,        // latitude
                DATA_LONGITUDE        // longitude
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(testEvent));

        auth.signOut();
    }

    @Before
    public void setupAuth() throws InterruptedException, ExecutionException {
        loginUser();
    }

    @Test
    public void testViewOtherProfilePageNavigationFromMoodDetails() throws InterruptedException, ExecutionException {
        onView(withText("Sign In")).perform(click());
        SystemClock.sleep(3000);

        onView(withId(R.id.emailSignIn))
                .perform(replaceText(USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn))
                .perform(replaceText(USER_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButtonAuthToHome))
                .perform(click());

        // Wait for HomeFeed to load data.
        SystemClock.sleep(3000);

        // Click on the first mood item's "View Details" button.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.viewDetailsButton)));

        // Wait for the MoodDetails screen to load.
        SystemClock.sleep(3000);

        // Scroll the NestedScrollView to the bottom.
        onView(withId(R.id.nestedScrollView))
                .perform(scrollNestedScrollViewToBottom());

        // Click the "View Profile" button.
        onView(withId(R.id.btnViewProfile))
                .perform(click());

        // Wait for the OtherUserProfile screen to load.
        SystemClock.sleep(3000);

        // Verify that the OtherUserProfile screen displays the expected username (contains "testUser")
        // and that the email matches.
        onView(withId(R.id.username_text))
                .check(matches(withText(Matchers.containsString(EXPECTED_USERNAME))));
        onView(withId(R.id.email_text)).check(matches(withText(USER_EMAIL)));
    }

    @After
    public void cleanup() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Clean up the test data in Firestore.
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null) {
            try {
                Tasks.await(db.collection("users").document(uid).delete());
            } catch (Exception ignored) { }
            try {
                Tasks.await(db.collection("usernames").document(EXPECTED_USERNAME).delete());
            } catch (Exception ignored) { }
            // Optionally: Delete any mood events associated with this UID if needed.
        }
        // Sign out the user.
        auth.signOut();
    }
}
