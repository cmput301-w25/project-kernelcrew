package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.UserProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OtherUserProfile extends Fragment {

    private static final String TAG = "OtherUserProfile";
    private String uidToLoad;
    private TextView usernameText;
    private TextView emailText;
    private MaterialToolbar toolbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("OtherUserProfile", "User is not authenticated!");
        } else {
            Log.d("OtherUserProfile", "User authenticated: " + currentUser.getUid());
        }

        toolbar = view.findViewById(R.id.topAppBarOther);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);

        // Setup back button to return to Mood Details
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Retrieve the UID from the navigation arguments
        if (getArguments() != null) {
            uidToLoad = getArguments().getString("uid");
            Log.d(TAG, "UID to load: " + uidToLoad);
        }

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

        return view;
    }
}
