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
import com.kernelcrew.moodapp.data.UserProvider;

public class MyProfile extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;

    TextView usernameText;
    ImageView profileImage;
    Button signOutButton;
    NavigationBarView navigationBarView;
    BottomNavBarController navBarController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        usernameText = view.findViewById(R.id.username_text);
        profileImage = view.findViewById(R.id.profile_image);

        signOutButton = view.findViewById(R.id.signOutButton);
        Button followersButton = view.findViewById(R.id.followers_button);
        Button followingButton = view.findViewById(R.id.following_button);
        Button moodHistoryButton = view.findViewById(R.id.mood_history_button);

        navigationBarView = view.findViewById(R.id.bottom_navigation);
        navigationBarView.setSelectedItemId(R.id.page_myProfile);
        navBarController = new BottomNavBarController(navigationBarView);

        // Set click listeners
        signOutButton.setOnClickListener(this::onClickSignOut);
//        followersButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followersPage));
//        followingButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followingPage));
        Bundle bundle = new Bundle();
        bundle.putString("sourceScreen", "profile");
        moodHistoryButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_myProfile_to_moodHistoryPage, bundle)
        );

        // Retrieve UID from arguments if available; otherwise, use the current user's UID.
        String uidToLoad = null;
        if (getArguments() != null) {
            String uidArg = getArguments().getString("uid");
            if (uidArg != null && !uidArg.isEmpty()) {
                uidToLoad = uidArg;
            }
        }
        if (uidToLoad == null && user != null) {
            uidToLoad = user.getUid();
        }

        // If loading own profile, use auth user details first
        if (user != null && uidToLoad.equals(user.getUid())) {
            usernameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Guest User");
            if (user.getPhotoUrl() != null) {
                // Code to load the user photo from URL can be added here
            } else {
                profileImage.setImageResource(R.drawable.ic_person);
            }
        }

        // Listen for profile changes using the selected UID
        if (uidToLoad != null) {
            UserProvider userProvider = UserProvider.getInstance();
            userProvider.addSnapshotListenerForUser(uidToLoad, (documentSnapshot, error) -> {
                if (error != null) {
                    followersButton.setText("Followers: 0");
                    followingButton.setText("Following: 0");
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Long followersCount = documentSnapshot.getLong("followersCount");
                    Long followingCount = documentSnapshot.getLong("followingCount");

                    followersButton.setText("Followers: " + (followersCount != null ? followersCount : 0));
                    followingButton.setText("Following: " + (followingCount != null ? followingCount : 0));
                }
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
