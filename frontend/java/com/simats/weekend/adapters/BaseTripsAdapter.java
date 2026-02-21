package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.simats.weekend.R;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.fragments.IActionModeController;
import com.simats.weekend.models.Trip;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTripsAdapter extends RecyclerView.Adapter<BaseTripsAdapter.TripViewHolder> {

    protected Context context;
    protected List<Trip> trips;
    protected IActionModeController actionModeController;
    protected boolean isSelectionMode = false;
    protected List<Integer> selectedTripIds = new ArrayList<>();

    public BaseTripsAdapter(Context context, List<Trip> trips, IActionModeController actionModeController) {
        this.context = context;
        this.trips = trips;
        this.actionModeController = actionModeController;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.title.setText(trip.getPlaceName());
        holder.date.setText(trip.getDate());

        if (trip.getPlaceImage() != null && !trip.getPlaceImage().isEmpty()) {
            String imageUrl = RetrofitClient.BASE_URL + trip.getPlaceImage();
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bali)
                    .error(R.drawable.bali)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.bali);
        }

        holder.checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedTripIds.contains(trip.getId()));

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(trip.getId());
                holder.checkBox.setChecked(selectedTripIds.contains(trip.getId()));
            } else {
                holder.viewDetailsButton.performClick();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                if (actionModeController != null) actionModeController.startActionMode();
                toggleSelection(trip.getId());
                notifyItemChanged(position);
            }
            return true;
        });

        holder.checkBox.setOnClickListener(v -> toggleSelection(trip.getId()));
        holder.viewDetailsButton.setOnClickListener(v -> {});
    }

    @Override
    public int getItemCount() {
        return trips == null ? 0 : trips.size();
    }

    public void setTrips(List<Trip> newTrips) {
        // Create a new list to avoid modifying the list held by the fragment directly
        this.trips = new ArrayList<>(newTrips);
        notifyDataSetChanged();
    }

    public void startSelectionMode() {
        isSelectionMode = true;
        notifyDataSetChanged();
    }

    public void finishSelectionMode() {
        isSelectionMode = false;
        selectedTripIds.clear();
        if (actionModeController != null) {
            actionModeController.finishActionMode();
        }
        notifyDataSetChanged();
    }

    protected void toggleSelection(int tripId) {
        if (selectedTripIds.contains(tripId)) {
            selectedTripIds.remove(Integer.valueOf(tripId));
        } else {
            selectedTripIds.add(tripId);
        }
        if (actionModeController != null) actionModeController.updateActionModeTitle(selectedTripIds.size());
        if (selectedTripIds.isEmpty() && isSelectionMode) {
            finishSelectionMode();
        }
    }

    public List<Integer> getSelectedTripIds() {
        return new ArrayList<>(selectedTripIds);
    }

    // --- ViewHolder Updated ---
    static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, date;
        CheckBox checkBox;
        Button viewDetailsButton;
        TextView todayTag;
        TextView activeTag; // <-- ADDED THIS

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.trip_image);
            title = itemView.findViewById(R.id.trip_title);
            date = itemView.findViewById(R.id.trip_date);
            checkBox = itemView.findViewById(R.id.trip_checkbox);
            viewDetailsButton = itemView.findViewById(R.id.view_details_button);
            todayTag = itemView.findViewById(R.id.trip_today_tag);
            activeTag = itemView.findViewById(R.id.trip_active_tag); // <-- ADDED THIS
        }
    }
}