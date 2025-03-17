package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.UserProvider;

public class OtherUserProfile extends Fragment {

    private String uidToLoad;
    private TextView usernameText;
    private TextView emailText;
    private MaterialToolbar toolbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate our layout for the fragment
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        toolbar = view.findViewById(R.id.topAppBarOther);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);

        // Set up the back button in the app bar to return to Mood Details
        toolbar.setNavigationIcon(R.drawable.ic_back); // Ensure you have a back icon resource
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Retrieve the UID passed from MoodDetails
        if (getArguments() != null) {
            uidToLoad = getArguments().getString("uid");
        }

        // Load the user details from Firebase using UserProvider
        if (uidToLoad != null) {
            UserProvider userProvider = UserProvider.getInstance();
            userProvider.addSnapshotListenerForUser(uidToLoad, (documentSnapshot, error) -> {
                if (error != null) {
                    usernameText.setText("Error loading User");
                    emailText.setText("");
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String email = documentSnapshot.getString("email");
                    usernameText.setText(username != null ? username : "Unknown User");
                    emailText.setText(email != null ? email : "No Email Provided");
                }
            });
        }

        return view;
    }
}
