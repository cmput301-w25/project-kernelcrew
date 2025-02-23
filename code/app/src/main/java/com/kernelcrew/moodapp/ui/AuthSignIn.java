package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.kernelcrew.moodapp.R;

import org.jetbrains.annotations.Nullable;

public class AuthSignIn extends Fragment {
    FirebaseAuth auth;

    MaterialToolbar topAppBar;

    EditText emailEditText;
    EditText passwordEditText;
    Button signInButton;

    private class SignInDetails {
        String email;
        String password;

        SignInDetails() {
            this.email = emailEditText.getText().toString();
            this.password = passwordEditText.getText().toString();
        }
    }

    private @Nullable SignInDetails validateFields() {
        emailEditText.setError(null);
        passwordEditText.setError(null);

        SignInDetails details = new SignInDetails();

        if (details.email.isBlank()) {
            emailEditText.setError("An email is required");
            return null;
        }

        if (details.password.isBlank()) {
            passwordEditText.setError("A password is required");
            return null;
        }

        return details;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_sign_in, container, false);

        auth = FirebaseAuth.getInstance();

        topAppBar = view.findViewById(R.id.topAppBar);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signInButton = view.findViewById(R.id.signInButton);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignIn_to_authHome));

        signInButton.setOnClickListener((btnView) -> {
            SignInDetails details = validateFields();
            if (details != null) {
                auth.signInWithEmailAndPassword(details.email, details.password)
                        .addOnSuccessListener(result -> {
                            Log.i("Login", "Logged in as user with id: " + result.getUser().getUid());
                            Navigation.findNavController(btnView).navigate(R.id.action_authSignIn_to_homeFeed);
                        });
            }
        });

        return view;
    }
}