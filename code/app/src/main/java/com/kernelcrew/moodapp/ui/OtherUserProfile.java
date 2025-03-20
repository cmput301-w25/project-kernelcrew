package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.UserProvider;

import java.util.Collections;

public class OtherUserProfile extends Fragment {

    private static final String TAG = "OtherUserProfile";

    // We'll store the user's UID that we're viewing
    private String uidToLoad;

    // UI elements
    private TextView usernameText;
    private TextView emailText;
    private MaterialToolbar toolbar;
    private Button followButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User is not authenticated!");
        } else {
            Log.d(TAG, "User authenticated: " + currentUser.getUid());
        }

        toolbar = view.findViewById(R.id.topAppBarOther);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);

        // Setup back button to return to previous screen
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Retrieve the UID from the navigation arguments
        if (getArguments() != null) {
            uidToLoad = getArguments().getString("uid");
            Log.d(TAG, "UID to load: " + uidToLoad);
        }

        // Listen for changes in the target user's document
        if (uidToLoad != null) {
            UserProvider.getInstance().addSnapshotListenerForUser(uidToLoad, (documentSnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading user: ", error);
                    usernameText.setText("Error loading user");
                    emailText.setText("");
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "User document data: " + documentSnapshot.getData());
                    String username = documentSnapshot.getString("username");
                    if (username == null) {
                        username = documentSnapshot.getString("name");
                    }
                    usernameText.setText(username != null ? username : "Unknown User");

                    // Get email from Firestore
                    String email = documentSnapshot.getString("email");
                    emailText.setText(email != null ? email : "No Email Provided");
                } else {
                    usernameText.setText("User not found");
                    emailText.setText("");
                }
            });
        } else {
            usernameText.setText("No user ID provided");
            emailText.setText("");
        }

        // Initialize and wire up the Follow button
        followButton = view.findViewById(R.id.followButton);
        followButton.setOnClickListener(v -> sendFollowRequest());

        return view;
    }

    /**
     * Sends a follow request from the current user to the user with uidToLoad.
     */
    private void sendFollowRequest() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Current user is null; cannot send follow request.");
            return;
        }
        if (uidToLoad == null) {
            Log.e(TAG, "Target user ID is null; cannot send follow request.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(uidToLoad)
                .collection("followRequests")
                .document(currentUser.getUid())
                .set(Collections.emptyMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Follow request sent successfully.");
                    // Provide user feedback: disable the button and update text
                    followButton.setEnabled(false);
                    followButton.setText("Requested");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send follow request", e));
    }
}
