package com.example.healthcareassitant;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicationReminderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<Medication> medicationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_reminder);

        recyclerView = findViewById(R.id.recyclerViewMedications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadMedications();

        FloatingActionButton fabAdd = findViewById(R.id.fabAddEmergency);
        //fabAdd.setOnClickListener(view -> startActivity(new Intent(this, AddMedicationActivity.class)));


        //
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(MedicationReminderActivity.this, AddMedicationActivity.class);
            startActivityForResult(intent, 1); // Request code 1
        }); //

    }

    private void loadMedications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("medication_reminders")
                .whereEqualTo("userId", userId)  // Filter medications for the logged-in user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    medicationList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Medication medication = document.toObject(Medication.class);
                        medication.setId(document.getId());
                        medicationList.add(medication);
                    }
                    adapter = new MedicationAdapter(medicationList);
                    recyclerView.setAdapter(adapter);
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadMedications(); // Reload medications immediately after adding
        }
    }


}
