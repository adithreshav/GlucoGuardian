package com.example.healthcareassitant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

public class BloodSugarLoggingActivity extends AppCompatActivity {

    private EditText bloodSugarInput;
    private Button logBloodSugarButton;
    private RecyclerView bloodSugarRecyclerView;
    private BloodSugarAdapter adapter;
    private List<BloodSugarLog> bloodSugarLogs;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_sugar_logging);

        bloodSugarInput = findViewById(R.id.bloodSugarInput);
        logBloodSugarButton = findViewById(R.id.logBloodSugarButton);
        bloodSugarRecyclerView = findViewById(R.id.bloodSugarRecyclerView);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        bloodSugarLogs = new ArrayList<>();
        adapter = new BloodSugarAdapter(bloodSugarLogs, db, userId);
        bloodSugarRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bloodSugarRecyclerView.setAdapter(adapter);

        logBloodSugarButton.setOnClickListener(v -> logBloodSugar());

        loadBloodSugarLogs();
    }

    private void logBloodSugar() {
        String valueStr = bloodSugarInput.getText().toString().trim();
        if (TextUtils.isEmpty(valueStr)) {
            bloodSugarInput.setError("Enter a valid number");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            bloodSugarInput.setError("Invalid number");
            return;
        }

        if (value < 50 || value > 300) {  // Example range for blood sugar
            bloodSugarInput.setError("Value must be between 50 and 300");
            return;
        }

        Map<String, Object> log = new HashMap<>();
        log.put("value", value);
        log.put("timestamp", Timestamp.now());

        db.collection("users").document(userId)
                .collection("blood_sugar_logs")
                .add(log)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Logged Successfully", Toast.LENGTH_SHORT).show();
                    bloodSugarInput.setText("");
                    loadBloodSugarLogs();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to log", Toast.LENGTH_SHORT).show());
    }

    private void loadBloodSugarLogs() {
        db.collection("users").document(userId)
                .collection("blood_sugar_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bloodSugarLogs.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        BloodSugarLog log = doc.toObject(BloodSugarLog.class);
                        log.setId(doc.getId());
                        bloodSugarLogs.add(log);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show());
    }
}
