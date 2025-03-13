package com.example.healthcareassitant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private List<Medication> medicationList;

    public MedicationAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.tvName.setText(medication.getName());
        holder.tvDosage.setText(medication.getDosage());
        holder.tvTime.setText(medication.getTime());

        if (medication.getFrequency().equals("Specific Days")) {
            holder.tvFrequency.setText("Days: " + medication.getDays().toString());
        } else {
            holder.tvFrequency.setText("Frequency: " + medication.getFrequency());
        }

//        // Handle Delete
//        holder.btnDelete.setOnClickListener(view -> {
//            FirebaseFirestore.getInstance().collection("medication_reminders")
//                    .document(medication.getId()).delete()
//                    .addOnSuccessListener(aVoid -> {
//                        medicationList.remove(position);
//                        notifyItemRemoved(position);
//                    });
//        });

        holder.btnDelete.setOnClickListener(view -> {
            FirebaseFirestore.getInstance().collection("medication_reminders")
                    .document(medication.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        ReminderScheduler.cancelReminder(view.getContext(), medication.getId());
                        medicationList.remove(position);
                        notifyItemRemoved(position);
                    });
        });

    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime,tvFrequency;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicationName);
            tvDosage = itemView.findViewById(R.id.tvMedicationDosage);
            tvTime = itemView.findViewById(R.id.tvMedicationTime);
            tvFrequency = itemView.findViewById(R.id.tvMedicationFrequency);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
