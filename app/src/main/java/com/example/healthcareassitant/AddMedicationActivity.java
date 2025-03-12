package com.example.healthcareassitant;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthcareassitant.Medication;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddMedicationActivity extends AppCompatActivity {
    private EditText etMedicationName, etDosage;
    private TextView tvSelectedTime;
    private String selectedTime = "Not Set";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get UI elements
        etMedicationName = findViewById(R.id.etMedicationName);
        etDosage = findViewById(R.id.etDosage);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        Button btnPickTime = findViewById(R.id.btnPickTime);
        Button btnSave = findViewById(R.id.btnSaveMedication);

        // Time Picker Dialog
        btnPickTime.setOnClickListener(v -> showTimePicker());

        // Save Medication
        btnSave.setOnClickListener(v -> saveMedication());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfDay) -> {
            selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfDay);
            tvSelectedTime.setText("Time: " + selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void saveMedication() {
        String name = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty() || selectedTime.equals("Not Set")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create medication data
        Map<String, Object> medication = new HashMap<>();
        medication.put("name", name);
        medication.put("dosage", dosage);
        medication.put("time", selectedTime);

        // Save to Firestore
        db.collection("medication_reminders")
                .add(medication)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddMedicationActivity.this, "Medication Saved", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and return
                })
                .addOnFailureListener(e -> Toast.makeText(AddMedicationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
