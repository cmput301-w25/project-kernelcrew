package com.kernelcrew.moodapp.ui;

import static com.kernelcrew.moodapp.ui.MoodIconUtil.getMoodIconResource;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;
import com.kernelcrew.moodapp.data.UserProvider;

public class MoodDetails extends Fragment implements DeleteDialogFragment.DeleteDialogListener {

    private MaterialToolbar toolbar;
    private ImageView imageMoodIcon, ivMoodPhoto;
    private TextView tvMoodState, tvSocialSituationValue, tvReasonValue;
    private Chip tvUsernameDisplay;
    private Button btnEditMood;
    private Button btnDeleteMood;
    private Button btnMoodComments;
    private ImageView visibilityIcon;

    private TextView tvPhotoLabel;
    private MaterialCardView cardPhoto;
    private TextView tvLocationLabel;
    private MaterialCardView cardLocation;

    private MoodEventProvider provider;

    // Document ID for the mood event and source of navigation
    private String moodEventId;

    // Use UID to identify the user
    private String userId;

    public MoodDetails() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve arguments passed from previous screen
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
        }

        // Initialize Firestore
        provider = MoodEventProvider.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_details, container, false);

        tvUsernameDisplay = view.findViewById(R.id.tvUsernameDisplay);
        toolbar = view.findViewById(R.id.moodDetailsToolbar);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        tvMoodState = view.findViewById(R.id.tvMoodState);
        tvSocialSituationValue = view.findViewById(R.id.tvSocialSituationValue);
        tvReasonValue = view.findViewById(R.id.tvReasonValue);
        ivMoodPhoto = view.findViewById(R.id.ivMoodPhoto);
        btnEditMood = view.findViewById(R.id.btnEditMood);
        btnDeleteMood = view.findViewById(R.id.btnDeleteMood);
        btnMoodComments = view.findViewById(R.id.btnMoodComments);
        visibilityIcon = view.findViewById(R.id.visibility_icon);
        tvPhotoLabel = view.findViewById(R.id.tvPhotoLabel);
        cardPhoto = view.findViewById(R.id.cardPhoto);
        tvLocationLabel = view.findViewById(R.id.tvLocationLabel);
        cardLocation = view.findViewById(R.id.cardLocation);

        toolbar.setNavigationContentDescription("BackButton_MoodDetails");

        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        fetchMoodDetails(moodEventId);

        btnEditMood.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", moodEventId);
            NavHostFragment.findNavController(this).navigate(R.id.editMoodEvent, args);
        });

        btnDeleteMood.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", moodEventId);
            DeleteDialogFragment dialogFragment = new DeleteDialogFragment();
            dialogFragment.setArguments(args);
            dialogFragment.setDeleteDialogListener(this);
            dialogFragment.show(getParentFragmentManager(), "delete_dialog");
        });

        btnMoodComments.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", moodEventId);
            NavHostFragment.findNavController(this).navigate(R.id.commentsFragment, args);
        });

        return view;

    }

    private void fetchMoodDetails(String moodEventId) {
        if (moodEventId == null) return;

        provider.getMoodEvent(moodEventId)
                .addOnSuccessListener(moodEvent -> {
                    if (moodEvent != null) {
                        bindMoodData(moodEvent);
                    } else {
                        Toast.makeText(requireContext(), "Mood event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load mood details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Binds MoodEvent data to the UI components.
     */
    private void bindMoodData(MoodEvent moodEvent) {
        tvMoodState.setText(moodEvent.getEmotion().toString());
        tvSocialSituationValue.setText(moodEvent.getSocialSituation());
        tvReasonValue.setText(moodEvent.getReason());

        userId = moodEvent.getUid();

        int moodImageRes = getMoodIconResource(moodEvent.getEmotion().toString());
        imageMoodIcon.setImageResource(moodImageRes);

        // Conditionally display photo
        Bitmap photo = moodEvent.getPhoto();
        if (photo != null) {
            ivMoodPhoto.setImageBitmap(photo);
            tvPhotoLabel.setVisibility(View.VISIBLE);
            cardPhoto.setVisibility(View.VISIBLE);
        } else {
            tvPhotoLabel.setVisibility(View.GONE);
            cardPhoto.setVisibility(View.GONE);
        }

        // Conditionally display location
        if (moodEvent.hasLocation()) {
            tvLocationLabel.setVisibility(View.VISIBLE);
            cardLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocationLabel.setVisibility(View.GONE);
            cardLocation.setVisibility(View.GONE);
        }

        switch (moodEvent.getVisibility()) {
            case PUBLIC:
                visibilityIcon.setImageResource(R.drawable.ic_public);
                break;
            case PRIVATE:
                visibilityIcon.setImageResource(R.drawable.ic_lock);
                break;
        }

        // Fetch username via UserProvider
        if (userId != null && !userId.isEmpty()) {
            UserProvider.getInstance().fetchUsername(userId)
                    .addOnSuccessListener(username -> {
                        tvUsernameDisplay.setText("@" + username);
                        // Navigate to OtherUserProfile on click
                        tvUsernameDisplay.setOnClickListener(v -> {
                            Bundle args = new Bundle();
                            args.putString("uid", userId);
                            NavHostFragment.findNavController(MoodDetails.this)
                                    .navigate(R.id.otherUserProfile, args);
                        });
                    })
                    .addOnFailureListener(e -> {
                        tvUsernameDisplay.setText(R.string.error_loading_user);
                    });
        } else {
            tvUsernameDisplay.setText(R.string.no_user_id_provided);
        }

        // Update edit/delete button visibility based on ownership
        handleOwnershipUI(userId);

        if (moodEvent.hasLocation()) {
            SupportMapFragment mapFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.mapContainer);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    Log.d("MoodDetails", "Map is ready");

                    if (moodEvent.getLatitude() != null && moodEvent.getLongitude() != null) {
                        LatLng location = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
                        BitmapDescriptor icon = EmotionIconUtils.getEmotionIcon(requireContext(), moodEvent.getEmotion().toString());
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(moodEvent.getUsername())
                                .snippet(moodEvent.getEmotion().toString())
                                .icon(icon));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));
                    } else {
                        Log.e("MoodDetails", "MoodEvent has no coordinates");
                    }
                });
            } else {
                Log.e("MoodDetails", "Map fragment not found");
            }
        }
    }

    /**
     * Checks if the current user owns the mood, and updates the edit/delete button visibility.
     */
    private void handleOwnershipUI(String moodOwnerUid) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // If no one is logged in or we have no owner, hide everything
        if (currentUser == null || moodOwnerUid == null) {
            btnEditMood.setVisibility(View.GONE);
            btnDeleteMood.setVisibility(View.GONE);
            return;
        }

        // Compare the mood's owner ID to the current user's ID
        String currentUserUid = currentUser.getUid();
        if (moodOwnerUid.equals(currentUserUid)) {
            // The current user owns this mood => show the edit/delete buttons
            btnEditMood.setVisibility(View.VISIBLE);
            btnDeleteMood.setVisibility(View.VISIBLE);
        } else {
            // The current user does NOT own it => hide the buttons
            btnEditMood.setVisibility(View.GONE);
            btnDeleteMood.setVisibility(View.GONE);
        }
    }

    private void handleBackButton() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void onDeleteConfirmed() {
        Toast.makeText(requireContext(), "Mood deleted successfully", Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(this).popBackStack();
    }


}
