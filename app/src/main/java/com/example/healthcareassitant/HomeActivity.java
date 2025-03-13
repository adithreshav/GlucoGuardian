package com.example.healthcareassitant;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

        //
        // Create Notification Channel
        NotificationHelper.createNotificationChannel(this);
        //

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


        // Request alarm permission for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("MainActivity", "⚠️ Exact alarm permission NOT granted. Requesting...");
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            } else {
                Log.d("MainActivity", "✅ Exact alarm permission is already granted.");
            }
        }
    }
    // Prevent swipe-back navigation to LoginActivity
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Do nothing to disable back navigation (swipe-back behavior)
    }
}
