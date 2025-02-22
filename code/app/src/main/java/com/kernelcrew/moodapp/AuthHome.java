package com.kernelcrew.moodapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AuthHome extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_home, container, false);

        view.findViewById(R.id.signInButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_authHome_to_authSignIn));
        view.findViewById(R.id.signUpButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_authHome_to_authSignUp));

        return view;
    }
}