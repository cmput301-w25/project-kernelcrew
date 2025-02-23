package com.kernelcrew.moodapp.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kernelcrew.moodapp.R;

import org.jetbrains.annotations.Nullable;

public class AuthSignUp extends Fragment {
    FirebaseAuth auth;

    MaterialToolbar topAppBar;

    EditText userNameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    Button signUpButton;

    private class SignUpDetails {
        String userName;
        String email;
        String password;

        SignUpDetails() {
            this.userName = userNameEditText.getText().toString();
            this.email = emailEditText.getText().toString();
            this.password = passwordEditText.getText().toString();
        }
    }

    private @Nullable SignUpDetails validateFields() {
        userNameEditText.setError(null);
        emailEditText.setError(null);
        passwordEditText.setError(null);

        SignUpDetails details = new SignUpDetails();

        if (details.userName.isBlank()) {
            userNameEditText.setError("A user name is required");
            return null;
        }

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
        View view = inflater.inflate(R.layout.fragment_auth_sign_up, container, false);

        auth = FirebaseAuth.getInstance();

        topAppBar = view.findViewById(R.id.topAppBar);
        userNameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signUpButton = view.findViewById(R.id.signUpButton);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignUp_to_authHome));

        signUpButton.setOnClickListener((btnView) -> {
            SignUpDetails details = validateFields();
            if (details != null) {
                auth.createUserWithEmailAndPassword(details.email, details.password)
                        .addOnSuccessListener(result -> {
                            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(details.userName)
                                    .build();

                            result.getUser()
                                    .updateProfile(changeRequest)
                                    .addOnSuccessListener(_void -> {
                                        Log.i("Signup", "User created with id: " + auth.getCurrentUser().getUid());
                                        Navigation.findNavController(btnView).navigate(R.id.action_authSignUp_to_homeFeed);
                                    });
                        });
            }
        });

        return view;
    }
}