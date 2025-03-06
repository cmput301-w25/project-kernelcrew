//Code from OpenAi, ChatGPT, "Build location fragment using google maps API in java in android studio requesting the user for permission to get their location and handling error gracefully if location can't be reached", accessed 03-02-2025
//Modified by Anthropic, Claude 3.7 Sonnet, "Fix LocationFragment to properly handle location permissions and services", accessed 05-12-2024

package com.kernelcrew.moodapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.kernelcrew.moodapp.R;

/**
 * Fragment responsible for handling location functionality in the MoodApp.
 * <p>
 * This fragment provides functionality to request and handle location permissions,
 * retrieve current device location using the FusedLocationProviderClient, and
 * save mood events with or without location data depending on permission status.
 * <p>
 * The fragment handles different scenarios:
 * - Location permissions granted: retrieves location and saves mood with location
 * - Location permissions denied: saves mood without location data
 * - Location services disabled: alerts user and saves mood without location
 *
 * @author OpenAI, ChatGPT (base implementation)
 * @author Anthropic, Claude 3.7 Sonnet (enhancements and fixes)
 * @version 1.0
 */
public class LocationFragment extends Fragment {
    /**
     * Request code used for permission requests.
     * Used to identify the permission request in onRequestPermissionsResult.
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    /**
     * Main client for interacting with the fused location provider.
     * Provides access to device location with the appropriate permissions.
     */
    private FusedLocationProviderClient fusedLocationClient;
    
    /**
     * Button for requesting location access.
     * When clicked, initiates the location permission flow.
     */
    private Button requestLocationButton;
    
    /**
     * Callback for receiving location updates from the location client.
     * Processes incoming location results.
     */
    private LocationCallback locationCallback;
    
    /**
     * Configuration for the location request.
     * Specifies parameters such as accuracy, update interval, etc.
     */
    private LocationRequest locationRequest;

    /**
     * Stores the retrieved latitude coordinate.
     * Null if location hasn't been obtained yet.
     */
    private Double latitude = null;
    
    /**
     * Stores the retrieved longitude coordinate.
     * Null if location hasn't been obtained yet.
     */
    private Double longitude = null;

    // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
    // Flag to track if we've received any real location data
    private boolean realLocationReceived = false;
    
    // Timeout values for location fetching
    private static final int LOCATION_REQUEST_TIMEOUT = 10000; // 10 seconds

    /**
     * Creates the fragment view and initializes location services.
     * Sets up the location button, location client, and necessary callbacks.
     *
     * @param inflater The LayoutInflater to inflate views
     * @param container The parent view container
     * @param savedInstanceState Previously saved state
     * @return The inflated and configured fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Code from Claude AI, Anthropic, "Fix location API initialization", accessed 03-07-2024
        try {
            // Initialize the fusedLocationClient with the activity context
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            Log.i("LocationFragment", "FusedLocationProviderClient initialized successfully");
        } catch (Exception e) {
            Log.e("LocationFragment", "Error initializing FusedLocationProviderClient: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing location services: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        requestLocationButton = view.findViewById(R.id.request_location_button);

        // Code from Claude AI, Anthropic, "Fix location API initialization", accessed 03-07-2024
        // Create a high accuracy request
        try {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                    .setIntervalMillis(500)    // Update interval of 500ms
                    .setMinUpdateIntervalMillis(100)  // Minimum of 100ms
                    .setMaxUpdateDelayMillis(1000)    // Maximum delay of 1s
                    .setWaitForAccurateLocation(true) // Wait for accuracy
                    .build();
            Log.i("LocationFragment", "Location request configured with high accuracy");
        } catch (Exception e) {
            Log.e("LocationFragment", "Error creating location request: " + e.getMessage(), e);
        }

        // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
        Log.i("LocationFragment", "Fragment created, location client initialized");

        // Initialize location callback
        locationCallback = new LocationCallback() {
            /**
             * Called when new location data is available.
             * Processes the location result and saves the mood event with location.
             *
             * @param locationResult The location result containing the latest location data
             */
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
                Log.i("LocationFragment", "Location callback triggered");
                
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
                    // Mark that we've received a real location
                    realLocationReceived = true;
                    
