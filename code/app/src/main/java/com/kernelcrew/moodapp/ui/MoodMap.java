package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationBarView;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MoodMap extends Fragment
        implements OnMapReadyCallback {

    private NavigationBarView navigationBarView;
    private BottomNavBarController navBarController;
    private MoodEventProvider moodEventProvider;
    private GoogleMap moodMap;
    private Map<Marker, MoodEvent> markerToMoodMap = new HashMap<>();
    private static List<MoodEvent> sharedMoodEvents = null;

    public static void setSharedMoodEvents(List<MoodEvent> events) {
        sharedMoodEvents = events;
    }

    public static List<MoodEvent> getSharedMoodEvents() {
        return sharedMoodEvents;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_map);
        navBarController = new BottomNavBarController(navigationBarView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        moodEventProvider = MoodEventProvider.getInstance();
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        moodMap = googleMap;
        moodMap.getUiSettings().setZoomControlsEnabled(true);
        List<MoodEvent> events = MoodMap.getSharedMoodEvents();
        loadMoodEventsOnMap(events);
        MoodMap.setSharedMoodEvents(null); // Free up memory
    }


    public void loadMoodEventsOnMap(List<MoodEvent> events) {
        // Clear existing markers
        moodMap.clear();
        markerToMoodMap.clear();

        boolean hasMarkers = false;
        LatLng lastPosition = null;

        for (MoodEvent moodEvent : events) {
            if (moodEvent != null && moodEvent.getLatitude() != null && moodEvent.getLongitude() != null) {
                // Get location for marker
                LatLng position = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
                lastPosition = position;

                // Get marker color based on emotion
                float markerColor = getEmotionColour(moodEvent.getEmotion().toString());

                // Create the marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(moodEvent.getEmotion().toString())
                        .snippet(moodEvent.getReason())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

                // Add the marker to the map
                Marker marker = moodMap.addMarker(markerOptions);
                if (marker != null) {
                    markerToMoodMap.put(marker, moodEvent);
                    hasMarkers = true;
                }
            }
        }

        // If we added markers, move camera to the last one
        if (hasMarkers) {
            moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 10.0f));
        }
    }


    private float getEmotionColour(String emotion) {
        switch (emotion) {
            case "Anger":
                return 0;
            case "Confused":
                return (float) 175.32;
            case "Disgust":
                return (float) 133.25;
            case "Fear":
                return (float) 274.05;
            case "Happy":
                return (float) 61.82;
            case "Sad":
                return (float) 216;
            case "Shame":
                return (float) 312.88;
            case "Surprise":
                return (float) 31.96;
            default:
                return 247;
        }
    }


}
