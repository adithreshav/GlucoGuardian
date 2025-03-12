package com.example.healthcareassitant;

//package com.example.healthassistant.models;

public class Medication {
    private String id;  // Firestore ID
    private String name;
    private String dosage;
    private String time;

    public Medication() {}  // Empty constructor for Firestore

    public Medication(String id, String name, String dosage, String time) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
