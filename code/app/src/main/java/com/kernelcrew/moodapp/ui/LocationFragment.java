//Code from OpenAi, ChatGPT, "Build location fragment using google maps API in java in android studio requesting the user for permission to get their location and handling error gracefully if location can't be reached", accessed 03-02-2025
//Modified by Anthropic, Claude 3.7 Sonnet, "Fix LocationFragment to properly handle location permissions and services", accessed 05-12-2024
//Modified by Anthropic, Claude 3.7 Sonnet, "Update to use modern Activity Result API", accessed 05-13-2024

package com.kernelcrew.moodapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.kernelcrew.moodapp.R;

public class LocationFragment extends Fragment {
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
     * Stores the retrieved latitude coordinate.
     * Null if location hasn't been obtained yet.
     */
    private Double latitude = null;

    /**
     * Stores the retrieved longitude coordinate.
     * Null if location hasn't been obtained yet.
     */
    private Double longitude = null;

    /**
     * Activity result launcher for requesting location permission.
     * Uses the modern Activity Result API instead of onRequestPermissionsResult.
     */
    private ActivityResultLauncher<String> requestPermissionLauncher;

    /**
     * Listener for location updates.
     */
    private LocationUpdateListener updateListener;

    /**
     * Creates the fragment view and initializes location services.
     * Sets up the location button and location client.
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
            // Log the error if location services can't be initialized
            Log.e("LocationFragment", "Error initializing FusedLocationProviderClient: " + e.getMessage(), e);
        }

        // Find the button in the layout
        requestLocationButton = view.findViewById(R.id.add_location_button);

        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        // Set up the button click listener to start the location request process
        requestLocationButton.setOnClickListener(v -> {
            Log.i("LocationFragment", "Location button clicked");
            checkLocationAndRequestPermission();
        });

        // Check if we already have permission when the fragment is created
        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        Log.i("LocationFragment", "Checking initial permission state");
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("LocationFragment", "Permission already granted on startup");
            requestLocationButton.setText("Add Location");
        } else {
            Log.i("LocationFragment", "Permission not granted yet, waiting for user to request");
            requestLocationButton.setText("Request Location Permission");
        }

        return view;
    }

    /**
     * Called when the fragment is attached to an activity.
     * Initializes the permission request launcher.
     *
     * @param context The context the fragment is attached to
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Code from Anthropic, Claude 3.7 Sonnet, "Update to use modern Activity Result API", accessed 05-13-2024
        // Initialize the permission request launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted
                        Log.i("LocationFragment", "Location permission granted by user");
                        requestLocationButton.setText("Add Location");
                        getLastLocation();
                    } else {
                        // Permission denied
                        Log.i("LocationFragment", "Location permission denied by user");

                        // Show toast message for denied permission
                        Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();

                        saveMoodEventWithoutLocation();

                        // If the user denied with "don't ask again", show a dialog to guide them to settings
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Permission Required")
                                    .setMessage("Location permission is required for this feature. Please enable it in app settings.")
                                    .setPositiveButton("Settings", (dialog, which) -> {
                                        // Open the app settings
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        }
                    }
                }
        );
    }

    /**
     * Checks if location services are enabled before requesting permission.
     * If services are enabled, requests permission; otherwise shows a dialog.
     */
    private void checkLocationAndRequestPermission() {
        // Code from Claude AI, Anthropic, "Enhance location logging", accessed 03-07-2024
        Log.i("LocationFragment", "Checking location services and requesting permission");

        if (isLocationEnabled()) {
            // Location services are enabled, so proceed with permission request
            Log.i("LocationFragment", "Location services are enabled");
            requestLocationPermission();
        } else {
            // Location services are disabled, so show dialog to enable them
            Log.i("LocationFragment", "Location services are disabled");
            handleLocationServicesDisabled();
        }
    }

    /**
     * Checks if location services (GPS or network) are enabled on the device.
     * This verifies if the user has enabled the location toggle in system settings.
     *
     * @return true if location services are enabled, false otherwise
     */
    private boolean isLocationEnabled() {
        // Get the location manager service
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Check if either GPS or network provider is enabled
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
        // Show toast indicating location couldn't be fetched
        Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();

        // Save mood event without location
        saveMoodEventWithoutLocation();

        // Show a dialog to let the user enable location services
        new AlertDialog.Builder(requireContext())
                .setTitle("Location Services Disabled")
                .setMessage("Please enable location services to use this feature.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    // Open the device location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Requests location permission using the modern Activity Result API.
     * This shows the system permission dialog to the user.
     */
    private void requestLocationPermission() {
        Log.i("LocationFragment", "Requesting location permission");

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Launch permission request using Activity Result API
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            // Permission is already granted, get location directly
            Log.i("LocationFragment", "Location permission already granted, getting location");
            getLastLocation();
        }
    }

    /**
     * Gets the last known location of the device.
     * This is a simplified approach to get location data without continuous updates.
     */
    private void getLastLocation() {
        // Return early if we don't have permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationFragment", "Missing permission in getLastLocation");
            return;
        }

        try {
            // Make a single request for the last known location
            Log.i("LocationFragment", "Requesting last known location");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            // We got a valid location
                            Log.i("LocationFragment", "Last known location retrieved: "
                                    + "Lat=" + location.getLatitude()
                                    + ", Lng=" + location.getLongitude());

                            // Store the location coordinates
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            // Notify listener about the location update
                            if (updateListener != null) {
                                updateListener.onLocationUpdated(latitude, longitude);
                            }

                            // Save the mood event with location
                            saveMoodEventWithLocation(latitude, longitude);

                            // Show success message
                            Toast.makeText(getContext(), "Location was successfully added", Toast.LENGTH_SHORT).show();
                        } else {
                            // No location available
                            Log.i("LocationFragment", "getLastLocation returned null - no location available");
                            saveMoodEventWithoutLocation();

                            // Show message that location couldn't be fetched
                            Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors during location retrieval
                        Log.e("LocationFragment", "Error getting last location: " + e.getMessage(), e);
                        saveMoodEventWithoutLocation();

                        // Show message that location couldn't be fetched
                        Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("LocationFragment", "Exception requesting last location: " + e.getMessage(), e);
            saveMoodEventWithoutLocation();

            // Show message that location couldn't be fetched
            Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves a mood event without location data using a default message.
     */
    public void saveMoodEventWithoutLocation() {
        Log.i("LocationFragment", "Saving mood event without location.");
        // Show message that location couldn't be fetched
        Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();
        // TODO: Implement saving logic here
    }

    /**
     * Saves a mood event with location data.
     * Called when location has been successfully retrieved.
     *
     * @param lat The latitude coordinate to save
     * @param lon The longitude coordinate to save
     */
    public void saveMoodEventWithLocation(double lat, double lon) {
        Log.i("LocationFragment", "Saving mood event with location - Lat: " + lat + ", Lng: " + lon);
        // TODO: Implement saving logic here

        // Store the coordinates for retrieval by getters
        this.latitude = lat;
        this.longitude = lon;
    }

    /**
     * Gets the latitude coordinate from the last successful location retrieval.
     *
     * @return The latitude value, or null if location has not been retrieved
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude coordinate from the last successful location retrieval.
     *
     * @return The longitude value, or null if location has not been retrieved
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the location update listener.
     *
     * @param listener The location update listener to set
     */
    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.updateListener = listener;
    }

    /**
     * Listener for location updates.
     */

}

