package com.example.healthcareassitant;

import java.util.List;
public class Medication {
    private String id;  // Firestore ID
    private String userId;  // User who owns this medication
    private String name;
    private String dosage;
    private String time;
    private String frequency;
    private List<String> days; // Optional: Only for "Specific Days"

    public Medication() {}  // Empty constructor for Firestore

    public Medication(String id, String userId, String name, String dosage, String time, String frequency, List<String> days) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.frequency = frequency;
        this.days = days;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }
}
