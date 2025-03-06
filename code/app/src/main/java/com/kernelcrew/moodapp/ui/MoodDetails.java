package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;

public class MoodDetails extends Fragment {

    private MaterialToolbar toolbar;
    private ImageView imageMoodIcon, ivMoodPhoto;
    private TextView tvMoodState, tvTriggerValue, tvSocialSituationValue, tvReasonValue;
    private Button btnEditMood;
    private FirebaseFirestore db;

    // Document ID for the mood event and source of navigation
    private String moodEventId;
    private String sourceScreen; // e.g., "home" or "filtered"

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_details, container, false);

        // Bind views
        toolbar = view.findViewById(R.id.moodDetailsToolbar);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        tvMoodState = view.findViewById(R.id.tvMoodState);
        tvTriggerValue = view.findViewById(R.id.tvTriggerValue);
        tvSocialSituationValue = view.findViewById(R.id.tvSocialSituationValue);
        tvReasonValue = view.findViewById(R.id.tvReasonValue);
        ivMoodPhoto = view.findViewById(R.id.ivMoodPhoto);
        btnEditMood = view.findViewById(R.id.btnEditMood);

        // Handle back navigation via the toolbar
        toolbar.setNavigationOnClickListener(v -> handleBackButton());

        // Fetch mood details from Firestore
        fetchMoodDetails(moodEventId);

        // TODO: Implement navigation to EditMood screen when ready
        btnEditMood.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("moodEventId", moodEventId);
            // TODO: Pass any additional fields if needed

            // Navigate to the EditMood fragment using the Navigation Component
//            NavHostFragment.findNavController(this).navigate(R.id.action_moodDetails_to_editMood, args);
        });

        return view;
    }

    /**
     * Fetches the mood details document from Firestore using the moodEventId.
     */
    private void fetchMoodDetails(String moodEventId) {
        if (moodEventId == null) return;
        db.collection("moodEvent")
                .document(moodEventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bindMoodData(documentSnapshot);
                    } else {
                        Toast.makeText(requireContext(), "Mood event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load mood details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Binds Firestore document data to the UI components.
     */
    private void bindMoodData(DocumentSnapshot doc) {
        String moodState = doc.getString("moodState");
        String trigger = doc.getString("trigger");
        String socialSituation = doc.getString("socialSituation");
        String reason = doc.getString("reason");
        String photoUrl = doc.getString("photoUrl");

        tvMoodState.setText(moodState);
        tvTriggerValue.setText(trigger);
        tvSocialSituationValue.setText(socialSituation);
        tvReasonValue.setText(reason);

        // TODO: Dynamically set the mood icon based on moodState if needed.
        // e.g., if ("Happy".equalsIgnoreCase(moodState)) { imageMoodIcon.setImageResource(R.drawable.ic_happy_color); }

        // Load photo with Glide if a URL is available
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(photoUrl)
                    .into(ivMoodPhoto);
        }
    }

    /**
     * Handles the back button behavior. Customize based on sourceScreen if necessary.
     */
    private void handleBackButton() {
        // Example using the Navigation Component:
        // If you need to implement custom logic based on sourceScreen, you can do it here.
        if ("filtered".equalsIgnoreCase(sourceScreen)) {
            // TODO: Implement custom navigation logic for filtered feed if needed.
            // For now, simply pop back stack.
            androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
        } else {
            // Default: pop back stack to return to previous screen (e.g., HomeFeed)
            androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
        }
    }

}
