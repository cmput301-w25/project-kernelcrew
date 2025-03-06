package com.kernelcrew.moodapp;

// Code from Anthropic, Claude 3.7 Sonnet, "Create simplified Android UI tests following Occam's razor principle for location functionality", accessed 05-12-2024

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.kernelcrew.moodapp.ui.MainActivity;

/**
 * UI tests for the location functionality in the MoodApp.
 * <p>
 * This test class demonstrates a minimal, focused approach to UI testing,
 * following the Occam's razor principle: "Entities should not be multiplied
 * unnecessarily." Only the essential, most stable tests are included to
 * ensure long-term test reliability.
 * <p>
 * The test focuses on checking basic UI element visibility for location-related
 * features, without relying on complex asynchronous operations or system interactions
 * that tend to make tests brittle.
 *
 * @author Anthropic, Claude 3.7 Sonnet
 * @version 1.0
 * @see com.kernelcrew.moodapp.ui.LocationFragment
 */
@RunWith(AndroidJUnit4.class)
public class LocationFragmentUITest {

    /**
     * Rule to launch the main activity before each test.
     * <p>
     * ActivityScenarioRule handles setting up and tearing down the activity
     * lifecycle automatically for each test method.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Tests that the location request button is visible to the user.
     * <p>
     * This test verifies the basic UI element visibility, which is a fundamental
     * requirement for the location functionality to be accessible to the user.
     * The test passes if the button is displayed on the screen.
     */
    @Test
    public void testLocationButtonVisibility() {
        // Verify button is displayed
        onView(withId(R.id.request_location_button))
            .check(matches(isDisplayed()));
    }
}