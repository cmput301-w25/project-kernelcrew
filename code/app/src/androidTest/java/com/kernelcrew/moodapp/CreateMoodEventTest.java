package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.SystemClock;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.fragment.app.FragmentContainerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventVisibility;
import com.kernelcrew.moodapp.ui.MainActivity;
import com.kernelcrew.moodapp.ui.MoodEventForm;
import com.kernelcrew.moodapp.ui.components.EmotionPickerFragment;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CreateMoodEventTest extends FirebaseEmulatorMixin {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void signupUser() throws ExecutionException, InterruptedException {
        staticCreateUser();
    }

    @Before
    public void signingUser() throws ExecutionException, InterruptedException {
        loginUser();
    }

    @Test
    public void createNewMood() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.toggle_happy)).perform(click());
        onView(withId(R.id.emotion_picker)).check((view, noViewFoundException) -> {
            FragmentContainerView fragmentContainerView = (FragmentContainerView) view;
            EmotionPickerFragment emotionPicker = fragmentContainerView.getFragment();
            assertEquals(Emotion.HAPPINESS, emotionPicker.getSelected());
        });

        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    MoodEvent newEvent = null;
                    for (DocumentSnapshot snapshot : moodEvents) {
                        MoodEvent event = snapshot.toObject(MoodEvent.class);
                        if (event.getEmotion() == Emotion.HAPPINESS) {
                            newEvent = event;
                        }
                    }
                    assertNotNull(newEvent);
                });
    }

    @Test
    public void createMoodVisibilitySelector() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SystemClock.sleep(3000);
        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.toggle_sadness)).perform(click());
        onView(withId(R.id.visible_private_button)).perform(scrollTo()).perform(click());
        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    MoodEvent newEvent = null;
                    for (DocumentSnapshot snapshot : moodEvents) {
                        MoodEvent event = snapshot.toObject(MoodEvent.class);
                        assertNotNull(event);
                        if (event.getEmotion() == Emotion.SADNESS) {
                            newEvent = event;
                        }
                    }
                    assertNotNull(newEvent);
                    assertEquals(MoodEventVisibility.PRIVATE, newEvent.getVisibility());
                });
    }

    @Test
    public void createMoodTimestamp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SystemClock.sleep(3000);
        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.toggle_sadness)).perform(click());

        onView(withId(R.id.timestamp_input)).perform(click());
        onView(ViewMatchers.withText("OK")).perform(click());
        onView(ViewMatchers.withText("OK")).perform(click());

        Date testDate = new Date(2025, 3, 1, 14, 30);
        onView(withId(R.id.mood_event_form)).check((view, noViewFoundException) -> {
            FragmentContainerView fragmentContainerView = (FragmentContainerView) view;
            MoodEventForm moodEventForm = fragmentContainerView.getFragment();
            moodEventForm.setSelectedDate(testDate);
        });

        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    MoodEvent newEvent = null;
                    for (DocumentSnapshot snapshot : moodEvents) {
                        MoodEvent event = snapshot.toObject(MoodEvent.class);
                        assertNotNull(event);
                        if (event.getEmotion() == Emotion.SADNESS) {
                            newEvent = event;
                        }
                    }
                    assertNotNull(newEvent);
                    assertEquals(testDate, newEvent.getCreated());
                });
    }

    @Test
    public void createNewMoodNoEmotionError() {
        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());
        onView(withId(R.id.emotion_picker)).check((view, noViewFoundException) -> {
            FragmentContainerView fragmentContainerView = (FragmentContainerView) view;
            EmotionPickerFragment emotionPicker = fragmentContainerView.getFragment();
            assertEquals("An emotion is required", emotionPicker.getError());
        });
    }

    @Test
    public void createNewMoodReasonTooManyChars() {
        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.toggle_happy)).perform(click());

        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), typeText("This is a really long string with too many characters when will it end. I have to make this stretch until 200 characters which is absurdly long so the text field should be able to handle almost all messages -- except for this one! Just a few more characters to go"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());
        onView(withId(R.id.emotion_reason))
                .check(matches(hasErrorText("Reason must be less than 200 characters")));
    }

    @Test
    public void createNewMoodReasonNotTooLong() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        onView(withId(R.id.page_createMoodEvent)).perform(click());

        onView(withId(R.id.toggle_anger)).perform(click());

        onView(withId(R.id.emotion_reason))
                .perform(scrollTo(), typeText("AAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    assertEquals(1, moodEvents.size());
                    assertEquals("ANGER", moodEvents.get(0).get("emotion"));
                });
    }
}
