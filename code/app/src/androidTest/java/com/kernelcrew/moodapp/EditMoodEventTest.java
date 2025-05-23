package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

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
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EditMoodEventTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void seedDatabase() throws ExecutionException, InterruptedException, IOException {
        teardownAll();
        staticCreateUser();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        MoodEvent moodEvent = new MoodEvent(
                auth.getCurrentUser().getUid(),
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

        onView(withId(R.id.moodRecyclerView))
                .perform(actionOnItemAtPosition(
                        0,
                        MoodDetailsNavigationTest.clickChildViewWithId(R.id.viewDetailsButton)));

        SystemClock.sleep(3000);
        onView(withId(R.id.btnEditMood)).perform(click());
        SystemClock.sleep(3000);
        onView(withId(R.id.toggle_shame)).perform(click());
        onView(withId(R.id.visible_private_button)).perform(scrollTo()).perform(click());
        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    assertEquals(1, moodEvents.size());
                    MoodEvent moodEvent = moodEvents.get(0).toObject(MoodEvent.class);
                    assertEquals(Emotion.SHAME, moodEvent.getEmotion());
                    assertEquals(MoodEventVisibility.PRIVATE, moodEvent.getVisibility());
                });
    }
}
