package com.example.healthcareassitant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {
    private List<EmergencyContact> emergencyContacts;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EmergencyAdapter(List<EmergencyContact> emergencyContacts, Context context) {
        this.emergencyContacts = emergencyContacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = emergencyContacts.get(position);

        // ✅ Ensure both name and phone number are set
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());  // This line ensures the number is displayed!

        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhone()));  // Ensure correct number is used
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("users").document(userId).collection("emergency_contacts")
                    .document(contact.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        emergencyContacts.remove(position);
                        notifyItemRemoved(position);
                    });
        });
    }


    @Override
    public int getItemCount() {
        return emergencyContacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnCall, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
