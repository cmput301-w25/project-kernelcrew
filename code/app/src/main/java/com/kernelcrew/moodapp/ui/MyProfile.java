package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;
import com.kernelcrew.moodapp.data.FollowProvider;
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
        Button followRequestsButton = view.findViewById(R.id.followRequestsButton);
        followRequestsButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followRequestsFragment)
        );

        signOutButton.setOnClickListener(this::onClickSignOut);
        followersButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followersPage));
        followingButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_myProfile_to_followingPage));
        Bundle bundle = new Bundle();
        bundle.putString("sourceScreen", "profile");
        moodHistoryButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_myProfile_to_moodHistoryPage, bundle)
        );

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

        if (user != null && uidToLoad.equals(user.getUid())) {
            usernameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Guest User");
            if (user.getPhotoUrl() != null) {
                // Code to load the user photo from URL can be added here
            } else {
                profileImage.setImageResource(R.drawable.ic_person);
            }
        }

        if (uidToLoad != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uidToLoad)
                    .collection("followers")
                    .addSnapshotListener((snap, e) -> {
                        int count = (snap == null) ? 0 : snap.size();
                        followersButton.setText("Followers: " + count);
                    });

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uidToLoad)
                    .collection("following")
                    .addSnapshotListener((snap, e) -> {
                        int count = (snap == null) ? 0 : snap.size();
                        followingButton.setText("Following: " + count);
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
        NotificationManagerCompat manager = NotificationManagerCompat.from(requireContext());
        manager.cancelAll();
        auth.signOut();
        Navigation.findNavController(btnView).navigate(R.id.authHome);
    }
}
