package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Email regex provided by user
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PASSWORD_PATTERN =   "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])" +  "(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Set click listeners
        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            if (validateForm(email, password, confirmPassword)) {
                registerUser(email, password);
            }
        });

        btnSignIn.setOnClickListener(v -> {
            // Navigate back to Login
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser(String email, String password) {

        // Tránh người dùng bấm nút nhiều lần
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            btnSignUp.setEnabled(true);
            btnSignUp.setText("Sign Up");
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.d("SignUp", "Account created: " + user.getEmail());
                }
                Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            } else {
                handleSignUpError(task.getException());
            }
        });
        }

    private void handleSignUpError(Exception exception) {
        Log.e("SignUp", "Account creation failed", exception);

        if (exception instanceof FirebaseAuthUserCollisionException) {
            etEmail.setError("This email is already registered");
            etEmail.requestFocus();

        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            etPassword.setError("Password is too weak");
            etPassword.requestFocus();

        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            etEmail.setError("Invalid email address");
            etEmail.requestFocus();

        } else if (exception instanceof FirebaseNetworkException) {
            Toast.makeText(SignUpActivity.this, "Network error. Please check your connection", Toast.LENGTH_SHORT).show();

        } else {
            String errorMessage = "Sign up failed";
            if (exception != null && exception.getMessage() != null) {
                errorMessage = exception.getMessage();
            }
            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    private boolean validateForm(
            String email,
            String password,
            String confirmPassword
    ) {

        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(
                    "Please confirm your password"
            );

            etConfirmPassword.requestFocus();
            return false;
        }

        if (!Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            etEmail.requestFocus();
            return false;
        }

        if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches()) {
            etPassword.setError("Min 8 characters, including uppercase, " + "lowercase, number and special character");
            etPassword.requestFocus();
            return false;
        }

        // Check password matches with confirm password
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }
}