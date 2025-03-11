package com.example.healthcareassitant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Button Click Listeners
        findViewById(R.id.btnMedicationReminder).setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, MedicationReminderActivity.class)));

        findViewById(R.id.btnEmergency).setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, EmergencyActivity.class)));

        findViewById(R.id.btnHealthRecommendation).setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, HealthRecommendationActivity.class)));

        findViewById(R.id.btnGlucoseGuardian).setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, GlucoseGuardianActivity.class)));

        // Bottom Navigation Handling
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
}
