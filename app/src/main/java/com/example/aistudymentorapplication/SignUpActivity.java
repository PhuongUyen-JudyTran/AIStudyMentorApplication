package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnSignIn;
    private TextView tvError;

    private FirebaseAuth mAuth;

    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])" +
                    "(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvError = findViewById(R.id.tvError);

        btnSignUp.setOnClickListener(v -> {

            hideError();

            String email =
                    etEmail.getText().toString().trim();

            String password =
                    etPassword.getText().toString().trim();

            String confirmPassword =
                    etConfirmPassword.getText().toString().trim();

            if (validateForm(email, password, confirmPassword)) {
                registerUser(email, password);
            }
        });

        btnSignIn.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SignUpActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
            finish();
        });
    }

    private void registerUser(String email, String password) {

        hideError();

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign Up");

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            Log.d(
                                    "SignUp",
                                    "Account created: " + user.getEmail()
                            );
                        }

                        Toast.makeText(
                                SignUpActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        /*
                         * createUserWithEmailAndPassword tự động đăng nhập
                         * tài khoản mới. Sign out để người dùng đăng nhập lại.
                         */
                        mAuth.signOut();

                        Intent intent = new Intent(
                                SignUpActivity.this,
                                SignInActivity.class
                        );

                        startActivity(intent);
                        finish();

                    } else {
                        handleSignUpError(task.getException());
                    }
                });
    }

    private void handleSignUpError(Exception exception) {

        if (exception == null) {
            showError("Sign up failed");
            return;
        }

        Log.e(
                "SignUp",
                "Account creation failed",
                exception
        );

        if (exception instanceof FirebaseAuthUserCollisionException) {

            showError("This email is already registered");
            etEmail.requestFocus();

        } else if (exception instanceof FirebaseAuthWeakPasswordException) {

            showError("Password is too weak");
            etPassword.requestFocus();

        } else if (exception
                instanceof FirebaseAuthInvalidCredentialsException) {

            showError("Invalid email address");
            etEmail.requestFocus();

        } else if (exception instanceof FirebaseNetworkException) {

            showError(
                    "Network error. Please check your internet connection"
            );

        } else {

            String message = exception.getMessage();

            if (message == null || message.isEmpty()) {
                message = "Sign up failed";
            }

            showError(message);
        }
    }

    private boolean validateForm(
            String email,
            String password,
            String confirmPassword
    ) {

        hideError();

        if (TextUtils.isEmpty(email)) {
            showError("Please enter your email");
            etEmail.requestFocus();
            return false;
        }

        if (!Pattern.compile(EMAIL_PATTERN)
                .matcher(email)
                .matches()) {

            showError("Invalid email format");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Please enter your password");
            etPassword.requestFocus();
            return false;
        }

        if (!Pattern.compile(PASSWORD_PATTERN)
                .matcher(password)
                .matches()) {

            showError(
                    "Password must contain at least 8 characters, " +
                            "including uppercase, lowercase, number and special character"
            );

            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }
}