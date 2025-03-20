package com.kernelcrew.moodapp.ui;

import static com.kernelcrew.moodapp.ui.MoodIconUtil.getMoodIconResource;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

public class MoodDetails extends Fragment implements DeleteDialogFragment.DeleteDialogListener {

    private MaterialToolbar toolbar;
    private ImageView imageMoodIcon, ivMoodPhoto;
    private TextView tvMoodState, tvTriggerValue, tvSocialSituationValue, tvReasonValue;
    private Button btnEditMood;
    private Button btnViewProfile;

    private Button btnDeleteMood;
    private FirebaseFirestore db;
    private MoodEventProvider provider;

    // Document ID for the mood event and source of navigation
    private String moodEventId;
    private String sourceScreen; // e.g., "home" or "filtered"

    // Use UID to identify the user (instead of a username)
    private String userId;

    public MoodDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve arguments passed from previous screen
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
            sourceScreen = getArguments().getString("sourceScreen", "home");
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        provider = MoodEventProvider.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_details, container, false);

        toolbar = view.findViewById(R.id.moodDetailsToolbar);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        tvMoodState = view.findViewById(R.id.tvMoodState);
        tvTriggerValue = view.findViewById(R.id.tvTriggerValue);
        tvSocialSituationValue = view.findViewById(R.id.tvSocialSituationValue);
        tvReasonValue = view.findViewById(R.id.tvReasonValue);
        ivMoodPhoto = view.findViewById(R.id.ivMoodPhoto);
        btnEditMood = view.findViewById(R.id.btnEditMood);
        btnDeleteMood = view.findViewById(R.id.btnDeleteMood);
        btnViewProfile = view.findViewById(R.id.btnViewProfile);

        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        // Set up "View Profile" button click to navigate to MyProfile, passing UID
        btnViewProfile.setOnClickListener(v -> {
            if (userId != null && !userId.isEmpty()) {
                Bundle args = new Bundle();
                args.putString("uid", userId);
                // Ensure your nav_graph.xml includes a destination for OtherUserProfile with the ID otherUserProfile
                NavHostFragment.findNavController(this)
                        .navigate(R.id.otherUserProfile, args);
            } else {
                Toast.makeText(requireContext(), "User information unavailable.", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch mood details from Firestore
        fetchMoodDetails(moodEventId);

        btnEditMood.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", moodEventId);
            NavHostFragment.findNavController(this).navigate(R.id.editMoodEvent, args);
        });
        //Logic to delete a mood
        btnDeleteMood.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", moodEventId);
            DeleteDialogFragment dialogFragment = new DeleteDialogFragment();
            dialogFragment.setArguments(args);
            dialogFragment.setDeleteDialogListener(this); // Add this line
            dialogFragment.show(getParentFragmentManager(), "delete_dialog");
        });

        return view;

    }

    /**
     * Fetches the mood details document from Firestore using the moodEventId.
     */
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
        tvTriggerValue.setText(moodEvent.getTrigger());
        tvSocialSituationValue.setText(moodEvent.getSocialSituation());
        tvReasonValue.setText(moodEvent.getReason());

        // Retrieve the UID from the mood event
        userId = moodEvent.getUid();

        int moodImageRes = getMoodIconResource(moodEvent.getEmotion().toString());
        imageMoodIcon.setImageResource(moodImageRes);

        Bitmap photo = moodEvent.getPhoto();
        if (photo != null) {
            ivMoodPhoto.setImageBitmap(photo);
        }
    }


    /**
     * Handles the back button behavior.
     */
    private void handleBackButton() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void onDeleteConfirmed() {
        Toast.makeText(requireContext(), "Mood deleted successfully", Toast.LENGTH_SHORT).show();
        // Navigate back to home screen
        NavHostFragment.findNavController(this).popBackStack();
    }
}
