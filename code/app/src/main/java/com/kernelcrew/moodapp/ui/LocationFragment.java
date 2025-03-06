//Code from OpenAi, ChatGPT, "Build location fragment using google maps API in java in android studio requesting the user for permission to get their location and handling error gracefully if location can't be reached", accessed 03-02-2025

package com.kernelcrew.moodapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kernelcrew.moodapp.R;

public class LocationFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private Button requestLocationButton;

    // Variables to store latitude and longitude
    private Double latitude = null;
    private Double longitude = null;

    // Permission launcher
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getLastKnownLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied. Saving mood event without location.", Toast.LENGTH_SHORT).show();
                    saveMoodEventWithoutLocation();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        requestLocationButton = view.findViewById(R.id.request_location_button);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        requestLocationButton.setOnClickListener(v -> requestLocationPermission());

        return view;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Save location into variables
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            // Log or use the values
                            Toast.makeText(getContext(), "Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_SHORT).show();
                            saveMoodEventWithLocation(latitude, longitude);
                        } else {
                            Toast.makeText(getContext(), "Unable to get location. Saving mood event without location.", Toast.LENGTH_SHORT).show();
                            saveMoodEventWithoutLocation();
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Location retrieval failed. Saving mood event without location.", Toast.LENGTH_SHORT).show();
                    saveMoodEventWithoutLocation();
                });
    }

    // Method to save mood event without location
    private void saveMoodEventWithoutLocation() {
        // Placeholder for saving a mood event without location
        Toast.makeText(getContext(), "Mood event saved without location.", Toast.LENGTH_SHORT).show();
        // TODO: Implement saving logic here
    }

    // Method to save mood event with location
    private void saveMoodEventWithLocation(double lat, double lon) {
        // Placeholder for saving a mood event with location
        Toast.makeText(getContext(), "Mood event saved with location: " + lat + ", " + lon, Toast.LENGTH_SHORT).show();
        // TODO: Implement saving logic here
    }

    // Getter methods
    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
