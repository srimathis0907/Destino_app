package com.simats.weekend.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.simats.weekend.CompletedTripDetailsActivity;
import com.simats.weekend.fragments.IActionModeController;
import com.simats.weekend.models.Trip;
import java.util.List;

public class CompletedTripsAdapter extends BaseTripsAdapter {

    public CompletedTripsAdapter(Context context, List<Trip> trips, IActionModeController controller) {
        super(context, trips, controller);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // Call base class method first

        Trip trip = trips.get(position);

        // OVERRIDE: Implement specific navigation for the "View Details" button
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (!isSelectionMode) { // Only navigate if not in selection mode
                Intent intent = new Intent(context, CompletedTripDetailsActivity.class);
                intent.putExtra("FINISHED_TRIP", trip);
                context.startActivity(intent);
            }
        });
    }
}