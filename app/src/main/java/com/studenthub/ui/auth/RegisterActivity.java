package com.studenthub.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.studenthub.R;
import com.studenthub.ui.MainActivity;

/**
 * Registration screen for new user account creation.
 */
public class RegisterActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText emailInput, passwordInput, confirmPasswordInput;
    private MaterialButton registerButton;
    private CircularProgressIndicator loadingIndicator;
    private View loginLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.back_button);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        registerButton = findViewById(R.id.register_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loginLink = findViewById(R.id.login_link);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> attemptRegister());

        loginLink.setOnClickListener(v -> finish());

        // Clear errors on focus
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) emailLayout.setError(null);
        });
        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) passwordLayout.setError(null);
        });
        confirmPasswordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) confirmPasswordLayout.setError(null);
        });
    }

    private void attemptRegister() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        String confirmPassword = confirmPasswordInput.getText() != null 
            ? confirmPasswordInput.getText().toString() : "";

        // Validate inputs
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.required_field));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.required_field));
            valid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.password_too_short));
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.required_field));
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.passwords_not_match));
            valid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (!valid) return;

        // Show loading
        setLoading(true);

        // Create Firebase account
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                setLoading(false);

                if (task.isSuccessful()) {
                    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    String errorMessage = task.getException() != null 
                        ? task.getException().getMessage() 
                        : "Registration failed";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setLoading(boolean loading) {
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? "" : getString(R.string.register));
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        confirmPasswordInput.setEnabled(!loading);
        backButton.setEnabled(!loading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
