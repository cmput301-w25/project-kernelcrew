package com.kernelcrew.moodapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFeed extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;

    TextView homeTextView;

    public HomeFeed() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;

        homeTextView = view.findViewById(R.id.homeTextView);
        homeTextView.setText("Currently signed in as user: " + user.getDisplayName());

        return view;
    }
}