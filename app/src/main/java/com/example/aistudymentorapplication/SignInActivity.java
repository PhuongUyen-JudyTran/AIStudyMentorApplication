package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnSignUp;
    private TextView tvError;
    private FirebaseAuth mAuth;

    // Email and Password demo
    // String email = "aimentor@gmail.com";
    // String pass = "admin123@App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
                {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        mAuth = FirebaseAuth.getInstance();


        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvError = findViewById(R.id.tvError);

        btnSignIn.setOnClickListener(v -> signInUser());

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignInActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });
    }


    private void signInUser() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            showError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    btnSignIn.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            tvError.setVisibility(View.GONE);

                            Toast.makeText(
                                    SignInActivity.this,
                                    "Sign in successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    SignInActivity.this,
                                    HomeActivity.class
                            );

                            startActivity(intent);
                            finish();
                        }

                    } else {
                        showError("Email or password is incorrect");
                        Log.e("SignIn", "Sign in failed", task.getException()
                        );
                    }
                });
    }

            private void showError(String message) {
                tvError.setText(message);
                tvError.setVisibility(View.VISIBLE);
            }

    private void handleSignInError(Exception exception) {

        if (exception == null) {
            showError("Sign in failed");
            return;
        }

        Log.e("SignIn", "Sign in failed", exception);

        if (exception instanceof FirebaseAuthInvalidUserException) {

            FirebaseAuthException authException = (FirebaseAuthException) exception;

            String errorCode = authException.getErrorCode();

            if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                showError("Account does not exist");
                etEmail.requestFocus();

            } else if ("ERROR_USER_DISABLED".equals(errorCode)) {
                showError("This account has been disabled");

            } else {
                showError("Invalid user account");
            }

        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {

            FirebaseAuthException authException =
                    (FirebaseAuthException) exception;

            String errorCode = authException.getErrorCode();

            if ("ERROR_INVALID_EMAIL".equals(errorCode)) {
                showError("Invalid email format");
                etEmail.requestFocus();

            } else if ("ERROR_WRONG_PASSWORD".equals(errorCode)) {
                showError("Incorrect password");
                etPassword.requestFocus();

            } else {
                showError("Email or password is incorrect");
                etPassword.requestFocus();
            }

        } else {
            showError("Error: " + exception.getMessage());
        }
    }
}
