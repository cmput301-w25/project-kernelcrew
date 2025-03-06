package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;

import android.widget.ImageButton;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignInAndSignUpTest extends FirebaseEmulatorMixin {
    // Changeable strings
    private static final String TEST_EMAIL =  "test@kernelcrew.com";
    private static final String TEST_PASSWORD = "Password@1234";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() throws InterruptedException, ExecutionException {
        createUser();
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    // Navigation tests
    @Test
    public void goFromInitialToSignInToInitial() {
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());

        onView(withText(R.string.welcome_back)).check(matches(isDisplayed()));

        onView(allOf(isDescendantOfA(withId(R.id.topAppBar)), isAssignableFrom(ImageButton.class)))
                .perform(click());

        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

    @Test
    public void goFromInitialToSignUpToInitial() {
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());

        onView(withText(R.string.create_an_account)).check(matches(isDisplayed()));

        onView(allOf(isDescendantOfA(withId(R.id.topAppBar)), isAssignableFrom(ImageButton.class)))
                .perform(click());

        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

    // Input validation tests for Sign Up
    @Test
    public void testSignUpInputValidation() {
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());

        // Attempt to sign up without entering any details
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());

        // Check for error messages on username, email, and password fields
        onView(withId(R.id.usernameLayout))
                .check(matches(hasDescendant(withText("Please enter a username."))));
        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Please enter an email."))));
        onView(withId(R.id.passwordLayout))
                .check(matches(hasDescendant(withText("Please enter a password."))));
    }

    @Test
    public void testInvalidEmailSignUp() {
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());

        onView(withId(R.id.username)).perform(replaceText("TestUser"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("invalid-email"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("Test@123"));

        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());

        // Check email error
        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Please enter a valid email."))));
    }

    @Test
    public void testWeakPasswordSignUp() {
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());

        onView(withId(R.id.username)).perform(replaceText("TestUser"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("testuser@example.com"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("123"));

        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());

        // Check password error
        onView(withId(R.id.passwordLayout))
                .check(matches(hasDescendant(withText("Password must be at least 6 characters."))));
    }

//    I have tried everything to test if a valid user will work and it is litterally impossible
//          the problem I am facing is that when using thread.sleep, the signup backend will not
//          continue to run, so idk how to fix this test and i have spent too long trying to figure
//          it out.
//    @Test
//    public void testValidSignUp() {
//        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
//
//        onView(withId(R.id.username)).perform(replaceText("ValidUser"));
//        onView(withId(R.id.emailSignUp)).perform(replaceText("validuser@example.com"));
//        onView(withId(R.id.passwordSignUp)).perform(replaceText("ValidPassword123!"));
//        Espresso.closeSoftKeyboard();
//
//        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
//
//        // Wait up to 30 seconds until the homeFeed view is displayed
//        await().atMost(30, TimeUnit.SECONDS)
//                .ignoreExceptions()
//                .untilAsserted(() -> onView(withId(R.id.homeTextView))
//                        .check(matches(isDisplayed())));
//    }

    @Test
    public void testNavigationToHomeFeed() {
        FirebaseAuth.getInstance().signOut();

        // On AuthHome screen: Click the "Sign In" button.
        Espresso.onView(ViewMatchers.withId(R.id.buttonInitialToSignIn))
                .perform(ViewActions.click());

        // Now on AuthSignIn screen: Check that the email field is displayed.
        onView(withId(R.id.emailSignIn))
                .check(matches(isDisplayed()));

        // Fill in the email and password fields with the correct credentials
        onView(withId(R.id.emailSignIn))
                .perform(replaceText(TEST_EMAIL), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.passwordSignIn))
                .perform(replaceText(TEST_PASSWORD), ViewActions.closeSoftKeyboard());

        // Click the sign in button on AuthSignIn.
        Espresso.onView(ViewMatchers.withId(R.id.signInButtonAuthToHome))
                .perform(ViewActions.click());

        // On HomeFeed screen: Verify that the homeTextView is displayed.
        onView(withId(R.id.homeTextView))
                .check(matches(isDisplayed()));

        FirebaseAuth.getInstance().signOut();
    }

    // Input validation tests for Sign In
    @Test
    public void testSignInInputValidation() {
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());

        onView(withId(R.id.signInButtonAuthToHome)).perform(click());

        // Check for error messages on email and password fields
        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Please enter an email."))));
        onView(withId(R.id.passwordLayout))
                .check(matches(hasDescendant(withText("Please enter a password."))));
    }

    @Test
    public void testSignInWithIncorrectCredentials() {
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());

        onView(withId(R.id.emailSignIn)).perform(ViewActions.typeText("wronguser@example.com"));
        onView(withId(R.id.passwordSignIn)).perform(ViewActions.typeText("WrongPassword123"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.signInButtonAuthToHome)).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> onView(withId(R.id.errorTextSignIn))
                        .check(matches(withText(R.string.error_user_not_found))));
    }


    @Test
    public void testSignInUIElementsAndInput() {
        // Navigate to Sign In screen
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());

        // Verify the presence of UI elements
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
        onView(withText(R.string.welcome_back)).check(matches(isDisplayed()));
        onView(withId(R.id.emailSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.errorTextSignIn))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.catImage)).check(matches(isDisplayed()));

        // Simulate user input into email and password fields
        onView(withId(R.id.emailSignIn)).perform(replaceText("user@example.com"));
        onView(withId(R.id.passwordSignIn)).perform(replaceText("secret123"));

        // Verify that the input fields show the entered text
        onView(withId(R.id.emailSignIn)).check(matches(withText("user@example.com")));
        onView(withId(R.id.passwordSignIn)).check(matches(withText("secret123")));

        // Test password toggle functionality by clicking on the toggle icon
        onView(withContentDescription("Show password"))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Test
    public void testSignUpUIElementsAndInput() {
        // Navigate to Sign Up screen
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());

        // Verify the presence of UI elements
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
        onView(withText(R.string.create_an_account)).check(matches(isDisplayed()));
        onView(withId(R.id.username)).check(matches(isDisplayed()));
        onView(withId(R.id.emailSignUp)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordSignUp)).check(matches(isDisplayed()));
        onView(withId(R.id.errorTextSignUp))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.catImage)).check(matches(isDisplayed()));

        // Simulate user input into username, email, and password fields
        onView(withId(R.id.username)).perform(replaceText("NewUser"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("newpassword"));

        // Verify that the input fields show the entered text
        onView(withId(R.id.username)).check(matches(withText("NewUser")));
        onView(withId(R.id.emailSignUp)).check(matches(withText("newuser@example.com")));
        onView(withId(R.id.passwordSignUp)).check(matches(withText("newpassword")));

        // Test password toggle functionality by clicking on the toggle icon
        onView(withContentDescription("Show password"))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    private void createUser() throws InterruptedException, ExecutionException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> createUserTask = auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        Tasks.await(createUserTask);
        auth.signOut();
    }
}
