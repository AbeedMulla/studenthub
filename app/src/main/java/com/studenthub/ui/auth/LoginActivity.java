package com.studenthub.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
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
 * Login screen for Firebase email/password authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private CircularProgressIndicator loadingIndicator;
    private View registerLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Check if already logged in
        if (auth.getCurrentUser() != null) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        registerLink = findViewById(R.id.register_link);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Clear errors on text change
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) emailLayout.setError(null);
        });
        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) passwordLayout.setError(null);
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

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
        } else {
            passwordLayout.setError(null);
        }

        if (!valid) return;

        // Show loading
        setLoading(true);

        // Attempt Firebase login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                setLoading(false);

                if (task.isSuccessful()) {
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    String errorMessage = task.getException() != null 
                        ? task.getException().getMessage() 
                        : "Login failed";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "" : getString(R.string.login));
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
