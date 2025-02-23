package com.kernelcrew.moodapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MyProfile extends Fragment {
    FirebaseAuth auth;

    Button signOutButton;
    NavigationBarView navigationBarView;

    BottomNavBarController navBarController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        signOutButton = view.findViewById(R.id.signOutButton);
        navigationBarView = view.findViewById(R.id.bottom_navigation);

        navigationBarView.setSelectedItemId(R.id.page_myProfile);
        navBarController = new BottomNavBarController(navigationBarView);

        signOutButton.setOnClickListener(this::onClickSignOut);

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