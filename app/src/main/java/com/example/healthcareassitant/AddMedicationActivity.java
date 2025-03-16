package com.example.healthcareassitant;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class AddMedicationActivity extends AppCompatActivity {
    private EditText etMedicationName, etDosage;
    private TextView tvSelectedTime;
    private Spinner spinnerFrequency;
    private LinearLayout daySelectionLayout;
    private String selectedTime = "Not Set";
    private FirebaseFirestore db;
    private List<String> selectedDays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        etMedicationName = findViewById(R.id.etMedicationName);
        etDosage = findViewById(R.id.etDosage);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        Button btnPickTime = findViewById(R.id.btnPickTime);
        Button btnSave = findViewById(R.id.btnSaveMedication);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        daySelectionLayout = findViewById(R.id.daySelectionLayout);

        // Time Picker Dialog
        btnPickTime.setOnClickListener(v -> showTimePicker());

        // Show/hide specific day selection
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) { // "Specific Days" selected
                    daySelectionLayout.setVisibility(View.VISIBLE);
                } else {
                    daySelectionLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("AddMedicationActivity", "📝 Saving Medication: " + name);
        Log.d("AddMedicationActivity", "🔄 Frequency: " + frequency);

        if (name.isEmpty() || dosage.isEmpty() || selectedTime.equals("Not Set")) {
            Log.e("AddMedicationActivity", "❌ ERROR: Missing required fields!");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (frequency.equals("Specific Days")) {
            selectedDays.clear();
            checkAndAddDay(R.id.cbMonday, "Monday");
            checkAndAddDay(R.id.cbTuesday, "Tuesday");
            checkAndAddDay(R.id.cbWednesday, "Wednesday");
            checkAndAddDay(R.id.cbThursday, "Thursday");
            checkAndAddDay(R.id.cbFriday, "Friday");
            checkAndAddDay(R.id.cbSaturday, "Saturday");
            checkAndAddDay(R.id.cbSunday, "Sunday");

            if (selectedDays.isEmpty()) {
                Log.e("AddMedicationActivity", "❌ ERROR: selectedDays is EMPTY!");
                Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AddMedicationActivity", "✅ Selected Days: " + selectedDays.toString());
        }

        // Prepare data for Firestore
        Map<String, Object> medication = new HashMap<>();
        medication.put("userId", userId);
        medication.put("name", name);
        medication.put("dosage", dosage);
        medication.put("time", selectedTime);
        medication.put("frequency", frequency);
        medication.put("days", frequency.equals("Specific Days") ? selectedDays : null);

        db.collection("medication_reminders")
                .add(medication)
                .addOnSuccessListener(documentReference -> {
                    Log.d("AddMedicationActivity", "✅ Medication Saved: " + documentReference.getId());

                    // Extract hour and minute from selectedTime
                    int hour = Integer.parseInt(selectedTime.split(":")[0]);
                    int minute = Integer.parseInt(selectedTime.split(":")[1]);

                    // ✅ Call scheduleReminder() to set up notifications
                    ReminderScheduler.scheduleReminder(
                            AddMedicationActivity.this,
                            documentReference.getId(),
                            name,
                            hour,
                            minute,
                            frequency,
                            selectedDays
                    );
                    Log.d("AddMedicationActivity", "✅ scheduleReminder() CALLED for: " + name);

                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Log.e("AddMedicationActivity", "❌ Firestore Save Error: " + e.getMessage()));
    }



    private void checkAndAddDay(int checkBoxId, String day) {
        CheckBox checkBox = findViewById(checkBoxId);
        if (checkBox.isChecked()) {
            selectedDays.add(day);
        }
    }
}
