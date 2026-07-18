package com.example.aistudymentorapplication;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etFullName, etEmail, etGrade, etSchool;
    private MaterialButton btnUpdate;

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            etEmail.setText(email);
            etEmail.setEnabled(false);

            loadUserProfile(currentUser.getUid());
        } else {
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etGrade = findViewById(R.id.etGrade);
        etSchool = findViewById(R.id.etSchool);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadUserProfile(String uid) {
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        String grade = documentSnapshot.getString("grade");
                        String school = documentSnapshot.getString("school");

                        etFullName.setText(name);
                        etGrade.setText(grade);
                        etSchool.setText(school);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileActivity.this, "Data loading error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateProfile() {
        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String grade = etGrade.getText() != null ? etGrade.getText().toString().trim() : "";
        String school = etSchool.getText() != null ? etSchool.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (grade.isEmpty()) {
            Toast.makeText(this, "Please enter your grade", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (school.isEmpty()) {
            Toast.makeText(this, "Please enter your school", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("fullName", name);
            userProfile.put("email", email);
            userProfile.put("grade", grade);
            userProfile.put("school", school);

            // Save in collection "Users"
            db.collection("Users").document(uid)
                    .set(userProfile)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Update Profile Successfully!", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Update Profile Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}