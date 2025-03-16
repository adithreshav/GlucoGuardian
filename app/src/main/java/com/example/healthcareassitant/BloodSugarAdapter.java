package com.example.healthcareassitant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BloodSugarAdapter extends RecyclerView.Adapter<BloodSugarAdapter.BloodSugarViewHolder> {

    private List<BloodSugarLog> bloodSugarLogs;
    private FirebaseFirestore db;
    private String userId;

    public BloodSugarAdapter(List<BloodSugarLog> bloodSugarLogs, FirebaseFirestore db, String userId) {
        this.bloodSugarLogs = bloodSugarLogs;
        this.db = db;
        this.userId = userId;
    }

    @NonNull
    @Override
    public BloodSugarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_sugar, parent, false);
        return new BloodSugarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodSugarViewHolder holder, int position) {
        BloodSugarLog log = bloodSugarLogs.get(position);

        holder.valueTextView.setText(log.getValue() + " mg/dL");

        Timestamp timestamp = log.getTimestamp();
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            holder.dateTextView.setText(sdf.format(timestamp.toDate()));
        } else {
            holder.dateTextView.setText("Unknown Date");
        }

        // Fix: Pass holder as the third argument
        holder.deleteButton.setOnClickListener(v -> deleteLog(log, position, holder));
    }


    @Override
    public int getItemCount() {
        return bloodSugarLogs.size();
    }

    public static class BloodSugarViewHolder extends RecyclerView.ViewHolder {
        TextView valueTextView, dateTextView;
        Button deleteButton;

        public BloodSugarViewHolder(@NonNull View itemView) {
            super(itemView);
            valueTextView = itemView.findViewById(R.id.bloodSugarValue);
            dateTextView = itemView.findViewById(R.id.bloodSugarDate);
            deleteButton = itemView.findViewById(R.id.deleteBloodSugarButton);
        }
    }

    private void deleteLog(BloodSugarLog log, int position, BloodSugarViewHolder holder) {
        if (log.getId() == null || log.getId().isEmpty()) {
            Toast.makeText(holder.itemView.getContext(), "Invalid log ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(userId) // Reference the user first
                .collection("blood_sugar_logs") // Access the subcollection
                .document(log.getId()) // Delete specific log
                .delete()
                .addOnSuccessListener(aVoid -> {
                    bloodSugarLogs.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, bloodSugarLogs.size());
                    Toast.makeText(holder.itemView.getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to delete entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}
