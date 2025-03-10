package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.MainActivity;

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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        MoodEvent moodEvent = new MoodEvent(
                auth.getCurrentUser().getUid(),
                Emotion.DISGUST,
                "",
                "",
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

        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        MoodDetailsNavigationTest.clickChildViewWithId(R.id.viewDetailsButton)));

        onView(withId(R.id.btnEditMood)).perform(click());
        onView(withId(R.id.toggle_shame)).perform(click());
        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    assertEquals(1, moodEvents.size());
                    assertEquals("SHAME", moodEvents.get(0).get("emotion"));
                });
    }
}
