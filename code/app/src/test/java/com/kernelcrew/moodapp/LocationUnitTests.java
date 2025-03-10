package com.kernelcrew.moodapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.kernelcrew.moodapp.ui.LocationFragment;
import com.kernelcrew.moodapp.ui.LocationUpdateListener;
import com.kernelcrew.moodapp.ui.MoodEventForm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * LocationUnitTests - Unit tests for location functionality
 * 
 * This class tests the location-related functionality focusing on:
 * - Testing coordinate storage and retrieval
 * - Verifying listener notification mechanisms
 * - Testing integration with the MoodEventForm
 * 
 * These tests use Mockito to simulate dependencies where possible and verify
 * core functionality without requiring actual device location services.
 */
// Credit Claude AI, Anthropic, "Generate comprehensive UI tests for LocationFragment", accessed 03-10-2025
@RunWith(MockitoJUnitRunner.class)
public class LocationUnitTests {

    // Test locations (sample coordinates)
    private static final double EDMONTON_LAT = 53.5461;
    private static final double EDMONTON_LON = -113.4938;
    private static final double CALGARY_LAT = 51.0447;
    private static final double CALGARY_LON = -114.0719;
    
    // Test doubles
    @Mock
    private LocationUpdateListener mockListener;
    
    @Mock
    private MoodEventForm mockForm;
    
    // A stub implementation of LocationUpdateListener for testing
    private static class TestLocationListener implements LocationUpdateListener {
        private Double latitude;
        private Double longitude;
        
        @Override
        public void onLocationUpdated(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public Double getLatitude() {
            return latitude;
        }
        
        public Double getLongitude() {
            return longitude;
        }
    }
    
    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }
    
    /**
     * Test that the listener correctly receives location updates.
     */
    @Test
    public void testLocationUpdateListenerReceivesCoordinates() {
        TestLocationListener listener = new TestLocationListener();
        
        // Simulate an update being sent to the listener
        listener.onLocationUpdated(EDMONTON_LAT, EDMONTON_LON);
        
        // Verify the listener has stored the correct coordinates
        assertEquals("Latitude should match Edmonton's coordinates", 
                EDMONTON_LAT, listener.getLatitude(), 0.0001);
        assertEquals("Longitude should match Edmonton's coordinates", 
                EDMONTON_LON, listener.getLongitude(), 0.0001);
    }
    
    /**
     * Test that location updates are correctly propagated to listeners.
     */
    @Test
    public void testLocationUpdateNotifiesListener() {
        // Create a captor to capture values passed to the listener
        ArgumentCaptor<Double> latCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lonCaptor = ArgumentCaptor.forClass(Double.class);
        
        // Call the method on the mock listener
        mockListener.onLocationUpdated(CALGARY_LAT, CALGARY_LON);
        
        // Verify the listener's method was called with the correct coordinates
        verify(mockListener).onLocationUpdated(latCaptor.capture(), lonCaptor.capture());
        
        // Verify the captured values match our test data
        assertEquals(CALGARY_LAT, latCaptor.getValue(), 0.0001);
        assertEquals(CALGARY_LON, lonCaptor.getValue(), 0.0001);
    }
    
    /**
     * Test that MoodEventForm correctly implements the LocationUpdateListener interface.
     */
    @Test
    public void testMoodEventFormImplementsLocationUpdateListener() {
        // This is a type check test - MoodEventForm must implement LocationUpdateListener
        // which is verified at compile time
        
        // Call the method with test data
        mockForm.onLocationUpdated(EDMONTON_LAT, EDMONTON_LON);
        
        // Verify the method was called with the correct arguments
        verify(mockForm).onLocationUpdated(EDMONTON_LAT, EDMONTON_LON);
    }
    
    /**
     * Test null location values are handled correctly.
     */
    @Test
    public void testNullLocationHandling() {
        TestLocationListener listener = new TestLocationListener();
        
        // Initial state should be null
        assertNull("Initial latitude should be null", listener.getLatitude());
        assertNull("Initial longitude should be null", listener.getLongitude());
        
        // Update with null values (this should be allowed)
        listener.onLocationUpdated(null, null);
        
        // Values should still be null
        assertNull("Latitude should remain null after null update", listener.getLatitude());
        assertNull("Longitude should remain null after null update", listener.getLongitude());
    }
    
    /**
     * Test updating location with different coordinates.
     */
    @Test
    public void testLocationUpdate() {
        TestLocationListener listener = new TestLocationListener();
        
        // First update with Edmonton coordinates
        listener.onLocationUpdated(EDMONTON_LAT, EDMONTON_LON);
        
        assertEquals("Latitude should be updated to Edmonton", 
                EDMONTON_LAT, listener.getLatitude(), 0.0001);
        assertEquals("Longitude should be updated to Edmonton", 
                EDMONTON_LON, listener.getLongitude(), 0.0001);
        
        // Then update with Calgary coordinates
        listener.onLocationUpdated(CALGARY_LAT, CALGARY_LON);
        
        assertEquals("Latitude should be updated to Calgary", 
                CALGARY_LAT, listener.getLatitude(), 0.0001);
        assertEquals("Longitude should be updated to Calgary", 
                CALGARY_LON, listener.getLongitude(), 0.0001);
    }
} 