package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.utils.NotificationHelper;

import java.util.Collections;

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
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        NotificationHelper notificationHelper = new NotificationHelper(requireContext());
        FollowProvider.getInstance()
                .acceptRequest(me, username, notificationHelper)
                .addOnSuccessListener(aVoid ->
                        Navigation.findNavController(requireView()).popBackStack());
    }


    private void denyFollowRequest(String username) {
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowProvider.getInstance()
                .deleteRequest(me, username)
                .addOnSuccessListener(aVoid ->
                        Navigation.findNavController(requireView()).popBackStack());
    }


    private void confirmUnfollow(String username) {
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FollowProvider.getInstance()
                .unfollow(me, username)
                .addOnSuccessListener(aVoid -> Navigation.findNavController(requireView()).popBackStack())
                .addOnFailureListener(e -> Log.e("RequestFragment", "Unfollow failed", e));
    }
}
