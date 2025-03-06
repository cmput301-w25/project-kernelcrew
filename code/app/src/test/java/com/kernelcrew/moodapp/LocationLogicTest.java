package com.kernelcrew.moodapp;

// Code from Claude AI, Anthropic, "Create unit tests for location handling logic", accessed 03-05-2025
// Code from Claude AI, Anthropic, "Fix ClassNotFoundException in Android unit tests with Robolectric", accessed 03-06-2024

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kernelcrew.moodapp.ui.LocationFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowToast;

/**
 * Unit tests for the location functionality logic.
 * <p>
 * This test class uses Robolectric to test the LocationFragment in isolation,
 * without requiring a device or emulator. It focuses on testing the non-UI logic
 * of the fragment, including the initial state, value handling, and toast messages.
 * <p>
 * The tests use FragmentScenario to properly create and manage fragment instances,
 * ensuring that the fragment's lifecycle is correctly handled during testing.
 *
 * @author Anthropic, Claude 3.7 Sonnet
 * @version 1.0
 * @see com.kernelcrew.moodapp.ui.LocationFragment
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 29)
@LooperMode(LooperMode.Mode.PAUSED)
public class LocationLogicTest {
    
    /**
     * Fragment scenario used to create and manage the LocationFragment instance.
     * <p>
     * FragmentScenario provides a way to test fragments in isolation and control
     * their lifecycle programmatically.
     */
    private FragmentScenario<LocationFragment> fragmentScenario;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * This method initializes a FragmentScenario for the LocationFragment, which
     * creates a fragment instance in a testing environment.
     */
    @Before
    public void setUp() {
        // Create a FragmentScenario for LocationFragment
        fragmentScenario = FragmentScenario.launchInContainer(LocationFragment.class);
    }

    /**
     * Tests that the initial state of the LocationFragment has null location values.
     * <p>
     * This test verifies that the latitude and longitude properties are initially null,
     * which is the expected state before any location is retrieved.
     */
    @Test
    public void testInitialState() {
        fragmentScenario.onFragment(fragment -> {
            assertNull("Initial latitude should be null", fragment.getLatitude());
            assertNull("Initial longitude should be null", fragment.getLongitude());
        });
    }

    /**
     * Tests the saveMoodEventWithoutLocation method with the default message.
     * <p>
     * This test verifies that calling saveMoodEventWithoutLocation:
     * 1. Does not modify the latitude and longitude values (they remain null)
     * 2. Shows the correct toast message to the user
     */
    @Test
    public void testSaveMoodEventWithoutLocation() {
        fragmentScenario.onFragment(fragment -> {
            // Call the method
            fragment.saveMoodEventWithoutLocation();
            
            // Process any pending messages
            shadowOf(Looper.getMainLooper()).idle();
            
            // Verify location values
            assertNull("Latitude should remain null", fragment.getLatitude());
            assertNull("Longitude should remain null", fragment.getLongitude());
            
            // Verify toast message
            assertEquals(
                "Location permission denied. Saving mood event without location.",
                ShadowToast.getTextOfLatestToast()
            );
        });
    }

    /**
     * Tests the saveMoodEventWithoutLocation method with a custom message.
     * <p>
     * This test verifies that calling saveMoodEventWithoutLocation with a custom message:
     * 1. Does not modify the latitude and longitude values (they remain null)
     * 2. Shows the specified custom toast message to the user
     */
    @Test
    public void testSaveMoodEventWithoutLocationCustomMessage() {
        fragmentScenario.onFragment(fragment -> {
            // Call the method with custom message
            String customMessage = "Location services are disabled. Saving mood event without location.";
            fragment.saveMoodEventWithoutLocation(customMessage);
            
            // Process any pending messages
            shadowOf(Looper.getMainLooper()).idle();
            
            // Verify location values
            assertNull("Latitude should remain null", fragment.getLatitude());
            assertNull("Longitude should remain null", fragment.getLongitude());
            
            // Verify toast message
            assertEquals(
                customMessage,
                ShadowToast.getTextOfLatestToast()
            );
        });
    }

    /**
     * Tests the getLatitude and getLongitude accessor methods.
     * <p>
     * This test verifies that the getter methods correctly return the internal
     * latitude and longitude values, which should be null in the initial state.
     */
    @Test
    public void testLocationGetters() {
        fragmentScenario.onFragment(fragment -> {
            // Initial values should be null
            assertNull("Initial latitude should be null", fragment.getLatitude());
            assertNull("Initial longitude should be null", fragment.getLongitude());
        });
    }
} 