package com.simats.weekend;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.simats.weekend.models.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static void scheduleNotification(Context context, Trip trip) {
        // FIX: Use getStartDate()
        if (trip == null || trip.getStartDate() == null || trip.getStartDate().isEmpty()) {
            return; // Cannot schedule without a start date
        }

        // 1. Parse the start date string into a Date object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date startDate;
        try {
            // FIX: Use getStartDate()
            startDate = sdf.parse(trip.getStartDate());
        } catch (ParseException e) {
            // FIX: Use getStartDate()
            Log.e(TAG, "Failed to parse trip start date: " + trip.getStartDate(), e);
            return;
        }

        // 2. Set up a Calendar instance for midnight on the start date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Midnight
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Don't schedule notifications for trips that have already started
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            // FIX: Use getPlaceName()
            Log.d(TAG, "Trip start date is in the past. No notification scheduled for: " + trip.getPlaceName());
            return;
        }

        // 3. Create the Intent that will be broadcasted when the alarm goes off
        Intent intent = new Intent(context, TripBroadcastReceiver.class);
        intent.setAction("com.example.weekend.TRIP_ALARM"); // Use the same action as in the manifest
        intent.putExtra("TRIP_ID", trip.getId());
        // FIX: Use getPlaceName()
        intent.putExtra("PLACE_NAME", trip.getPlaceName());

        // Use the trip ID as the request code to make each PendingIntent unique
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, trip.getId(), intent, flags);

        // 4. Get the system's AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 5. Set the exact alarm
        if (alarmManager != null) {
            // Check if we have permission to schedule exact alarms (for Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    // FIX: Use getPlaceName()
                    Log.d(TAG, "Exact alarm scheduled for " + trip.getPlaceName() + " at " + calendar.getTime());
                } else {
                    // Fallback or request permission. For now, we'll just log it.
                    Log.w(TAG, "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM permission.");
                    // You might want to schedule an inexact alarm as a fallback
                    // alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                // For older versions, set the exact alarm directly
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                // FIX: Use getPlaceName()
                Log.d(TAG, "Alarm scheduled for " + trip.getPlaceName() + " at " + calendar.getTime());
            }
        }
    }
}