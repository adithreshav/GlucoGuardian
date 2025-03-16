package com.example.healthcareassitant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthcareassitant.EmergencyAdapter;
import com.example.healthcareassitant.EmergencyContact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EmergencyAdapter adapter;
    private FirebaseFirestore db;
    private List<EmergencyContact> emergencyContacts;
    private static final String TAG = "EmergencyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        recyclerView = findViewById(R.id.recyclerViewEmergencyContacts);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddEmergency);
        db = FirebaseFirestore.getInstance();
        emergencyContacts = new ArrayList<>();
        adapter = new EmergencyAdapter(emergencyContacts, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(EmergencyActivity.this, AddEmergencyContactActivity.class);
            startActivity(intent);
        });

        loadEmergencyContacts();
    }

    private void loadEmergencyContacts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).collection("emergency_contacts")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore Error: " + e.getMessage());
                        return;
                    }

                    emergencyContacts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EmergencyContact contact = doc.toObject(EmergencyContact.class);
                        contact.setId(doc.getId());
                        emergencyContacts.add(contact);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
