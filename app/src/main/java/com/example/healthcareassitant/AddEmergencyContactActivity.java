package com.example.healthcareassitant;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddEmergencyContactActivity extends AppCompatActivity {
    private EditText etContactName, etContactPhone;
    private Button btnSaveContact;
    private FirebaseFirestore db;
    private static final String TAG = "AddEmergencyContact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contact);

        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        db = FirebaseFirestore.getInstance();

        btnSaveContact.setOnClickListener(v -> saveEmergencyContact());
    }

    private void saveEmergencyContact() {
        String name = etContactName.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter both name and phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> contact = new HashMap<>();
        contact.put("name", name);
        contact.put("phone", phone);

        db.collection("users").document(userId).collection("emergency_contacts")
                .add(contact)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Emergency contact added: " + documentReference.getId());
                    Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and return to EmergencyActivity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error adding contact: " + e.getMessage());
                    Toast.makeText(this, "Error saving contact", Toast.LENGTH_SHORT).show();
                });
    }
}
