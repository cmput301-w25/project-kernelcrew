package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.junit.Assert.*;

import androidx.fragment.app.FragmentContainerView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CreateMoodEventTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void login() throws ExecutionException, InterruptedException {
        staticCreateUser();
    }

    @Test
    public void createNewMood() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        onView(withId(R.id.page_createMoodEvent)).perform(click());
        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.emotion_picker)).check((view, noViewFoundException) -> {
            FragmentContainerView fragmentContainerView = (FragmentContainerView) view;
            EmotionPickerFragment emotionPicker = fragmentContainerView.getFragment();
            assertEquals(Emotion.HAPPINESS, emotionPicker.getSelected());
        });

        onView(withId(R.id.submit_button)).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    assertEquals(1, moodEvents.size());
                    assertEquals("HAPPINESS", moodEvents.get(0).get("emotion"));
                });

        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.submit_button)).perform(click());
        onView(withId(R.id.emotion_picker)).check((view, noViewFoundException) -> {
            FragmentContainerView fragmentContainerView = (FragmentContainerView) view;
            EmotionPickerFragment emotionPicker = fragmentContainerView.getFragment();
            assertEquals("An emotion is required", emotionPicker.getError());
        });
    }
}
