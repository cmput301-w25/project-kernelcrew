package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;

public class HomeFeed extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;

    TextView homeTextView;
    NavigationBarView navigationBar;
    BottomNavBarController navBarController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;

        homeTextView = view.findViewById(R.id.homeTextView);
        navigationBar = view.findViewById(R.id.bottom_navigation);

        navBarController = new BottomNavBarController(navigationBar);

        homeTextView.setText("Currently signed in as user: " + user.getDisplayName());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navBarController.bind(view);
    }
}