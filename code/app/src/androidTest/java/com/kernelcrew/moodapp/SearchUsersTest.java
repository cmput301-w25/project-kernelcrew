package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.os.SystemClock;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchUsersTest {

    private static final String USER_A_USERNAME = "searchusertest1";
    private static final String USER_A_EMAIL = "searchuser_auto1@test.com";
    private static final String USER_A_PASSWORD = "SearchUserTestPass1";
    private static final String USER_B_USERNAME = "searchusertest2";
    private static final String USER_B_EMAIL = "searchuser_auto2@test.com";
    private static final String USER_B_PASSWORD = "SearchUserTestPass2";
    private static final String USER_C_USERNAME = "searchusertest3";
    private static final String USER_C_EMAIL = "searchuser_auto3@test.com";
    private static final String USER_C_PASSWORD = "SearchUserTestPass3";
    private static final String INVALID_USERNAME = "nonexistentuser";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        createUserAndSetUsername(auth, db, USER_A_EMAIL, USER_A_PASSWORD, USER_A_USERNAME);
        createUserAndSetUsername(auth, db, USER_B_EMAIL, USER_B_PASSWORD, USER_B_USERNAME);
        createUserAndSetUsername(auth, db, USER_C_EMAIL, USER_C_PASSWORD, USER_C_USERNAME);
    }

    private static void createUserAndSetUsername(FirebaseAuth auth, FirebaseFirestore db, String email, String password, String username) throws ExecutionException, InterruptedException {
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(email, password));
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException))
                throw e;
        }

        Tasks.await(auth.signInWithEmailAndPassword(email, password));
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("username", username);
        Tasks.await(db.collection("users").document(uid).set(userData, SetOptions.merge()));

        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", uid);
        //Tasks.await(db.collection("usernames").document(username).set(usernameData, SetOptions.merge()));

        auth.signOut();
    }

    @Test
    public void testSearchUser() {
        signInUser(USER_A_EMAIL, USER_A_PASSWORD);
        SystemClock.sleep(3000);

        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterSearchEditText)).perform(replaceText(USER_B_USERNAME));
        SystemClock.sleep(1500);

        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(0, click()));
        SystemClock.sleep(1500);

        onView(withText(containsString(USER_B_USERNAME))).check(matches(isDisplayed()));
        onView(withId(R.id.followButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchNoResults() {
        signInUser(USER_A_EMAIL, USER_A_PASSWORD);
        SystemClock.sleep(3000);

        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterSearchEditText)).perform(replaceText(INVALID_USERNAME));
        SystemClock.sleep(1500);

        // No results
        onView(withId(R.id.moodRecyclerView))
                .check((view, noViewFoundException) -> {
                    assertNotNull("RecyclerView is null", view);
                    RecyclerView recyclerView = (RecyclerView) view;
                    int itemCount = recyclerView.getAdapter().getItemCount();
                    assertEquals(0, itemCount);
                });
    }

    @Test
    public void testSearchMultipleResults() {
        signInUser(USER_A_EMAIL, USER_A_PASSWORD);
        SystemClock.sleep(3000);

        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterSearchEditText)).perform(replaceText("searchusertest"));
        SystemClock.sleep(1500);

        onView(withText(containsString(USER_B_USERNAME))).check(matches(isDisplayed()));
        onView(withText(containsString(USER_C_USERNAME))).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchCaseInsensitive() {
        signInUser(USER_A_EMAIL, USER_A_PASSWORD);
        SystemClock.sleep(3000);

        onView(withId(R.id.searchUser)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterSearchEditText)).perform(replaceText(USER_B_USERNAME.toUpperCase()));
        SystemClock.sleep(1500);

        onView(withText(containsString(USER_B_USERNAME))).check(matches(isDisplayed()));
    }

    private void signInUser(String email, String password) {
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.emailSignIn)).perform(replaceText(email));
        onView(withId(R.id.passwordSignIn)).perform(replaceText(password));
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
                FirebaseAuth.getInstance().getCurrentUser() != null);
        SystemClock.sleep(1000);
    }

    @After
    public void tearDown() {
        FirebaseAuth.getInstance().signOut();
    }
}
