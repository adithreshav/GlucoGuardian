package com.example.healthcareassitant;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BloodSugarLog {
    private String id;
    private int value;
    private Timestamp timestamp;

    // Required empty constructor for Firestore
    public BloodSugarLog() {}

    public BloodSugarLog(int value, Timestamp timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        if (timestamp == null) return "Unknown date";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
