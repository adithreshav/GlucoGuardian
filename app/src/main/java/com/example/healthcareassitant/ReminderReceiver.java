package com.example.healthcareassitant;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationName = intent.getStringExtra("medicationName");

        Log.d("ReminderReceiver", "🔔 Notification Triggered for: " + medicationName);

        // Ensure notification channel is created
        NotificationHelper.createNotificationChannel(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e("ReminderReceiver", "❌ NotificationManager is null. Cannot send notification.");
            return;
        }

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Default icon for testing
                .setContentTitle("Medication Reminder")
                .setContentText("Time to take " + medicationName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
        Log.d("ReminderReceiver", "✅ Notification should now appear!");
    }
}
