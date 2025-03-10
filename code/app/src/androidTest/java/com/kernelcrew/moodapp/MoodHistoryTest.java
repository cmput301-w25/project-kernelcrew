package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.junit.Assert.*;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class MoodHistoryTest extends FirebaseEmulatorMixin {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException {
        staticCreateUser();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        MoodEvent mood1 = new MoodEvent(
                auth.getCurrentUser().getUid(), Emotion.HAPPINESS, "Trigger_test1",
                "social_test1", "reason_test1", "no photo", 0.0, 0.0
        );

        MoodEvent mood2 = new MoodEvent(
                auth.getCurrentUser().getUid(), Emotion.DISGUST, "Trigger_test1",
                "social_test1", "reason_test1", "no photo", 0.0, 0.0
        );

        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(mood1));
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(mood2));
    }


    @Test
    public void checkIfMoodEventsAreLoaded() throws InterruptedException {
        onView(withId(R.id.page_myHistory)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.recyclerViewMoodHistory))
                .check((view, noViewFoundException) -> {
                    if (noViewFoundException != null) {
                        throw noViewFoundException;
                    }

                    RecyclerView recyclerView = (RecyclerView) view;
                    RecyclerView.Adapter adapter = recyclerView.getAdapter();

                    assertNotNull("Adapter should not be null", adapter);
                    assertTrue("RecyclerView should have at least one item", adapter.getItemCount() == 2);
                });
    }


    @Test
    public void checkIfClickingMoodEventsWillNavigateToDetailsPage() throws InterruptedException {
        onView(withId(R.id.page_myHistory)).perform(click());

        onView(withId(R.id.recyclerViewMoodHistory))
                .perform(actionOnItemAtPosition(0, click() ));

        Thread.sleep(1000);

        onView(ViewMatchers.withId(R.id.moodDetailsToolbar))
                .check(matches(isDisplayed()));
    }

}
