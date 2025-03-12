package com.example.healthcareassitant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthcareassitant.MedicationAdapter;
import com.example.healthcareassitant.Medication;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> startActivity(new Intent(this, AddMedicationActivity.class)));
    }

    private void loadMedications() {
        FirebaseFirestore.getInstance().collection("medication_reminders")
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
}
