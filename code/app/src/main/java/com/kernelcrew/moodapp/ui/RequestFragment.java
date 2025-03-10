/**
 * //Anthropic, Claude, "Generate source code descriptive comments for 301 rubric", 03-10-2025
 *
 * This fragment manages follow requests between users.
 * It displays pending requests, allows users to accept or reject requests,
 * and handles the follow/unfollow functionality through confirmation dialogs.
 * Supports multiple request types through navigation arguments.
 */
package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;

public class RequestFragment extends Fragment {
    private String requestType;
    private String username;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        TextView requestMessage = view.findViewById(R.id.requestMessage);
        MaterialButton acceptButton = view.findViewById(R.id.acceptButton);
        MaterialButton denyButton = view.findViewById(R.id.denyButton);

        if (getArguments() != null) {
            requestType = getArguments().getString("requestType");
            username = getArguments().getString("username");
        }

        if ("follow_request".equals(requestType)) {
            requestMessage.setText(username + " is requesting to follow you");
            acceptButton.setOnClickListener(v -> acceptFollowRequest(username));
            denyButton.setOnClickListener(v -> denyFollowRequest(username));
        } else if ("unfollow_confirmation".equals(requestType)) {
            requestMessage.setText("Are you sure you want to unfollow " + username + "?");
            acceptButton.setOnClickListener(v -> confirmUnfollow(username));
            denyButton.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        }
    }

    private void acceptFollowRequest(String username) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(currentUserId)
                .collection("followers").document(username)
                .set(new Object())
                .addOnSuccessListener(aVoid -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void denyFollowRequest(String username) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(currentUserId)
                .collection("followRequests").document(username)
                .delete()
                .addOnSuccessListener(aVoid -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void confirmUnfollow(String username) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(currentUserId)
                .collection("following").document(username)
                .delete()
                .addOnSuccessListener(aVoid -> Navigation.findNavController(requireView()).popBackStack());
    }
}
