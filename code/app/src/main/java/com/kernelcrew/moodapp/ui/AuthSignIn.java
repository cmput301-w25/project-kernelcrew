package com.kernelcrew.moodapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.kernelcrew.moodapp.R;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A Fragment representing the Sign-In screen.
 * Allows users to log in using their email and password.
 */
public class AuthSignIn extends Fragment {
    // Database
    private FirebaseAuth auth;

    // UI elements
    private MaterialToolbar topAppBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private Button signInButton;
    private TextView generalErrorText;

    /**
     * Class containing the details entered by the user
     */
    private class SignInDetails {
        String email;
        String password;

        SignInDetails() {
            this.email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            this.password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
        }
    }

    /**
     * A method of the SignUpDetails which does data validation of the details inputted and displays
     *      corresponding error messages.
     * @return
     *      null if invalid data, otherwise SignUpDetails
     */
    private @Nullable SignInDetails validateFields() {
        boolean error = false   ;

        emailLayout.setError(null);
        passwordLayout.setError(null);

        SignInDetails details = new SignInDetails();

        if (!checkEmailValid(details.email))
            error = true;

        if (!checkPasswordValid(details.password))
            error = true;

        return error ? null : details;
    }

    /**
     * Inflates the fragment layout and initializes UI elements.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_sign_in, container, false);

        auth = FirebaseAuth.getInstance();

        topAppBar = view.findViewById(R.id.topAppBar);
        emailEditText = view.findViewById(R.id.emailSignIn);
        passwordEditText = view.findViewById(R.id.passwordSignIn);
        emailLayout = view.findViewById(R.id.emailLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        generalErrorText = view.findViewById(R.id.errorTextSignIn);
        signInButton = view.findViewById(R.id.signInButtonAuthToHome);

        topAppBar.setNavigationOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_authSignIn_to_authHome));

        signInButton.setOnClickListener((btnView) -> {
            SignInDetails details = validateFields();
            if (details != null) {
                auth.signInWithEmailAndPassword(details.email, details.password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Navigation.findNavController(btnView).navigate(R.id.action_authSignIn_to_homeFeed);
                            } else {
                                setGeneralError(Objects.requireNonNull(task.getException()));
                            }
                        });
            }
        });


        return view;
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

    // ChatGPT: `Do the same thing you did for sign up, but for sign in now.`
    private void setGeneralError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    generalErrorText.setText(R.string.error_invalid_email);
                    break;
                case "ERROR_USER_NOT_FOUND":
                    generalErrorText.setText(R.string.error_user_not_found);
                    break;
                case "ERROR_WRONG_PASSWORD":
                    generalErrorText.setText(R.string.error_wrong_password);
                    break;
                case "ERROR_USER_DISABLED":
                    generalErrorText.setText(R.string.error_user_disabled);
                    break;
                case "ERROR_TOO_MANY_REQUESTS":
                    generalErrorText.setText(R.string.error_too_many_requests);
                    break;
                default:
                    generalErrorText.setText(R.string.error_login_fail_default);
                    break;
            }
        } else {
            generalErrorText.setText(R.string.error_unexpected);
        }

        generalErrorText.setVisibility(View.VISIBLE);
    }
}
