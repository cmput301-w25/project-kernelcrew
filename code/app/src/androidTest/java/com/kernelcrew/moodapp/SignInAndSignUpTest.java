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
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.ui.MainActivity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignInAndSignUpTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setUp() throws InterruptedException, ExecutionException {
        staticCreateUser();
        FirebaseAuth.getInstance().signOut();
    }

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

    @Test
    public void testSignUpInputValidation() {
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
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
        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Please enter a valid email."))));
    }

    @Test
    public void testWeakPasswordSignUp() {
        clearUser();
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.username)).perform(replaceText("TestUser"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("testuser@example.com"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("123"));
        onView(withId(R.id.signUpButtonAuthToHome)).perform(click());
        onView(withId(R.id.passwordLayout))
                .check(matches(hasDescendant(withText("Password must be at least 6 characters."))));
    }
    
    @Test
    public void testSignInInputValidation() {
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.signInButtonAuthToHome)).perform(click());
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
        onView(withId(R.id.buttonInitialToSignIn)).perform(click());
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
        onView(withText(R.string.welcome_back)).check(matches(isDisplayed()));
        onView(withId(R.id.emailSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.errorTextSignIn))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.catImage)).check(matches(isDisplayed()));
        onView(withId(R.id.emailSignIn)).perform(replaceText("user@example.com"));
        onView(withId(R.id.passwordSignIn)).perform(replaceText("secret123"));
        onView(withId(R.id.emailSignIn)).check(matches(withText("user@example.com")));
        onView(withId(R.id.passwordSignIn)).check(matches(withText("secret123")));
        onView(withContentDescription("Show password"))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Test
    public void testSignUpUIElementsAndInput() {
        onView(withId(R.id.buttonInitialToSignUp)).perform(click());
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
        onView(withText(R.string.create_an_account)).check(matches(isDisplayed()));
        onView(withId(R.id.username)).check(matches(isDisplayed()));
        onView(withId(R.id.emailSignUp)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordSignUp)).check(matches(isDisplayed()));
        onView(withId(R.id.errorTextSignUp))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.catImage)).check(matches(isDisplayed()));
        onView(withId(R.id.username)).perform(replaceText("NewUser"));
        onView(withId(R.id.emailSignUp)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordSignUp)).perform(replaceText("newpassword"));
        onView(withId(R.id.username)).check(matches(withText("NewUser")));
        onView(withId(R.id.emailSignUp)).check(matches(withText("newuser@example.com")));
        onView(withId(R.id.passwordSignUp)).check(matches(withText("newpassword")));
        onView(withContentDescription("Show password"))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}
