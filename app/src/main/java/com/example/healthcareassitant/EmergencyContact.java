package com.example.healthcareassitant;

public class EmergencyContact {
    private String id;
    private String name;
    private String phone;  // Ensure this field exists

    public EmergencyContact() {
        // Required empty constructor for Firestore
    }

    public EmergencyContact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;  // Ensure this method exists!
    }
}
