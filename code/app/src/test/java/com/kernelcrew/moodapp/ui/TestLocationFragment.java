package com.kernelcrew.moodapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kernelcrew.moodapp.R;

/**
 * A test-specific version of LocationFragment that doesn't depend on Google Play Services.
 * This class is used for unit testing purposes only.
 */
public class TestLocationFragment extends LocationFragment {
    
    private Double latitude = null;
    private Double longitude = null;
    private Button requestLocationButton;
    
    /**
     * Override the onCreateView method to avoid initializing FusedLocationProviderClient
     * which causes issues in Robolectric tests.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Do NOT call super.onCreateView() as it would initialize GooglePlay services
        // Instead directly inflate the layout without initializing location services
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        
        Log.i("TestLocationFragment", "Creating test fragment without location services");
        
        // Find the button in the layout
        requestLocationButton = view.findViewById(R.id.request_location_button);
        requestLocationButton.setText("Test Location Button");
        
        // Set up the button click listener
        requestLocationButton.setOnClickListener(v -> {
            Log.i("TestLocationFragment", "Test location button clicked");
            // Directly set some test location values
            saveMoodEventWithLocation(53.5461, -113.4938);
        });
        
        return view;
    }
    
    /**
     * No-op implementation for onAttach to avoid registering ActivityResultLauncher
     */
    @Override
    public void onAttach(@NonNull Context context) {
        // Call the super method from Fragment directly, not from LocationFragment
        super.onAttach(context);
        Log.i("TestLocationFragment", "Test onAttach called - Avoiding permission launcher setup");
    }
    
    /**
     * Override to directly set the location without using Google Play services.
     */
    @Override
    public void saveMoodEventWithLocation(double lat, double lon) {
        Log.i("TestLocationFragment", "Test saving location - Lat: " + lat + ", Lng: " + lon);
        this.latitude = lat;
        this.longitude = lon;
    }
    
    /**
     * Override to return the test latitude.
     */
    @Override
    public Double getLatitude() {
        return latitude;
    }
    
    /**
     * Override to return the test longitude.
     */
    @Override
    public Double getLongitude() {
        return longitude;
    }
    
    /**
     * Since the parent method is private, we need to provide our own implementation
     * instead of trying to override it.
     */
    private void checkLocationAndRequestPermission() {
        Log.i("TestLocationFragment", "Test checkLocationAndRequestPermission() - Using test implementation");
        // For testing, directly set some test coordinates
        saveMoodEventWithLocation(53.5461, -113.4938);
    }
} 