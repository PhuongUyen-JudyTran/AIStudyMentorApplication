package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
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
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            //Verify
            if (email.isEmpty()) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email format");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();
                return;
            }

            btnSignIn.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            btnSignIn.setEnabled(true);
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(SignInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    Log.d("SignIn", "Signed in user: " + user.getEmail());

                    // Move to Main Page
                    Intent intent = new Intent(
                            SignInActivity.this,
                            SignUpActivity.class
                    );

                    startActivity(intent);
                    finish();
                }
            } else {
                handleSignInError(task.getException());
            }
            });
        }

            private void handleSignInError(Exception exception) {

                if (exception == null) {
                    Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("SignIn", "Sign in failed", exception);

                if (exception instanceof FirebaseAuthInvalidUserException) {

                    FirebaseAuthException authException =
                            (FirebaseAuthException) exception;

                    String errorCode = authException.getErrorCode();

                    if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                        etEmail.setError("Account does not exist");
                        etEmail.requestFocus();

                    }
                        else {

                            Toast.makeText(
                                    SignInActivity.this,
                                    "Invalid user account",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                // Email and Password Error
                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    FirebaseAuthException authException = (FirebaseAuthException) exception;
                    String errorCode = authException.getErrorCode();

                    if ("ERROR_INVALID_EMAIL".equals(errorCode)) {

                        etEmail.setError("Invalid email format");
                        etEmail.requestFocus();

                    } else if ("ERROR_WRONG_PASSWORD".equals(errorCode)) {

                        etPassword.setError("Incorrect password");
                        etPassword.requestFocus();

                    } else {

                        Toast.makeText(
                                SignInActivity.this,
                                "Email or password is incorrect",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                } else {
                    Toast.makeText(
                            SignInActivity.this,
                            "Error: " + exception.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        }
}
