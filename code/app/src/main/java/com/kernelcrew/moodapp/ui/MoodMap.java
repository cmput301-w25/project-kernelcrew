package com.kernelcrew.moodapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.CombinedListener;
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.data.LocationHandler;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.ui.components.DefaultFilterBarFragment;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import java.util.ArrayList;
import java.util.List;

public class MoodMap extends Fragment implements OnMapReadyCallback, FilterBarFragment.OnFilterChangedListener {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap moodMap;
    private BottomNavBarController navBarController;
    private FilterBarFragment filterBarFragment;
    private MoodEventFilter currentFilter;
    private LatLng currentUserLocation;
    private ListenerRegistration reg;
    private Marker userMarker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        NavigationBarView navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_map);
        navBarController = new BottomNavBarController(navigationBarView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        filterBarFragment = new DefaultFilterBarFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.filterBarContainer, filterBarFragment);
        transaction.commit();
        filterBarFragment.setOnFilterChangedListener(this);

        new LocationHandler(getContext()).fetchLocation(new LocationHandler.OnLocationObtainedListener() {
            @Override
            public void onLocationObtained(double latitude, double longitude) {
                currentUserLocation = new LatLng(latitude, longitude);
                if (moodMap != null) {
                    moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f));
                    moodMap.addMarker(new MarkerOptions()
                            .position(currentUserLocation)
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }

                currentFilter = filterBarFragment.getMoodEventFilter().setLocation(
                        currentUserLocation.latitude,
                        currentUserLocation.longitude,
                        5
                );

                // Now load mood events with the obtained location.
                loadMoodEventsOnMap();
            }

            @Override
            public void onLocationFailed(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // Use the most recent location
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateUserLocationOnMap();
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        moodMap = googleMap;
        moodMap.getUiSettings().setZoomControlsEnabled(true);
        moodMap.getUiSettings().setZoomGesturesEnabled(true);

        if (currentUserLocation != null) {
            moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
        }
        loadMoodEventsOnMap();
    }

    private void loadMoodEventsOnMap() {
        if (moodMap == null || currentFilter == null) return;

        moodMap.clear();

        if (currentUserLocation != null) {
            MarkerOptions userMarkerOptions = new MarkerOptions()
                    .position(currentUserLocation)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .zIndex(10f);
            moodMap.addMarker(userMarkerOptions);
            moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
        }

        Double filterLat = currentFilter.getFilterLatitude();
        Double filterLon = currentFilter.getFilterLongitude();
        Double filterRadius = currentFilter.getFilterRadius();
        if (filterLat != null && filterLon != null && filterRadius != null) {
            LatLng center = new LatLng(filterLat, filterLon);
            CircleOptions circleOptions = new CircleOptions()
                    .center(center)
                    .radius(filterRadius * 1000)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF);
            moodMap.addCircle(circleOptions);
        }

        if (reg != null) {
            reg.remove();
            reg = null;
        }

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowProvider.getInstance().listenForFollowing(myUid, (snapshot, error) -> {
            if (error != null) {
                Log.e("MoodMap", "Error listening for following", error);
                return;
            }
            // Build a list of user IDs: include the current user and all users they follow.
            List<String> userIds = new ArrayList<>();
            userIds.add(myUid);
            if (snapshot != null && !snapshot.isEmpty()) {
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    userIds.add(doc.getId());
                }
            }

            // Remove any previous mood event listener
            if (reg != null) {
                reg.remove();
                reg = null;
            }

            // Now listen to mood events only from these userIds using a helper method.
            // (Assuming your MoodEventProvider has a method listenToMoodEventsForUsers that accepts
            // a list of user IDs and a filter.)
            reg = MoodEventProvider.getInstance()
                    .listenToMoodEventsForUsers(userIds, currentFilter, new CombinedListener() {
                        @Override
                        public void onEvent(List<DocumentSnapshot> documents, FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e("MoodMap", "Error listening to mood events", error);
                                return;
                            }
                            // Convert documents to MoodEvent objects.
                            List<MoodEvent> moodList = new ArrayList<>();
                            for (DocumentSnapshot document : documents) {
                                MoodEvent mood = document.toObject(MoodEvent.class);
                                if (mood != null && mood.getLatitude() != null && mood.getLongitude() != null) {
                                    mood.setId(document.getId());
                                    moodList.add(mood);
                                }
                            }

                            // Clear mood markers (user marker and circle overlays were already added above)
                            // (If you want to preserve them, you can re-add them after clearing.)
                            // For simplicity, clear and then re-add:
                            moodMap.clear();
                            // Re-add the user marker and circle overlays:
                            if (currentUserLocation != null) {
                                MarkerOptions userMarkerOptions = new MarkerOptions()
                                        .position(currentUserLocation)
                                        .title("You are here")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                        .zIndex(10f);
                                moodMap.addMarker(userMarkerOptions);
                                moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
                            }
                            if (filterLat != null && filterLon != null && filterRadius != null) {
                                LatLng center = new LatLng(filterLat, filterLon);
                                CircleOptions circleOptions = new CircleOptions()
                                        .center(center)
                                        .radius(filterRadius * 1000)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(0x220000FF);
                                moodMap.addCircle(circleOptions);
                            }

                            // Add markers for each mood event.
                            for (MoodEvent moodEvent : moodList) {
                                LatLng position = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(position)
                                        .title(moodEvent.getUsername())
                                        .snippet(moodEvent.getEmotion().toString())
                                        .icon(getEmotionIcon(moodEvent.getEmotion().toString()))
                                        .zIndex(1f);
                                moodMap.addMarker(markerOptions);
                            }
                        }
                    });
        });
    }

    @Override
    public void onFilterChanged(MoodEventFilter filter) {
        currentFilter = filter;
        loadMoodEventsOnMap();
    }

    private BitmapDescriptor getEmotionIcon(String emotion) {
        int resourceId;
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
        return getBitmapDescriptorFromVector(getContext(), resourceId);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)            // Update interval in milliseconds
                .setFastestInterval(2000)     // Fastest update interval
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateUserLocationOnMap() {
        if (moodMap != null && currentUserLocation != null) {
            if (userMarker == null) {
                // Create the marker if it doesn't exist yet
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(currentUserLocation)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                userMarker = moodMap.addMarker(markerOptions);
                moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
            } else {
                // Update the marker's position
                userMarker.setPosition(currentUserLocation);
            }
        }
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