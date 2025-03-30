package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.FirebaseEmulatorMixin;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EditMoodEventTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        staticCreateUser();
        loginUser();

        MoodEvent moodEvent = new MoodEvent(
                FirebaseAuth.getInstance().getUid(),
                "Username",
                Emotion.DISGUST,
                "",
                "",
                0.0,
                0.0
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(moodEvent));
    }

    @Test
    public void editNewMood() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Wait until the RecyclerView is displayed.
        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        MoodDetailsNavigationTest.clickChildViewWithId(R.id.viewDetailsButton)));

        SystemClock.sleep(3000);
        onView(withId(R.id.btnEditMood)).perform(click());
        SystemClock.sleep(3000);
        onView(withId(R.id.toggle_shame)).perform(click());
        onView(withText("Private")).perform(click());
        onView(withId(R.id.editMood_submitButton)).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    // Expecting a single mood event document that has been updated.
                    assertEquals(1, moodEvents.size());
                    MoodEvent moodEvent = moodEvents.get(0).toObject(MoodEvent.class);
                    assertEquals(Emotion.SHAME, moodEvent.getEmotion());
                    assertEquals(MoodEventVisibility.PRIVATE, moodEvent.getVisibility());
                });
    }

    /**
     * Utility method that returns a ViewAction to click on a child view with the specified id.
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with id " + id;
            }

            @Override
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null && childView.isClickable()) {
                    childView.performClick();
                } else {
                    throw new PerformException.Builder()
                            .withCause(new Throwable("No clickable view with id " + id))
                            .withViewDescription(view.toString())
                            .build();
                }
            }
        };
    }

    public void setupKeyboardHiding(View view) {
        // No-op for testing
    }
}