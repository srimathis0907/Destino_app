package com.simats.weekend.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import com.simats.weekend.FutureTripDetailsActivity;
import com.simats.weekend.fragments.IActionModeController;
import com.simats.weekend.models.Trip;
import java.util.List;

public class FutureTripsAdapter extends BaseTripsAdapter {

    private final ActivityResultLauncher<Intent> tripDetailsLauncher;

    public FutureTripsAdapter(Context context, List<Trip> trips, ActivityResultLauncher<Intent> launcher, IActionModeController controller) {
        super(context, trips, controller);
        this.tripDetailsLauncher = launcher;
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // Call base class method first

        Trip trip = trips.get(position);

        // --- FIX: Logic for the "Today" and "Active" tags ---
        if ("active".equalsIgnoreCase(trip.getStatus())) {
            holder.activeTag.setVisibility(View.VISIBLE);
            holder.todayTag.setVisibility(View.GONE);
        } else if (trip.isToday() && "future".equalsIgnoreCase(trip.getStatus())) {
            holder.activeTag.setVisibility(View.GONE);
            holder.todayTag.setVisibility(View.VISIBLE);
        } else {
            // Hide both tags if neither condition is met
            holder.activeTag.setVisibility(View.GONE);
            holder.todayTag.setVisibility(View.GONE);
        }
        // --- END OF FIX ---

        // Navigation for the "View Details" button (remains the same)
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (!isSelectionMode) {
                Intent intent = new Intent(context, FutureTripDetailsActivity.class);
                intent.putExtra("TRIP_DETAILS", trip);
                tripDetailsLauncher.launch(intent);
            }
        });
    }
}