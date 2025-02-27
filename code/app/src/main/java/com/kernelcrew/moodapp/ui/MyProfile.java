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
import com.kernelcrew.moodapp.R;


public class MyProfile extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;


    TextView usernameText;
    ImageView profileImage;
    Button signOutButton;
    NavigationBarView navigationBarView;


    BottomNavBarController navBarController;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);


        usernameText = view.findViewById(R.id.username_text);
        profileImage = view.findViewById(R.id.profile_image);


        signOutButton = view.findViewById(R.id.signOutButton);
        followersButton = view.findViewById(R.id.followers_button);
        followingButton = view.findViewById(R.id.following_button);


        navigationBarView = view.findViewById(R.id.bottom_navigation);


        navigationBarView.setSelectedItemId(R.id.page_myProfile);
        navBarController = new BottomNavBarController(navigationBarView);


        signOutButton.setOnClickListener(this::onClickSignOut);




        //User details - in progress
        if (user != null) {
            usernameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Guest User");
            if (user.getPhotoUrl() != null) {
                // Set a default image
            } else {
                profileImage.setImageResource();
            }
        }


        // Set click listeners for buttons
        signOutButton.setOnClickListener(this::onClickSignOut);
        followersButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followersPage));
        followingButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followingPage));
        moodHistoryButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_moodHistoryPage));


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }


    /**
     * Sign the user out and return to the auth home screen.
     * @param btnView View of the sign out button
     */
    private void onClickSignOut(View btnView) {
        auth.signOut();
        Navigation.findNavController(btnView).navigate(R.id.authHome);
    }
}
