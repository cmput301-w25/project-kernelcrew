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
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.kernelcrew.moodapp.R;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Fragment representing the Sign-Up screen.
 * Allows the user to sign up and sign-in after creating an account.
 */
public class AuthSignUp extends Fragment {
    // Database
    FirebaseAuth auth;
    FirebaseFirestore db;

    // UI elements
    MaterialToolbar topAppBar;
    EditText userNameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    Button signUpButton;

    /**
     * Class containing the details entered by the user
     * */
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

    /**
     * A method of the SignUpDetails which does data validation of the details inputted and displays
     *      corresponding error messages.
     * @return
     *      null if invalid data, otherwise SignUpDetails
     * */
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

    /**
     * Inflates the fragment layout and initializes UI elements.
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_sign_up, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        topAppBar = view.findViewById(R.id.topAppBar);
        userNameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signUpButton = view.findViewById(R.id.signUpButtonAuth);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignUp_to_authHome));

        signUpButton.setOnClickListener(btnView -> {
            SignUpDetails details = validateFields();

            if (details != null) {
                checkUsernameAndSignUp(details);
            }
        });

        return view;
    }

    /**
     * Check for if the username is unique; if it is unique, then sign the user up
     * @param details
     *      The sign in user information we need to sign up with. details.userName is the checked value.
     * */
    private void checkUsernameAndSignUp(SignUpDetails details) {
        db.collection("usernames")
                .whereEqualTo("username", details.userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.isEmpty()) {
                            signUpUser(details);
                        } else {
                            userNameEditText.setError("Username is already taken");
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Error in check username: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Sign the user up, then create and save the user to firestore users collection
     * @param details
     *      The users information needed to sign them up
     * */
    private void signUpUser(SignUpDetails details) {
        auth.createUserWithEmailAndPassword(details.email, details.password)
            .addOnCompleteListener(attempt -> {
                if (attempt.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();

                    if (user == null) {
                        Toast.makeText(requireContext(),
                                "Error in signing up user: A user was not returned by the database.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(details.userName)
                            .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            WriteBatch batch = db.batch();

                            // Add user document
                            batch.set(db.collection("users").document(user.getUid()),
                                    new HashMap<String, Object>() {{
                                        put("uid", user.getUid());
                                        put("email", details.email);
                                        put("username", details.userName);
                                    }});

                            // Add username document
                            batch.set(db.collection("usernames").document(details.userName),
                                    new HashMap<String, Object>() {{
                                        put("uid", user.getUid());
                                    }});

                            // Commit Firestore changes
                            batch.commit().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).navigate(R.id.action_authSignUp_to_authHome);
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(requireContext(),
                            "Error in signing up user: A user was not returned by the database.",
                            Toast.LENGTH_SHORT).show();
                }
            });
    }
}