                    // Code from Claude AI, Anthropic, "Log detailed location info", accessed 03-07-2024
                    Log.i("LocationFragment", "LOCATION_UPDATE_DATA: Provider=" + location.getProvider() 
                        + ", Lat=" + location.getLatitude() 
                        + ", Lng=" + location.getLongitude()
                        + ", Accuracy=" + location.getAccuracy()
                        + ", Time=" + location.getTime()
                        + ", Altitude=" + location.getAltitude()
                        + ", Speed=" + location.getSpeed());
                    
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    // Code from Claude AI, Anthropic, "Add location logging to terminal", accessed 03-07-2024
                    Log.i("LocationFragment", "Location retrieved - Latitude: " + latitude + ", Longitude: " + longitude);
                    saveMoodEventWithLocation(latitude, longitude, false);
                    stopLocationUpdates();
                } else {
                    // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
                    Log.i("LocationFragment", "Location callback returned null location");
                    // No location available from updates - don't simulate here, let the timeout handle it
                    if (!realLocationReceived) {
                        Log.i("LocationFragment", "Waiting for timeout before simulating location");
                    }
                }
            }
        };

        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        requestLocationButton.setOnClickListener(v -> {
            Log.i("LocationFragment", "Location button clicked");
            Toast.makeText(requireContext(), "Requesting location permission...", Toast.LENGTH_SHORT).show();
            checkLocationAndRequestPermission();
        });
        
        // Code from Claude AI, Anthropic, "Verify location getters work correctly", accessed 03-07-2024
        // Set up long-press gesture for testing location simulation
        setupLocationTestingFeatures();
        
        // Code from Claude AI, Anthropic, "Fix location API initialization", accessed 03-07-2024
        // Add a direct test call if we already have permission
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, test API directly
            testDirectLocationCall();
        }
        
        // Check if we already have permission when the fragment is created
        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        Log.i("LocationFragment", "Checking initial permission state");
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("LocationFragment", "Permission already granted on startup");
            requestLocationButton.setText("Update Location");
        } else {
            Log.i("LocationFragment", "Permission not granted yet, waiting for user to request");
            requestLocationButton.setText("Request Location Permission");
        }

        return view;
    }

    /**
     * Checks if location services are enabled before requesting permission.
     * If services are enabled, requests permission; otherwise shows a dialog.
     */
    private void checkLocationAndRequestPermission() {
        // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
        Log.i("LocationFragment", "Checking location services and requesting permission");
        
        if (isLocationEnabled()) {
            // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
            Log.i("LocationFragment", "Location services are enabled");
            // Using the old-style permission request to ensure dialog appears
            requestLocationPermissionOldStyle();
        } else {
            // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
            Log.i("LocationFragment", "Location services are disabled");
            handleLocationServicesDisabled();
        }
    }

    /**
     * Checks if location services (GPS or network) are enabled on the device.
     *
     * @return true if location services are enabled, false otherwise
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && 
               (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    /**
     * Handles the case when location services are disabled on the device.
     * Shows a toast message, saves the mood event without location,
     * and offers the user an option to enable location services.
     */
    private void handleLocationServicesDisabled() {
        Toast.makeText(getContext(), "Location services are disabled. Saving mood event without location.", Toast.LENGTH_LONG).show();
        saveMoodEventWithoutLocation("Location services are disabled. Saving mood event without location.");
        
        // Show a dialog to let the user enable location services
        new AlertDialog.Builder(requireContext())
            .setTitle("Location Services Disabled")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Requests location permission using the traditional requestPermissions approach.
     * This method is more reliable for showing the system permission dialog.
     */
    private void requestLocationPermissionOldStyle() {
        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        Log.i("LocationFragment", "Requesting location permission (old style)");
        
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
            Log.i("LocationFragment", "Permission not granted, showing permission dialog");
            Toast.makeText(requireContext(), "Please grant location permission in the popup", Toast.LENGTH_LONG).show();
            
            try {
                // This will show the standard permission dialog
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
                );
                Log.i("LocationFragment", "Permission request sent to system");
            } catch (Exception e) {
                Log.e("LocationFragment", "Error requesting permissions: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Error requesting permissions: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
            Log.i("LocationFragment", "Permission already granted");
            // Already have permission
            startLocationUpdates();
        }
    }

    /**
     * Handles the result of a permission request.
     * Starts location updates if permission is granted; otherwise, saves mood without location.
     *
     * @param requestCode The request code passed in requestPermissions
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        Log.i("LocationFragment", "Permission result received: requestCode=" + requestCode);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
                Log.i("LocationFragment", "Permission granted by user");
                Toast.makeText(requireContext(), "Location permission granted!", Toast.LENGTH_SHORT).show();
                requestLocationButton.setText("Update Location");
                // Permission granted
                startLocationUpdates();
            } else {
                // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
                Log.i("LocationFragment", "Permission denied by user");
                Toast.makeText(requireContext(), "Location permission denied. Using simulated location.", Toast.LENGTH_LONG).show();
                // Permission denied
                saveMoodEventWithoutLocation("Location permission denied. Saving mood event without location.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Starts receiving location updates if permission is granted.
     * Shows a toast message to confirm that permission is granted.
     * Requires ACCESS_FINE_LOCATION permission.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Code from Claude AI, Anthropic, "Fix location API not being called", accessed 03-07-2024
        Log.i("LocationFragment", "Starting location updates");
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission granted, getting your location...", Toast.LENGTH_SHORT).show();
            
            // Reset the flag when starting location updates
            // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
            realLocationReceived = false;
            
            // Code from Claude AI, Anthropic, "Fix location API not being called", accessed 03-07-2024
            Log.i("LocationFragment", "Reconfiguring location request with highest priority");
            
            // Configure a high-priority request that will force API calls
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(100)  // 100ms
                .setMaxUpdateDelayMillis(1000)    // 1s
                .setIntervalMillis(500)           // 500ms
                .build();
            
            // Only setup timeout for simulation if no location is found
            // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
            final android.os.Handler timeoutHandler = new android.os.Handler(Looper.getMainLooper());
            final Runnable timeoutRunnable = () -> {
                if (!realLocationReceived) {
                    Log.i("LocationFragment", "Location timeout reached after " + LOCATION_REQUEST_TIMEOUT + "ms");
                    simulateLocation();
                }
            };
            
            // Set timeout for location request
            timeoutHandler.postDelayed(timeoutRunnable, LOCATION_REQUEST_TIMEOUT);
            
            // Code from Claude AI, Anthropic, "Fix location API not being called", accessed 03-07-2024
            Log.i("LocationFragment", "Requesting location updates with aggressive settings");
            
            try {
                // Try to get immediate current location first
                // Code from Claude AI, Anthropic, "Fix location API not being called", accessed 03-07-2024
                com.google.android.gms.tasks.Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null);
                    
                currentLocationTask.addOnSuccessListener(location -> {
                    Log.i("LocationFragment", "getCurrentLocation API call succeeded");
                    if (location != null) {
                        // Process location
                        realLocationReceived = true;
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        Log.i("LocationFragment", "DIRECT_LOCATION_DATA: Provider=" + location.getProvider() 
                            + ", Lat=" + location.getLatitude() 
                            + ", Lng=" + location.getLongitude()
                            + ", Accuracy=" + location.getAccuracy()
                            + ", Time=" + location.getTime());
                            
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        saveMoodEventWithLocation(latitude, longitude, false);
                    } else {
                        Log.i("LocationFragment", "getCurrentLocation returned null, waiting for updates");
                    }
                });
                
                currentLocationTask.addOnFailureListener(e -> {
                    Log.e("LocationFragment", "getCurrentLocation failed: " + e.getMessage(), e);
                });
                
                // Also request continuous updates as a backup
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
                        
                Log.i("LocationFragment", "Successfully requested location updates");
                
            } catch (Exception e) {
                Log.e("LocationFragment", "Error starting location updates: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Location error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // If we can't get updates, simulate after a short delay
                new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!realLocationReceived) {
                        simulateLocation();
                    }
                }, 2000);
            }
            
            // Also try the standard getLastLocation as a fallback
            try {
                // Code from Claude AI, Anthropic, "Get last known location directly", accessed 03-07-2024
                Log.i("LocationFragment", "Also requesting last known location");
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        // Code from Claude AI, Anthropic, "Get last known location directly", accessed 03-07-2024
                        if (location != null) {
                            // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
                            // Mark that we've received a real location
                            realLocationReceived = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            
                            Log.i("LocationFragment", "Last known location retrieved directly:");
                            Log.i("LocationFragment", "LOCATION_DATA: Provider=" + location.getProvider() 
                                + ", Lat=" + location.getLatitude() 
                                + ", Lng=" + location.getLongitude()
                                + ", Accuracy=" + location.getAccuracy()
                                + ", Time=" + location.getTime()
                                + ", Altitude=" + location.getAltitude()
                                + ", Speed=" + location.getSpeed());
                            
                            // Use this location directly
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            saveMoodEventWithLocation(latitude, longitude, false);
                        } else {
                            Log.i("LocationFragment", "Last known location is null, waiting for updates");
                            // We'll wait for the timeout to trigger simulation if needed
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LocationFragment", "Error getting last location: " + e.getMessage());
                        // Don't simulate immediately, let the timeout handle it
                    });
            } catch (Exception e) {
                Log.e("LocationFragment", "Error getting last location: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Simulates a location with hard-coded coordinates.
     * This is useful for testing or when real location services are unavailable.
     *
     * @author Anthropic, Claude 3.7 Sonnet
     */
    // Code from Claude AI, Anthropic, "Simulate location if real location not available", accessed 03-07-2024
    private void simulateLocation() {
        // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
        // Only simulate if we haven't already received real location
        if (realLocationReceived) {
            Log.i("LocationFragment", "Skipping simulation as real location was already received");
            return;
        }
        
        Log.i("LocationFragment", "Simulating location with hard-coded coordinates");
        
        // Edmonton coordinates (University of Alberta)
        double simulatedLatitude = 53.5232;
        double simulatedLongitude = -113.5263;
        
        // Print the simulated location
        Log.i("LocationFragment", "SIMULATED_LOCATION: Lat=" + simulatedLatitude + ", Lng=" + simulatedLongitude);
        
        // Store the simulated coordinates
        latitude = simulatedLatitude;
        longitude = simulatedLongitude;
        
        // Code from Claude AI, Anthropic, "Verify location getters work correctly", accessed 03-07-2024
        // Test to ensure location getters are working correctly
        verifyLocationGetters(simulatedLatitude, simulatedLongitude);
        
        // Save the mood event with simulated location
        saveMoodEventWithLocation(simulatedLatitude, simulatedLongitude, true);
    }
    
    /**
     * Directly simulates a location with specified coordinates and tests the getters.
     * This is useful for immediate testing of location functionality.
     *
     * @param testLatitude Latitude value to simulate
     * @param testLongitude Longitude value to simulate
     */
    // Code from Claude AI, Anthropic, "Verify location getters work correctly", accessed 03-07-2024
    public void simulateDirectLocationTest(double testLatitude, double testLongitude) {
        // Set the coordinates
        latitude = testLatitude;
        longitude = testLongitude;
        
        // Log the successful simulation
        Log.i("LocationFragment", "DIRECT_SIMULATION: Set coordinates to Lat=" + testLatitude + ", Lng=" + testLongitude);
        
        // Verify getters work
        verifyLocationGetters(testLatitude, testLongitude);
        
        // Display success message to user
        Toast.makeText(requireContext(), 
                "Location simulated: " + testLatitude + ", " + testLongitude, 
                Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Verifies that the location getters correctly return the stored latitude and longitude values.
     * Logs any discrepancies found during verification.
     *
     * @param expectedLatitude The expected latitude value
     * @param expectedLongitude The expected longitude value
     * @return true if getters return expected values, false otherwise
     */
    // Code from Claude AI, Anthropic, "Verify location getters work correctly", accessed 03-07-2024
    private boolean verifyLocationGetters(double expectedLatitude, double expectedLongitude) {
        // Get values using getters
        Double retrievedLatitude = getLatitude();
        Double retrievedLongitude = getLongitude();
        
        // Check if values match
        boolean latitudeMatches = (retrievedLatitude != null && 
                                  Math.abs(retrievedLatitude - expectedLatitude) < 0.0001);
        boolean longitudeMatches = (retrievedLongitude != null && 
                                   Math.abs(retrievedLongitude - expectedLongitude) < 0.0001);
        
        // Log verification results
        Log.i("LocationFragment", "GETTER_VERIFICATION: Latitude: expected=" + expectedLatitude + 
              ", retrieved=" + retrievedLatitude + ", matches=" + latitudeMatches);
        Log.i("LocationFragment", "GETTER_VERIFICATION: Longitude: expected=" + expectedLongitude + 
              ", retrieved=" + retrievedLongitude + ", matches=" + longitudeMatches);
        
        // Notify of any issues
        if (!latitudeMatches || !longitudeMatches) {
            Log.e("LocationFragment", "GETTER_ERROR: Location getters not returning expected values!");
            
            if (!latitudeMatches) {
                Log.e("LocationFragment", "GETTER_ERROR: Latitude mismatch! Expected: " + 
                      expectedLatitude + ", Got: " + retrievedLatitude);
            }
            
            if (!longitudeMatches) {
                Log.e("LocationFragment", "GETTER_ERROR: Longitude mismatch! Expected: " + 
                      expectedLongitude + ", Got: " + retrievedLongitude);
            }
            
            return false;
        }
        
        Log.i("LocationFragment", "GETTER_VERIFICATION: Success! Getters working correctly");
        return true;
    }
    
    /**
     * Add a button long click listener to directly test location simulation
     * with different coordinates each time.
     */
    // Code from Claude AI, Anthropic, "Verify location getters work correctly", accessed 03-07-2024
    private void setupLocationTestingFeatures() {
        requestLocationButton.setOnLongClickListener(v -> {
            // Generate slightly random coordinates around Edmonton for testing variety
            double testLat = 53.5232 + (Math.random() - 0.5) * 0.01; // Random deviation ±0.005
            double testLng = -113.5263 + (Math.random() - 0.5) * 0.01; // Random deviation ±0.005
            
            // Run the simulation with these coordinates
            simulateDirectLocationTest(testLat, testLng);
            
            // Return true to indicate we handled the long click
            return true;
        });
    }

    /**
     * Stops receiving location updates.
     * Called when location is successfully obtained or when the fragment is destroyed.
     */
    private void stopLocationUpdates() {
        // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
        Log.i("LocationFragment", "Stopping location updates");
        
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Cleans up resources when the fragment's view is destroyed.
     * Ensures location updates are stopped to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
    }

    /**
     * Saves a mood event without location data.
     * Used when location permission is denied or location services are disabled.
     *
     * @param message Custom message to display as toast
     */
    public void saveMoodEventWithoutLocation(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // TODO: Implement saving logic here
    }

    /**
     * Saves a mood event without location data using a default message.
     * Convenience method when a custom message is not needed.
     */
    public void saveMoodEventWithoutLocation() {
        saveMoodEventWithoutLocation("Location permission denied. Saving mood event without location.");
    }

    /**
     * Saves a mood event with location data.
     * Used when location permission is granted and location is successfully obtained.
     *
     * @param lat Latitude of the user's location
     * @param lon Longitude of the user's location
     * @param isSimulated Whether this is simulated location data
     */
    // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
    private void saveMoodEventWithLocation(double lat, double lon, boolean isSimulated) {
        String locationSource = isSimulated ? "SIMULATED" : "REAL";
        Log.i("LocationFragment", "Saving mood with " + locationSource + " location - Latitude: " + lat + ", Longitude: " + lon);
        
        String toastMessage = isSimulated 
            ? "Using simulated location (no GPS fix available)"
            : "Mood event saved with real location";
            
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
        // TODO: Implement saving logic here
    }

    // Add overloaded method for backward compatibility
    // Code from Claude AI, Anthropic, "Fix location priority to favor real locations", accessed 03-07-2024
    private void saveMoodEventWithLocation(double lat, double lon) {
        saveMoodEventWithLocation(lat, lon, false);
    }

    /**
     * Gets the current latitude value.
     *
     * @return The latitude or null if not set
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Gets the current longitude value.
     *
     * @return The longitude or null if not set
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Makes a direct call to the location API to test if it's working properly.
     * This is useful for debugging when the normal flow isn't triggering API calls.
     */
    // Code from Claude AI, Anthropic, "Fix location API initialization", accessed 03-07-2024
    @SuppressLint("MissingPermission")
    private void testDirectLocationCall() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        Log.i("LocationFragment", "Testing direct API call to getCurrentLocation");
        
        try {
            // Make a direct API call to test connectivity
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.i("LocationFragment", "TEST_DIRECT_API: Success, got location");
                        Log.i("LocationFragment", "TEST_LOCATION: Lat=" + location.getLatitude() 
                            + ", Lng=" + location.getLongitude());
                    } else {
                        Log.i("LocationFragment", "TEST_DIRECT_API: Success, but location is null");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LocationFragment", "TEST_DIRECT_API: Failed: " + e.getMessage(), e);
                });
                
            Log.i("LocationFragment", "TEST_DIRECT_API: Request sent");
        } catch (Exception e) {
            Log.e("LocationFragment", "TEST_DIRECT_API: Exception: " + e.getMessage(), e);
        }
    }
}
