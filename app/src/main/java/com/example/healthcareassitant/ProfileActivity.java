package com.example.healthcareassitant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameField, ageField, diabetesTypeField;
    private Button saveButton, logoutButton;
    private TextView emailText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId; // Store user's UID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        nameField = findViewById(R.id.nameField);
        ageField = findViewById(R.id.ageField);
        diabetesTypeField = findViewById(R.id.diabetesTypeField);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        emailText = findViewById(R.id.emailText);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid(); // Get the logged-in user's UID
            emailText.setText(user.getEmail()); // Display email
            loadUserProfile(userId); // Load user data from Firestore
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Input validation on focus change
        ageField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validateAge();
        });

        diabetesTypeField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validateDiabetesType();
        });

        // Save profile changes
        saveButton.setOnClickListener(v -> {
            if (validateAge() & validateDiabetesType()) {
                saveUserProfile();
            }
        });

        // Logout and return to LoginActivity
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });


        // Bottom Navigation Handling
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }

    private void loadUserProfile(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                nameField.setText(documentSnapshot.getString("name"));
                ageField.setText(documentSnapshot.getString("age"));
                diabetesTypeField.setText(documentSnapshot.getString("diabetesType"));
            }
        }).addOnFailureListener(e ->
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private boolean validateAge() {
        String ageStr = ageField.getText().toString().trim();
        if (ageStr.isEmpty()) {
            ageField.setError("Age is required!");
            ageField.setText(""); // Clear wrong input
            return false;
        }
        try {
            int age = Integer.parseInt(ageStr);
            if (age > 150) {
                ageField.setError("Max age is 150!");
                ageField.setText(""); // Clear wrong input
                return false;
            }
        } catch (NumberFormatException e) {
            ageField.setError("Invalid age!");
            ageField.setText(""); // Clear wrong input
            return false;
        }
        return true;
    }

    private boolean validateDiabetesType() {
        String diabetesType = diabetesTypeField.getText().toString().trim();
        if (diabetesType.isEmpty()) {
            diabetesTypeField.setError("Diabetes type is required!");
            diabetesTypeField.setText(""); // Clear wrong input
            return false;
        }
        if (!diabetesType.equals("1") && !diabetesType.equals("2")) {
            diabetesTypeField.setError("Only '1' or '2' allowed!");
            diabetesTypeField.setText(""); // Clear wrong input
            return false;
        }
        return true;
    }

    private void saveUserProfile() {
        String name = nameField.getText().toString().trim();
        String age = ageField.getText().toString().trim();
        String diabetesType = diabetesTypeField.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || diabetesType.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("age", age);
        userProfile.put("diabetesType", diabetesType);

        db.collection("users").document(userId)
                .set(userProfile)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show());
    }


}
