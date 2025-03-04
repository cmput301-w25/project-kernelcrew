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
import com.google.firebase.firestore.FirebaseFirestore;
import com.kernelcrew.moodapp.R;

import org.jetbrains.annotations.Nullable;

/**
 * A Fragment representing the Sign-In screen.
 * Allows users to log in using their email and password.
 */
public class AuthSignIn extends Fragment {
    FirebaseAuth auth;

    MaterialToolbar topAppBar;

    EditText emailEditText;
    EditText passwordEditText;
    Button signInButton;

    /**
     * Class containing the details entered by the user
     * */
    private class SignInDetails {
        String email;
        String password;

        SignInDetails() {
            this.email = emailEditText.getText().toString();
            this.password = passwordEditText.getText().toString();
        }
    }

    /**
     * A method of the SignUpDetails which does data validation of the details inputted and displays
     *      corresponding error messages.
     * @return
     *      null if invalid data, otherwise SignUpDetails
     * */
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

    /**
     * Inflates the fragment layout and initializes UI elements.
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_sign_in, container, false);

        auth = FirebaseAuth.getInstance();

        topAppBar = view.findViewById(R.id.topAppBar);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signInButton = view.findViewById(R.id.signInButtonAuth);

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
