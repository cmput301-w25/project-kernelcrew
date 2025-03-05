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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.kernelcrew.moodapp.R;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * A Fragment representing the Sign-Up screen.
 * Allows the user to sign up and sign-in after creating an account.
 */
public class AuthSignUp extends Fragment {
    // Prevent spam sign ups. (if you spam the sign up button it creates accounts)
    private long lastClickTime = 0;
    private boolean isSigningUp = false;

    // Database
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI elements
    private MaterialToolbar topAppBar;
    private EditText userNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;

    /**
     * Class containing the details entered by the user
     */
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
     * corresponding error messages.
     *
     * @return null if invalid data, otherwise SignUpDetails
     */
    private @Nullable SignUpDetails validateFields() {
        userNameEditText.setError(null);
        emailEditText.setError(null);
        passwordEditText.setError(null);

        SignUpDetails details = new SignUpDetails();

        if (details.userName.isBlank()) {
            userNameEditText.setError("Please enter a username.");
            return null;
        }
        if (details.email.isBlank()) {
            emailEditText.setError("Please enter an email.");
            return null;
        }
        if (details.password.isBlank()) {
            passwordEditText.setError("Please enter a password.");
            return null;
        }
        return details;
    }

    /**
     * Inflates the fragment layout and initializes UI elements.
     */
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
        signUpButton = view.findViewById(R.id.signUpButtonAuthToHome);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignUp_to_authHome));

        signUpButton.setOnClickListener(v -> {
            SignUpDetails details = validateFields();
            if (details != null) {
                signUpUser(details);
            }
        });

        signUpButton.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) return; // Ignore if less than 500ms since last click
            lastClickTime = currentTime;

            if (isSigningUp) return;

            SignUpDetails details = validateFields();
            if (details != null) {
                isSigningUp = true;
                signUpButton.setEnabled(false);
                signUpUser(details);
            }
        });

        return view;
    }

    /**
     * Sign the user up, then create and save the user to firestore users collection
     *
     * @param details The users information needed to sign them up
     */
    private void signUpUser(SignUpDetails details) {
        auth.createUserWithEmailAndPassword(details.email, details.password)
            .addOnCompleteListener(createUserTask -> {
                if (!createUserTask.isSuccessful()) {
                    deleteAuthUserIfExists();
                    isSigningUp = false;
                    signUpButton.setEnabled(true);
                    return;
                }

                FirebaseUser user = auth.getCurrentUser();
                if (user == null) {
                    deleteAuthUserIfExists();
                    isSigningUp = false;
                    signUpButton.setEnabled(true);
                    return;
                }

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(details.userName)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(updateProfileTask -> {
                    if (updateProfileTask.isSuccessful()) {
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
                            } else {
                                deleteAuthUserIfExists();
                                Toast.makeText(requireContext(),
                                        "Error creating account: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();

                            }
                            isSigningUp = false;
                            signUpButton.setEnabled(true);
                        });
                    } else {
                        deleteAuthUserIfExists();
                        isSigningUp = false;
                        signUpButton.setEnabled(true);
                    }
                });
            });
    }

    /**
     * Cleans up the auth user that may be left as a result of a sign up attempt
     * */
    private void deleteAuthUserIfExists() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete()
                    .addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Log.d("SignUp", "Orphaned Auth user deleted.");
                        } else {
                            Log.e("SignUp", "Failed to delete orphaned user: " +
                                    deleteTask.getException().getMessage());
                        }
                    });
        }
    }
}