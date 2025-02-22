package com.kernelcrew.moodapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;

public class AuthSignUp extends Fragment {
    MaterialToolbar topAppBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_sign_up, container, false);

        topAppBar = view.findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignUp_to_authHome));

        return view;
    }
}