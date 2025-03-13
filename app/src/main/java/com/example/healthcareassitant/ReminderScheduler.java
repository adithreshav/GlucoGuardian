package com.example.healthcareassitant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.util.Calendar;
import java.util.List;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, String medicationId, String medicationName, int hour, int minute, String frequency, List<String> days) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medicationName", medicationName);

        if (alarmManager == null) {
            Log.e("ReminderScheduler", "❌ AlarmManager is NULL. Cannot schedule reminder.");
            return;
        }

        if (frequency.equals("Every Day")) {
            // ✅ Schedule for Every Day
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, medicationId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to the next day if the time has passed
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("ReminderScheduler", "✅ Daily Reminder Scheduled for: " + medicationName + " at " + hour + ":" + minute);
        } else if (frequency.equals("Specific Days")) {
            // ✅ Schedule for Specific Days
            for (String day : days) {
                int dayOfWeek = getDayOfWeek(day);
                Calendar calendar = Calendar.getInstance();
                int today = calendar.get(Calendar.DAY_OF_WEEK);

                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // If today is the selected day but time has already passed, move to next week
                if (today == dayOfWeek && calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                } else if (today != dayOfWeek && calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7 - (today - dayOfWeek));
                }

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (medicationId + day).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d("ReminderScheduler", "✅ Scheduled for " + day + " at " + hour + ":" + minute);
            }
        }
    }

    public static void cancelReminder(Context context, String medicationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, medicationId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("ReminderScheduler", "🚫 Alarm CANCELED for medication: " + medicationId);
        }
    }

    private static int getDayOfWeek(String day) {
        switch (day) {
            case "Monday": return Calendar.MONDAY;
            case "Tuesday": return Calendar.TUESDAY;
            case "Wednesday": return Calendar.WEDNESDAY;
            case "Thursday": return Calendar.THURSDAY;
            case "Friday": return Calendar.FRIDAY;
            case "Saturday": return Calendar.SATURDAY;
            case "Sunday": return Calendar.SUNDAY;
            default: return -1;
        }
    }
}
