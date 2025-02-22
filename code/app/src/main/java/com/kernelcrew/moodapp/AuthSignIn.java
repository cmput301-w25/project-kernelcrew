package com.kernelcrew.moodapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class AuthSignIn extends Fragment {
    MaterialToolbar topAppBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_sign_in, container, false);

        topAppBar = view.findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignIn_to_authHome));

        return view;
    }
}