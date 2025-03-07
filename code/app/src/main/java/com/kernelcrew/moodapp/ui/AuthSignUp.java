package com.kernelcrew.moodapp.ui;

import static android.view.View.GONE;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.kernelcrew.moodapp.R;

import java.util.HashMap;
import java.util.Objects;

/**
 * A Fragment representing the Sign-Up screen.
 * Allows the user to sign up and sign-in after creating an account.
 */
public class AuthSignUp extends Fragment {
    // Prevent spam sign ups. (if you spam the sign up button it creates accounts)
    private long lastClickTime = 0;

    // Database
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI elements
    private MaterialToolbar topAppBar;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextInputLayout usernameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextView generalErrorText;
    private Button signUpButton;

    /**
     * Class containing the details entered by the user
     */
    private class SignUpDetails {
        String userName;
        String email;
        String password;

        SignUpDetails() {
            this.userName = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
            this.email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            this.password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
        }
    }

    public interface OnValidationCompleteListener {
        /**
         * @param details the validated sign-up details, or null if validation fails.
         */
        void onValidationComplete(SignUpDetails details);
    }

    /**
     * A method of the SignUpDetails which does data validation of the details inputted and displays
     * corresponding error messages.
     *
     * @return null if invalid data, otherwise SignUpDetails
     */
    private void validateFieldsAndSignUp(OnValidationCompleteListener listener) {
        boolean error = false;

        // Clear previous errors
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        SignUpDetails details = new SignUpDetails();

        // Validate fields synchronously
        if (details.userName.isBlank()) {
            usernameLayout.setError("Please enter a username.");
            error = true;
        }

        if (!checkEmailValid(details.email)) {
            error = true;
        }

        if (!checkPasswordValid(details.password)) {
            error = true;
        }

        // Validate username asynchronously
        checkUsernameUnique(details.userName, new OnUsernameCheckListener() {
            @Override
            public void onCheckComplete(boolean isUnique) {
                if (isUnique) {
                    listener.onValidationComplete(details);
                } else {
                    usernameLayout.setError("Username is already taken!");
                }
            }
        });

        if (error || usernameLayout.getError() == "Username is already taken!") {
            listener.onValidationComplete(null);
            signUpButton.setEnabled(true);
        } else {
            listener.onValidationComplete(details);
        }
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
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.emailSignUp);
        passwordEditText = view.findViewById(R.id.passwordSignUp);
        usernameLayout = view.findViewById(R.id.usernameLayout);
        emailLayout = view.findViewById(R.id.emailLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        signUpButton = view.findViewById(R.id.signUpButtonAuthToHome);
        generalErrorText = view.findViewById(R.id.errorTextSignUp);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignUp_to_authHome));

        signUpButton.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) return;
            lastClickTime = currentTime;

            if (!signUpButton.isEnabled()) return;

            signUpButton.setEnabled(false);
            generalErrorText.setVisibility(GONE);

            validateFieldsAndSignUp(new OnValidationCompleteListener() {
                @Override
                public void onValidationComplete(SignUpDetails details) {
                    if (details != null) {
                        signUpUser(details);
                    } else {
                        // Validation failed; re-enable the button
                        signUpButton.setEnabled(true);
                    }
                }
            });
        });

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkUsernameUnique(s.toString(), new OnUsernameCheckListener() {
                    @Override
                    public void onCheckComplete(boolean isUnique) {
                        if (!isUnique) {
                            usernameLayout.setError("Username is already taken!");
                        }
                    }
                });
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

                    setGeneralError(Objects.requireNonNull(createUserTask.getException()));
                    signUpButton.setEnabled(true);
                    return;
                }

                FirebaseUser user = auth.getCurrentUser();
                WriteBatch batch = db.batch();

                // Check that there is a user
                if (user == null) {
                    deleteAuthUserIfExists();

                    setGeneralError(new Exception("The database responded, but not with a user."));
                    signUpButton.setEnabled(true);
                    return;
                }

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
                        Navigation.findNavController(requireView()).navigate(R.id.action_authSignUp_to_authHome);
                    } else {
                        deleteAuthUserIfExists();
                        setGeneralError(task.getException());
                    }
                    signUpButton.setEnabled(true);
                });

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(details.userName)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(updateProfileTask -> {
                    if (!updateProfileTask.isSuccessful()) {
                        deleteAuthUserIfExists();
                        setGeneralError(new Exception("The username update failed, please try again."));
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

    // Since Firestore operations are asynchronous, you should use a callback to handle the result.
    // https://stackoverflow.com/questions/30659569/wait-until-firebase-retrieves-data/42811962#42811962
    public interface OnUsernameCheckListener {
        void onCheckComplete(boolean isUnique);
    }

    private void checkUsernameUnique(String username, OnUsernameCheckListener listener) {
        if (username == null || username.trim().isEmpty()) {
            listener.onCheckComplete(true);
            return;
        }

        db.collection("usernames").document(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        // If the document exists, the username is taken; otherwise, it's unique.
                        boolean isUnique = !document.exists();
                        listener.onCheckComplete(isUnique);
                    } else {
                        listener.onCheckComplete(true);
                        setGeneralError(task.getException());
                    }
                });
    }

    private boolean checkEmailValid(String email) {
        if (email == null || email.isEmpty()) {
            emailLayout.setError("Please enter an email.");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email.");
            return false;
        }
        return true;
    }

    private boolean checkPasswordValid(String password) {
        if (password == null || password.isEmpty()) {
            passwordLayout.setError("Please enter a password.");
            return false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters.");
            return false;
        } else if (password.length() > 4096) {
            passwordLayout.setError("Password must be no more than 4096 characters.");
            return false;
        }
        return true;
    }

    // ChatGPT:
    //      `
    //      Here is my current code in setGeneralError, i want setGeneralError to take exceptions
    //      instead of strings, write some general errors for Firebase account creation for my sign
    //      up page. Also make the general error messages more informative. Example usage would be
    //      .addOnCompleteListener(task -> if task not successful: setGeneralError(task.exception).
    //
    //      private void setGeneralError(String message) {
    //        generalErrorText.setText(message);
    //        generalErrorText.setVisibility(VISIBLE);
    //      }
    //
    //      (Edit) Also generate the string resources and start their names with error_ (end of edit)
    //      '
    private void setGeneralError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            generalErrorText.setText(R.string.error_account_with_email_exists_already);
        } else if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    generalErrorText.setText(R.string.error_invalid_email);
                    break;
                case "ERROR_WEAK_PASSWORD":
                    generalErrorText.setText(R.string.error_password_is_too_weak);
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    generalErrorText.setText(R.string.error_email_is_already_linked);
                    break;
                case "ERROR_OPERATION_NOT_ALLOWED":
                    generalErrorText.setText(R.string.error_sign_up_is_disabled);
                    break;
                default:
                    generalErrorText.setText(R.string.error_account_creation_failed);
                    break;
            }
        } else if (exception instanceof FirebaseNetworkException) {
            generalErrorText.setText(R.string.no_internet_connection);
        } else if (exception instanceof com.google.firebase.firestore.FirebaseFirestoreException &&
                ((com.google.firebase.firestore.FirebaseFirestoreException) exception).getCode() ==
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            generalErrorText.setText(R.string.error_insufficient_firestore_permissions);
        } else {
            generalErrorText.setText(R.string.error_unexpected);
        }
        generalErrorText.setVisibility(View.VISIBLE);
    }

}