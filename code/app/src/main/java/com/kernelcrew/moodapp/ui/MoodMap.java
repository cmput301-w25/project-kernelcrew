package com.kernelcrew.moodapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.Emotion;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

import java.util.HashMap;
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

        loadMoodEventsOnMap();

    }


    private void loadMoodEventsOnMap() {
        // Clear existing markers
        moodMap.clear();
        markerToMoodMap.clear();

        // ##################################################################
        //          TODO - Implement filter for MoodEvents on map
        // ##################################################################

        // Query all mood events with location data
        moodEventProvider.getAll()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasMarkers = false;
                    LatLng lastPosition = null;

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        MoodEvent moodEvent = document.toObject(MoodEvent.class);
                        if (moodEvent.getUsername() != null && moodEvent != null && moodEvent.getLatitude() != null && moodEvent.getLongitude() != null) {
                            // Get location for marker
                            LatLng position = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
                            lastPosition = position;

                            // Get marker icon based on emotion
                            BitmapDescriptor emotionIcon = getEmotionIcon(moodEvent.getEmotion().toString());

                            // Create the marker
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(position)
                                    .title(moodEvent.getUsername())
                                    .snippet(moodEvent.getEmotion().toString())
                                    .icon(emotionIcon);

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
                })
                .addOnFailureListener(e ->
                        Log.e("MoodMapFragment", "Error loading mood events on map", e));
    }


    private BitmapDescriptor getEmotionIcon(String emotion) {
        int resourceId;

        // Assign the appropriate drawable resource based on the emotion
        switch (emotion) {
            case "Anger":
                resourceId = R.drawable.ic_anger_color_with_bg;
                break;
            case "Confused":
                resourceId = R.drawable.ic_confused_color_with_bg;
                break;
            case "Disgust":
                resourceId = R.drawable.ic_disgust_color_with_bg;
                break;
            case "Fear":
                resourceId = R.drawable.ic_fear_color_with_bg;
                break;
            case "Happy":
                resourceId = R.drawable.ic_happy_color_with_bg;
                break;
            case "Sad":
                resourceId = R.drawable.ic_sad_color_with_bg;
                break;
            case "Shame":
                resourceId = R.drawable.ic_shame_color_with_bg;
                break;
            case "Surprise":
                resourceId = R.drawable.ic_surprise_color_with_bg;
                break;
            default:
                resourceId = R.drawable.ic_error_color;
                break;
        }

        // Create a properly scaled bitmap from the drawable resource
        return getBitmapDescriptorFromVector(getContext(), resourceId);
    }


    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();

        vectorDrawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}
