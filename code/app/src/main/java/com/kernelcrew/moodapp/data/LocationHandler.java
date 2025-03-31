package com.kernelcrew.moodapp.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.location.Location;

/**
 * LocationHandler - A non-UI class for requesting location updates
 * Created by taking logic from a functional LocationFragment, so that it can be used in other functions.
 */
public class LocationHandler {
    public static final int REQUEST_LOCATION_PERMISSION = 1001;

    public interface OnLocationObtainedListener {
        /**
         * Called when the location is successfully obtained.
         * @param latitude  the retrieved latitude
         * @param longitude the retrieved longitude
         */
        void onLocationObtained(double latitude, double longitude);

        /**
         * Called when obtaining location fails, either due to missing permissions,
         * disabled services, or runtime errors.
         * @param error human-readable error message
         */
        void onLocationFailed(String error);
    }

    private static final String TAG = "LocationHandler";

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHandler(Context context) {
        this.context = context.getApplicationContext(); // safer to hold appContext
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Initiates a location fetch:
     * 1. Checks if location services are enabled (GPS/Network).
     * 2. Checks if FINE_LOCATION permission is granted.
     * 3. Attempts to retrieve the last known location.
     * 4. If null or an error occurs, requests a new location update.
     *
     * @param listener callback for success/failure
     */
    public void fetchLocation(OnLocationObtainedListener listener) {
        Log.i(TAG, "fetchLocation() called");

        // 1. Check if location services (GPS/Network) are enabled
        if (!isLocationEnabled()) {
            Log.w(TAG, "Location services are disabled");
            listener.onLocationFailed("Location services are disabled");
            return;
        }

        // 2. Verify permission before proceeding
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing ACCESS_FINE_LOCATION permission");
            listener.onLocationFailed("Missing location permission");
            return;
        }

        // 3. Attempt to get the last known location
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Last known location found: " +
                                    location.getLatitude() + ", " + location.getLongitude());
                            listener.onLocationObtained(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.d(TAG, "Last known location is null, requesting a fresh update");
                            requestNewLocation(listener);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting last known location: " + e.getMessage(), e);
                        // Attempt a fresh update on failure
                        requestNewLocation(listener);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception while retrieving last known location: " + e.getMessage(), e);
            listener.onLocationFailed("Error accessing location services");
        }
    }

    /**
     * Requests a new single-location update using high accuracy.
     * Calls the OnLocationObtainedListener on success or failure.
     *
     * @param listener callback for success/failure
     */
    private void requestNewLocation(OnLocationObtainedListener listener) {
        Log.i(TAG, "requestNewLocation() called to get a fresh location fix");

        // Double-check permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing ACCESS_FINE_LOCATION permission (requestNewLocation)");
            listener.onLocationFailed("Missing location permission");
            return;
        }

        try {
            // Create a LocationRequest for single-update high accuracy
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10_000)      // 10s
                    .setFastestInterval(5_000)// 5s
                    .setNumUpdates(1);       // just one update

            // Define a callback to handle the new location result
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null || locationResult.getLastLocation() == null) {
                        Log.w(TAG, "Location update returned null location");
                        listener.onLocationFailed("Location update returned null");
                    } else {
                        Location location = locationResult.getLastLocation();
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        Log.d(TAG, "Newly requested location obtained: " + lat + ", " + lng);
                        listener.onLocationObtained(lat, lng);
                    }

                    // Stop further updates
                    fusedLocationClient.removeLocationUpdates(this);
                }
            };

            // Request a location update
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper());

        } catch (Exception e) {
            Log.e(TAG, "Error requesting new location updates: " + e.getMessage(), e);
            listener.onLocationFailed("Error requesting location updates");
        }
    }

    /**
     * Checks if location services (GPS or Network) are enabled on the device.
     *
     * @return true if enabled, false otherwise
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            // Rare case: device might not have a location manager service
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Returns the current location if permission is granted.
     * If not, it requests the ACCESS_FINE_LOCATION permission from the Activity.
     *
     * @param context The context (should be an Activity).
     * @return The last known Location, or null if not available or permission is missing.
     */
    public static Location getCurrentLocation(Context context) {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if context is an Activity
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION
                );
            }
            return null;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return location;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}