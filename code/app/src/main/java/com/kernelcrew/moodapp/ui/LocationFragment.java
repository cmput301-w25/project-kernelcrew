/**
 * LocationFragment - Handles location functionality for mood events
 *
 * This fragment manages all location-related operations including:
 * - Requesting and handling location permissions
 * - Getting the user's current location coordinates using FusedLocationProviderClient
 * - Providing location data to other components through the LocationUpdateListener interface
 * - Displaying UI for location functionality in the mood creation workflow
 *
 * Created by Anthropic, Claude 3.7 Sonnet, "Create LocationFragment class", accessed 03-10-2025
 */

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
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.kernelcrew.moodapp.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;
import android.location.Location;


public class LocationFragment extends Fragment {

    /**
     * Listener for user location changed updates.
     */
    private LocationUpdateListener updateListener;

    /**
     * Main client for interacting with the fused location provider.
     * Provides access to device location with the appropriate permissions.
     */
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Button for requesting location access.
     * When clicked, initiates the location permission flow.
     */
    private ImageButton requestLocationButton;

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
    private LocationUpdateListener listener;

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
        View view = inflater.inflate(R.layout.location_fragment, container, false);
        TextView mapTipText = view.findViewById(R.id.map_tip_text);
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

        // Find the location card view (map container)
        View cardLocation = view.findViewById(R.id.cardLocation);
        cardLocation.setVisibility(View.GONE); // Hide initially

        Button removeLocationButton = view.findViewById(R.id.remove_location_button);
        removeLocationButton.setVisibility(View.GONE); // Initially hidden

