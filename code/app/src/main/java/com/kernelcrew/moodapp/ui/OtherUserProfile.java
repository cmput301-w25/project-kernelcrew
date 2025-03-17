package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.UserProvider;

public class OtherUserProfile extends Fragment {

    private String uidToLoad;
    private TextView usernameText;
    private ImageView profileImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (create the file below)
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        usernameText = view.findViewById(R.id.username_text);
        profileImage = view.findViewById(R.id.profile_image);

        // Retrieve UID from arguments passed via navigation
        if (getArguments() != null) {
            uidToLoad = getArguments().getString("uid");
        }

        // Load the other user's details from Firestore using your UserProvider
        if (uidToLoad != null) {
            UserProvider userProvider = UserProvider.getInstance();
            userProvider.addSnapshotListenerForUser(uidToLoad, (documentSnapshot, error) -> {
                if (error != null) {
                    usernameText.setText("Error loading user");
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    usernameText.setText(username != null ? username : "Unknown User");
                    // Optionally, load and set the user's profile image here.
                }
            });
        }

        return view;
    }
}
