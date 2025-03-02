package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;
import com.google.firebase.firestore.FirebaseFirestore;


public class MyProfile extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;

    TextView usernameText;

    TextView followersCountText;
    TextView followingCountText;
    ImageView profileImage;
    Button signOutButton;
    NavigationBarView navigationBarView;
    BottomNavBarController navBarController;

    private Button followersButton;
    private Button followingButton;
    private Button moodHistoryButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        usernameText = view.findViewById(R.id.username_text);
        profileImage = view.findViewById(R.id.profile_image);

        signOutButton = view.findViewById(R.id.signOutButton);
        followersButton = view.findViewById(R.id.followers_button);
        followingButton = view.findViewById(R.id.following_button);
        moodHistoryButton = view.findViewById(R.id.mood_history_button);

        navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_myProfile);
        navBarController = new BottomNavBarController(navigationBarView);


        // Set click listeners
        signOutButton.setOnClickListener(this::onClickSignOut);
        followersButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followersPage));
        followingButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followingPage));

        // User details
        if (user != null) {
            usernameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Guest User");
            if (user.getPhotoUrl() != null) {
                // Code to load the user photo from URL can be added here
            } else {
                // Use ic_profile as the default profile image.
                profileImage.setImageResource(R.drawable.ic_person);
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Long followersCount = document.getLong("followersCount");
                            Long followingCount = document.getLong("followingCount");

                            followersButton.setText("Followers: " + (followersCount != null ? followersCount : 0));
                            followingButton.setText("Following: " + (followingCount != null ? followingCount : 0));
                        }
                    })
                    .addOnFailureListener(e -> {
                        followersButton.setText("Followers: 0");
                        followingButton.setText("Following: 0");
                    });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }

    private void onClickSignOut(View btnView) {
        auth.signOut();
        Navigation.findNavController(btnView).navigate(R.id.authHome);
    }
}