        // Set up the button click listener to start the location request process and show map
        requestLocationButton.setOnClickListener(v -> {
            Log.i("LocationFragment", "Location button clicked");
            requestLocationButton.setVisibility(View.GONE);
            cardLocation.setVisibility(View.VISIBLE);
            removeLocationButton.setVisibility(View.VISIBLE);
            checkLocationAndRequestPermission();
            //Following map tip text created by OpenAI, ChatGPT-4, "Add map tip using TextView in Java Android studio for OnCreateView", accessed 03-30-2025
            mapTipText.setAlpha(0f);
            mapTipText.setVisibility(View.VISIBLE);
            mapTipText.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        mapTipText.postDelayed(() -> {
                            mapTipText.animate()
                                    .alpha(0f)
                                    .setDuration(500)
                                    .withEndAction(() -> mapTipText.setVisibility(View.GONE))
                                    .start();
                        }, 4000);
                    })
                    .start();

        });


        removeLocationButton.setOnClickListener(v -> {
            Log.i("LocationFragment", "Remove location clicked");
            latitude = null;
            longitude = null;

            cardLocation.setVisibility(View.GONE);
            requestLocationButton.setVisibility(View.VISIBLE);
            removeLocationButton.setVisibility(View.GONE);

            if (updateListener != null) {
                updateListener.onLocationUpdated(null, null);
            }

            Toast.makeText(getContext(), "Location removed", Toast.LENGTH_SHORT).show();
        });

        // Check if we already have permission when the fragment is created
        // Code from Claude AI, Anthropic, "Fix permission dialog not appearing", accessed 03-07-2024
        Log.i("LocationFragment", "Checking initial permission state");
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("LocationFragment", "Permission already granted on startup");
        } else {
            Log.i("LocationFragment", "Permission not granted yet, waiting for user to request");
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
                        requestNewLocation();
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
            // First try getLastLocation
            Log.i("LocationFragment", "Requesting last known location");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            // We got a valid location from getLastLocation
                            handleLocationSuccess(location);
                        } else {
                            // No last location available, request a new location update
                            Log.i("LocationFragment", "No last location available, requesting location updates");

                        }
                        requestNewLocation();
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors during location retrieval
                        Log.e("LocationFragment", "Error getting last location: " + e.getMessage(), e);
                        // Try requesting a new location instead
                        requestNewLocation();
                    });
        } catch (Exception e) {
            Log.e("LocationFragment", "Exception requesting last location: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error accessing location services", Toast.LENGTH_SHORT).show();
            saveMoodEventWithoutLocation();
        }
    }

    // Add this method to request a new location (not just the last known one)
    private void requestNewLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            // Create location request
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    .setNumUpdates(1); // We only need one location update

            // Create location callback
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.i("LocationFragment", "Location update returned null result");
                        saveMoodEventWithoutLocation();
                        return;
                    }

                    // Get the newest location
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        // We got a valid location from the update
                        handleLocationSuccess(location);
                    } else {
                        // This shouldn't happen if locationResult is not null
                        Log.i("LocationFragment", "Location update returned null location");
                        saveMoodEventWithoutLocation();
                    }

                    // Remove updates after getting the location
                    fusedLocationClient.removeLocationUpdates(this);
                }
            };

            // Request location updates
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper());

        } catch (Exception e) {
            Log.e("LocationFragment", "Error requesting location updates: " + e.getMessage(), e);
            saveMoodEventWithoutLocation();
        }
    }

    // Helper method to handle successful location retrieval
    private void handleLocationSuccess(Location location) {
        Log.i("LocationFragment", "Location retrieved: "
                + "Lat=" + location.getLatitude()
                + ", Lng=" + location.getLongitude());

        // Store the location coordinates
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        // Notify listener about the location update
        if (updateListener != null) {
            Log.d("LocationDebug", "Calling onLocationUpdated");
            updateListener.onLocationUpdated(latitude, longitude);
        } else {
            Log.d("LocationDebug", "updateListener is null!");
        }

        // Initialize map similar to MoodDetails
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapContainer);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                googleMap.clear();
                Log.d("LocationFragment", "Map is ready");
                if (latitude != null && longitude != null) {
                    LatLng locationLatLng = new LatLng(latitude, longitude);
                    googleMap.addMarker(new MarkerOptions()
                            .position(locationLatLng)
                            .title("Mood Location"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 12f));
                } else {
                    Log.e("LocationFragment", "No coordinates to show on map.");
                }
                // Set up an OnMapClickListener so that if the user taps elsewhere, we update the location.
                googleMap.setOnMapClickListener(latLng -> {
                    // Clear current markers
                    googleMap.clear();
                    // Add a new marker at the tapped location with the same style
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Mood Location"));
                    // Move the camera to the new location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
                    // Update stored coordinates
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;
                    // Notify any listener of the update
                    if (updateListener != null) {
                        updateListener.onLocationUpdated(latitude, longitude);
                    }
                });
            });
        } else {
            Log.e("LocationFragment", "Map fragment not found.");
        }
    }

    /**
     * Saves a mood event without location data using a default message.
     */
    public void saveMoodEventWithoutLocation() {
        Log.i("LocationFragment", "Saving mood event without location.");
        Toast.makeText(getContext(), "Location was unable to be fetched", Toast.LENGTH_SHORT).show();
        // TODO: Implement saving logic here
    }

    /**
     * Listener for location updates.
     */
    public void setUpdateListener(LocationUpdateListener form) {
        this.updateListener = form;
    }

    public void setLocation(Double lat, Double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    //Created by Anthropic, Claude 3.7 Sonnet, "Generate comprehensive JavaDoc for LocationFragment methods", accessed 03-30-2025
    /**
     * Populates the map with an existing location coordinate pair.
     * This method is used when editing a mood event that already has location data,
     * or when restoring a previously saved location.
     *
     * The method performs the following actions:
     * <ul>
     *     <li>Updates the internal latitude and longitude properties</li>
     *     <li>Shows the map card and hides the add location button</li>
     *     <li>Shows the remove location button</li>
     *     <li>Initializes the map with a marker at the specified coordinates</li>
     *     <li>Sets up a click listener to allow the user to change the location by tapping elsewhere on the map</li>
     * </ul>
     *
     * Note that this method uses getView() which may return null if the fragment
     * is not attached to its activity. Callers should ensure the fragment is
     * in the proper lifecycle state before calling this method.
     *
     * @param latitude The latitude coordinate to display on the map
     * @param longitude The longitude coordinate to display on the map
     *
     * @throws NullPointerException if getView() returns null or if map initialization fails
     * @throws IllegalStateException if the fragment is not attached to an activity
     */

    public void populateMapFromExistingLocation (double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

        View cardLocation = getView().findViewById(R.id.cardLocation);
        ImageButton requestLocationButton = getView().findViewById(R.id.add_location_button);
        Button removeLocationButton = getView().findViewById(R.id.remove_location_button);

        if (cardLocation != null && requestLocationButton != null) {
            cardLocation.setVisibility(View.VISIBLE);
            requestLocationButton.setVisibility(View.GONE);
        }

        if (removeLocationButton != null) {
            removeLocationButton.setVisibility(View.VISIBLE);
            //Following map tip created by OpenAI, ChatGPT-4, "Add map tip using TextView in Java Android studio for populateMapFromExistingLocation", accessed 03-30-2025
            TextView mapTipText = getView().findViewById(R.id.map_tip_text);
            if (mapTipText != null) {
                mapTipText.setTranslationY(-50);
                mapTipText.setAlpha(0f);
                mapTipText.setVisibility(View.VISIBLE);

                mapTipText.animate()
                        .alpha(1f)
                        .translationY(0)
                        .setDuration(500)
                        .withEndAction(() -> {
                            mapTipText.postDelayed(() -> {
                                mapTipText.animate()
                                        .alpha(0f)
                                        .translationY(-50)
                                        .setDuration(500)
                                        .withEndAction(() -> mapTipText.setVisibility(View.GONE))
                                        .start();
                            }, 4000);
                        })
                        .start();
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapContainer);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                googleMap.clear();
                LatLng locationLatLng = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(locationLatLng)
                        .title("Existing Mood Location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 12f));

                // Allow user to tap to update location
                googleMap.setOnMapClickListener(latLng -> {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Updated Mood Location"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
                    // Notify any listener of the update
                    if (updateListener != null) {
                        updateListener.onLocationUpdated(latLng.latitude, latLng.longitude);
                    }
                });
            });
        }
    }
}

