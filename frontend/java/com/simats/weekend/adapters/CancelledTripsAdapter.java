package com.simats.weekend.adapters;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import com.simats.weekend.fragments.IActionModeController;
import com.simats.weekend.models.Trip;
import java.util.List;

public class CancelledTripsAdapter extends BaseTripsAdapter {

    public CancelledTripsAdapter(Context context, List<Trip> trips, IActionModeController controller) {
        super(context, trips, controller);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // Call base class method first

        // OVERRIDE: Cancelled trips don't have a details view, so hide the button.
        holder.viewDetailsButton.setVisibility(View.GONE);

        // We also disable the single-click listener from the base class for clarity
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(trips.get(position).getId());
                holder.checkBox.setChecked(selectedTripIds.contains(trips.get(position).getId()));
            }
        });
    }
}