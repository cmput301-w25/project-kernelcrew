package com.kernelcrew.moodapp.ui;

import static com.kernelcrew.moodapp.ui.MoodIconUtil.getMoodIconResource;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.MoodEvent;
import com.kernelcrew.moodapp.data.MoodEventProvider;

public class MoodDetails extends Fragment implements DeleteDialogFragment.DeleteDialogListener {

    private MaterialToolbar toolbar;
    private ImageView imageMoodIcon, ivMoodPhoto;
    private TextView tvMoodState, tvTriggerValue, tvSocialSituationValue, tvReasonValue;
    private Chip tvUsernameDisplay;
    private Button btnEditMood;

    private Button btnDeleteMood;
    private FirebaseFirestore db;
    private MoodEventProvider provider;

    // Document ID for the mood event and source of navigation
    private String moodEventId;
    private String sourceScreen;

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

        tvUsernameDisplay = view.findViewById(R.id.tvUsernameDisplay);
        toolbar = view.findViewById(R.id.moodDetailsToolbar);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        tvMoodState = view.findViewById(R.id.tvMoodState);
        tvSocialSituationValue = view.findViewById(R.id.tvSocialSituationValue);
        tvReasonValue = view.findViewById(R.id.tvReasonValue);
        ivMoodPhoto = view.findViewById(R.id.ivMoodPhoto);
        btnEditMood = view.findViewById(R.id.btnEditMood);
        btnDeleteMood = view.findViewById(R.id.btnDeleteMood);

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

        Bitmap photo = moodEvent.getPhoto();
        if (photo != null) {
            ivMoodPhoto.setImageBitmap(photo);
        }

        // Once we have userId, fetch user doc from Firestore
        if (userId != null && !userId.isEmpty()) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username == null) {
                                username = documentSnapshot.getString("name");
                            }

                            if (username == null) {
                                username = "UnknownUser";
                            }
                            tvUsernameDisplay.setText("@" + username);

                            // Navigate to OtherUserProfile on click
                            tvUsernameDisplay.setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putString("uid", userId);
                                NavHostFragment.findNavController(MoodDetails.this)
                                        .navigate(R.id.otherUserProfile, args);
                            });

                        } else {
                            tvUsernameDisplay.setText("User not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUsernameDisplay.setText("Error loading user");
                    });
        } else {
            // If no user ID, show something else or keep blank
            tvUsernameDisplay.setText("No user ID provided");
        }

        // 1) Get the current user from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser.getUid();

        // 2) Compare the mood's owner (userId) to the current user’s UID
        if (currentUser != null && userId != null && userId.equals(currentUserUid)) {
            // The current user OWNS this mood => show the edit/delete buttons
            btnEditMood.setVisibility(View.VISIBLE);
            btnDeleteMood.setVisibility(View.VISIBLE);
        } else {
            // The current user does NOT own it => hide them
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
