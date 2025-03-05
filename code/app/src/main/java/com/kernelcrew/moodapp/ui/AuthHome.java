package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kernelcrew.moodapp.R;

/**
 * A fragment representing the authentication home screen.
 * Allows navigation to sign-in or sign-up authentication screens.
 * If a user is already logged in, they are automatically redirected to the home feed.
 */
public class AuthHome extends Fragment {
    FirebaseAuth auth;

    /**
     * Called to have the fragment instantiate its user interface view.
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.fragment_auth_home, container, false);

        view.findViewById(R.id.buttonInitialToSignIn).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_authHome_to_authSignIn));
        view.findViewById(R.id.buttonInitialToSignUp).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_authHome_to_authSignUp));

        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * Checks if a user is already logged in and navigates to the home feed if they are.
     * */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate to the home feed if the user is already logged in
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Navigation.findNavController(view).navigate(R.id.homeFeed);
        }
    }
}