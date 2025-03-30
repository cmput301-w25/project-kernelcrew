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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.LocationHandler;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventFilter;
import com.kernelcrew.moodapp.ui.components.DefaultFilterBarFragment;
import com.kernelcrew.moodapp.ui.components.FilterBarFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

        // Setup map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Dynamically add the FilterBarFragment into the container in the map layout.
        filterBarFragment = new DefaultFilterBarFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.filterBarContainer, filterBarFragment);
        transaction.commit();
        filterBarFragment.setOnFilterChangedListener(this);
        currentFilter = filterBarFragment.getMoodEventFilter();

        // Request user's current location (permission if needed)
        new LocationHandler(getContext()).fetchLocation(new LocationHandler.OnLocationObtainedListener() {
            @Override
            public void onLocationObtained(double latitude, double longitude) {
                currentUserLocation = new LatLng(latitude, longitude);
                if (moodMap != null) {
                    moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f));
                    // Add marker for user's current location
                    MarkerOptions options = new MarkerOptions()
                            .position(currentUserLocation)
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    userMarker = moodMap.addMarker(options);
                }
                // Update filter with current location (e.g., radius 5 km)
                filterBarFragment.updateFilter(filter -> filter.setLocation(
                        currentUserLocation.latitude,
                        currentUserLocation.longitude,
                        5.0));

                loadMoodEventsOnMap();
            }

            @Override
            public void onLocationFailed(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup continuous location updates
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateUserLocationOnMap();
                }
            }
        };
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a
     * prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method
     * after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        moodMap = googleMap;
        moodMap.getUiSettings().setZoomControlsEnabled(true);
        moodMap.getUiSettings().setZoomGesturesEnabled(true);

        moodMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Object tag = marker.getTag();
                if (tag != null && tag instanceof String) {
                    String moodEventId = (String) tag;
                    Bundle args = new Bundle();
                    args.putString("moodEventId", moodEventId);
                    NavController navController = Navigation.findNavController(requireActivity(),
                            R.id.nav_host_fragment);
                    navController.navigate(R.id.moodDetails, args);
                    return true;
                }
                return false;
            }
        });

        if (currentUserLocation != null) {
            moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
        }
        loadMoodEventsOnMap();
    }

    private void loadMoodEventsOnMap() {
        if (moodMap == null || currentFilter == null)
            return;

        moodMap.clear();

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

        reg = currentFilter.buildQuery().addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.e("MoodMap", "loadMoodEventsOnMap: ", error);
                return;
            }
            if (queryDocumentSnapshots == null) {
                Log.w("MoodMap", "No snapshot data received.");
                return;
            }
            List<MoodEvent> moodList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                MoodEvent mood = doc.toObject(MoodEvent.class);
                if (mood != null && mood.getLatitude() != null && mood.getLongitude() != null) {
                    mood.setId(doc.getId());
                    moodList.add(mood);
                }
            }

            moodList = filterBarFragment.applyLocalSearch(moodList);

            float h = 1f;
            for (MoodEvent moodEvent : moodList) {
                LatLng position = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(moodEvent.getUsername())
                        .snippet(moodEvent.getEmotion().toString())
                        .icon(getEmotionIcon(moodEvent.getEmotion().toString()))
                        .zIndex(h++);
                Marker marker = moodMap.addMarker(markerOptions);
                marker.setTag(moodEvent.getId());
            }

            if (currentUserLocation != null) {
                MarkerOptions userOptions = new MarkerOptions()
                        .position(currentUserLocation)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .zIndex(h);
                userMarker = moodMap.addMarker(userOptions);
                moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
            }
        });
    }

    @Override
    public void onFilterChanged(MoodEventFilter filter) {
        currentFilter = filter;
        loadMoodEventsOnMap();
    }

    private void updateUserLocationOnMap() {
        if (moodMap != null && currentUserLocation != null) {
            if (userMarker == null) {
                MarkerOptions options = new MarkerOptions()
                        .position(currentUserLocation)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                userMarker = moodMap.addMarker(options);
                moodMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
            } else {
                userMarker.setPosition(currentUserLocation);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000) // update every 5 seconds
                .setFastestInterval(2000) // fastest update 2 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request missing permissions here if needed.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
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