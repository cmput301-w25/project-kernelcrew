package com.kernelcrew.moodapp;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.kernelcrew.moodapp.data.LocationHandler;
import com.kernelcrew.moodapp.data.LocationHandler.OnLocationObtainedListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit tests for {@link LocationHandler}.
 */
@RunWith(RobolectricTestRunner.class)
public class LocationHandlerTest {

    @Mock
    private Context mockContext;

    @Mock
    private Activity mockActivity;

    @Mock
    private LocationManager mockLocationManager;

    private LocationHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        handler = new LocationHandler(mockContext);
    }

    /**
     * Test that fetchLocation triggers onLocationFailed when location services are disabled.
     */
    @Test
    public void testFetchLocationServicesDisabled() {
        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
        when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false);
        when(mockLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(false);

        OnLocationObtainedListener listener = mock(OnLocationObtainedListener.class);

        handler.fetchLocation(listener);
        verify(listener).onLocationFailed("Location services are disabled");
    }

    /**
     * Test that fetchLocation triggers onLocationFailed if permission is missing.
     */
    @Test
    public void testFetchLocationMissingPermission() {
        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
        when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
        when(mockLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

        // Mark no permission granted
        when(ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        OnLocationObtainedListener listener = mock(OnLocationObtainedListener.class);
        handler.fetchLocation(listener);
        verify(listener).onLocationFailed("Missing location permission");
    }
}