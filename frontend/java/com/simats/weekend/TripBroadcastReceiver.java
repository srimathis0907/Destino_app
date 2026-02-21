package com.simats.weekend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TripBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TRIP_REMINDERS";
    private static final String CHANNEL_NAME = "Trip Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TripBroadcastReceiver", "Alarm received! Preparing notification.");

        int tripId = intent.getIntExtra("TRIP_ID", -1);
        String placeName = intent.getStringExtra("PLACE_NAME");
        if (placeName == null || placeName.isEmpty()) {
            placeName = "your amazing trip";
        }

        createNotificationChannel(context);

        Intent detailsIntent = new Intent(context, FutureTripDetailsActivity.class);
        detailsIntent.putExtra("TRIP_ID", tripId);
        // Ensure the activity stack is handled correctly
        detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, tripId, detailsIntent, flags);

        // --- NEW: Use BigTextStyle for a more descriptive notification ---
        String notificationText = "It's time to embark on your adventure to " + placeName + "! Tap here to view your itinerary and get started.";
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle().bigText(notificationText);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_flight)
                .setContentTitle("🚀 Your Trip Starts Today!")
                .setContentText("Your adventure to " + placeName + " begins!") // Shorter text for collapsed view
                .setStyle(bigTextStyle) // Apply the expanded text style
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // --- NEW: More descriptive action button ---
        builder.addAction(R.drawable.ic_start_trip, "Let's Begin!", pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("TripBroadcastReceiver", "Missing POST_NOTIFICATIONS permission.");
            return;
        }

        notificationManager.notify(tripId, builder.build());
        Log.d("TripBroadcastReceiver", "Notification for " + placeName + " has been sent.");
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for upcoming trip reminders.");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}