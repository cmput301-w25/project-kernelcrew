package com.kernelcrew.moodapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

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

/**
 * Instrumented tests for LocationFragment UI interactions.
 * 
 * This class tests the UI elements and user interactions of the LocationFragment.
 * 
 * @author Claude AI, Anthropic, "Generate comprehensive UI tests for LocationFragment", accessed 10-03-2025
 */
@RunWith(AndroidJUnit4.class)
public class LocationInstrumentedTest extends FirebaseEmulatorMixin {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);
    
    @BeforeClass
    public static void setupDatabase() throws ExecutionException, InterruptedException {
        staticCreateUser();
        
        // Add a mood event without location to be used in tests
        FirebaseAuth auth = FirebaseAuth.getInstance();
        MoodEvent moodEvent = new MoodEvent(
                auth.getCurrentUser().getUid(),
                "Username",
                Emotion.HAPPINESS,
                "Test Social Situation",
                "Test Reason",
                null,
                null
        );
        Tasks.await(MoodEventProvider.getInstance().insertMoodEvent(moodEvent));
    }
    
    /**
     * Test that the location button is displayed and clickable in the Create Mood Event screen.
     */
    @Test
    public void testLocationButtonDisplayed() {
        // Navigate to the Create Mood Event screen
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        
        // Wait for screen to load
        SystemClock.sleep(1000);
        
        // Check that the add location button is displayed in the LocationFragment
        onView(withId(R.id.add_location_button))
                .check(matches(isDisplayed()));
    }
    
    /**
     * Test the click behavior of the location button.
     * Note that actual permission dialog interactions can't be fully tested in instrumented tests.
     */
    @Test
    public void testLocationButtonClick() {
        // Navigate to the Create Mood Event screen
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        
        // Wait for screen to load
        SystemClock.sleep(1000);
        
        // Click the location button
        onView(withId(R.id.add_location_button)).perform(click());
        
        // Wait for permission dialog or any other UI response
        SystemClock.sleep(1000);
        
        // The permission dialog will appear, but we can't interact with it in tests
        // This test just verifies the button is clickable
    }
    
    /**
     * Test submitting a form without location data.
     * This verifies that a mood can be created even without location.
     */
    @Test
    public void testSubmitFormWithoutLocation() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Navigate to the Create Mood Event screen
        onView(withId(R.id.page_createMoodEvent)).perform(click());
        
        // Wait for screen to load
        SystemClock.sleep(1000);
        
        // Select an emotion
        onView(withId(R.id.toggle_happy)).perform(click());

        onView(withId(R.id.emotion_reason)).perform(scrollTo());

        onView(withId(R.id.emotion_reason)).perform(replaceText("Location Test"));
        onView(withId(R.id.emotion_reason)).perform(closeSoftKeyboard());

        // Wait to ensure keyboard is closed
        SystemClock.sleep(500);
        
        // Submit the form without adding location
        onView(withId(R.id.submit_button)).perform(scrollTo(), click());
        
        // Wait for submission to process
        SystemClock.sleep(2000);
        
        // Verify the new mood event was added to Firestore
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    QuerySnapshot results = Tasks.await(db.collection("moodEvents").get());
                    List<DocumentSnapshot> moodEvents = results.getDocuments();
                    
                    // Should have at least 2 mood events (the one from setup and the new one)
                    assertTrue(moodEvents.size() >= 2);
                    
                    // Find the mood event with our test trigger
                    boolean foundTestMood = false;
                    for (DocumentSnapshot doc : moodEvents) {
                        if ("Location Test".equals(doc.getString("reason"))) {
                            foundTestMood = true;
                            
                            // Verify location is null or empty
                            Object lat = doc.get("latitude");
                            Object lon = doc.get("longitude");
                            assertTrue(lat == null || lon == null);
                            
                            break;
                        }
                    }
                    
                    assertTrue("Test mood event not found in Firestore", foundTestMood);
                });
    }
} 