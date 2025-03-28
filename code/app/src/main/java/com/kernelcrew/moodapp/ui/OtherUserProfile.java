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
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowProvider;
import com.kernelcrew.moodapp.data.UserProvider;
import com.kernelcrew.moodapp.utils.NotificationHelper;

public class OtherUserProfile extends Fragment {

    private static final String TAG = "OtherUserProfile";
    private String uidToLoad;
    private TextView usernameText;
    private TextView emailText;
    private MaterialToolbar toolbar;
    private Button followButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        toolbar = view.findViewById(R.id.topAppBarOther);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        followButton = view.findViewById(R.id.followButton);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        if (getArguments() != null) {
            uidToLoad = getArguments().getString("uid");
            Log.d(TAG, "UID to load: " + uidToLoad);
        }

        if (uidToLoad != null) {
            UserProvider.getInstance().addSnapshotListenerForUser(uidToLoad, (doc, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading user: ", error);
                    usernameText.setText("Error loading user");
                    emailText.setText("");
                    return;
                }
                if (doc != null && doc.exists()) {
                    String username = doc.getString("username");
                    usernameText.setText(username != null ? username : "Unknown User");
                    String email = doc.getString("email");
                    emailText.setText(email != null ? email : "No Email Provided");
                }
            });
        } else {
            usernameText.setText("No user ID provided");
            emailText.setText("");
        }

        if (currentUser != null && uidToLoad != null && uidToLoad.equals(currentUser.getUid())) {
            followButton.setVisibility(View.GONE);
        } else if (currentUser != null && uidToLoad != null) {
            String currentUid = currentUser.getUid();
            FollowProvider provider = FollowProvider.getInstance();

            provider.isFollowing(currentUid, uidToLoad)
                    .addOnSuccessListener(isFollowing -> {
                        if (isFollowing) {
                            followButton.setText("Unfollow");
                            followButton.setOnClickListener(v ->
                                    provider.unfollow(currentUid, uidToLoad)
                                            .addOnSuccessListener(a -> followButton.setText("Follow"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Unfollow failed", e))
                            );
                        } else {
                            provider.hasPendingRequest(uidToLoad, currentUid)
                                    .addOnSuccessListener(isRequested -> {
                                        if (isRequested) {
                                            followButton.setText("Requested");
                                            followButton.setEnabled(false);
                                        } else {
                                            followButton.setText("Follow");
                                            followButton.setEnabled(true);
                                            followButton.setOnClickListener(v -> {
                                                provider.sendRequest(uidToLoad, currentUid)
                                                        .addOnSuccessListener(a -> {
                                                            followButton.setText("Requested");
                                                            followButton.setEnabled(false);
                                                        })
                                                        .addOnFailureListener(e -> Log.e(TAG, "Request failed", e));
                                            });
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error checking follow status", e));
        }

        return view;
    }
}
