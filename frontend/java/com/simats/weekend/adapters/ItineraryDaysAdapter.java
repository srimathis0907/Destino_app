package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.ItineraryDay;
import com.simats.weekend.models.ItinerarySpot;
import com.simats.weekend.models.NearbyPlace;
import com.simats.weekend.models.TopSpot;
import java.util.List;

public class ItineraryDaysAdapter extends RecyclerView.Adapter<ItineraryDaysAdapter.DayViewHolder> {
    // ... (Interface and constructor remain unchanged) ...
    private final List<ItineraryDay> dayList;
    private final List<TopSpot> availableSpots;
    private final OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onFindNearbyClicked(ItinerarySpot spot);
        void onItineraryChanged();
        void onSpotRemoved(ItineraryDay day, ItinerarySpot spot);
    }

    public ItineraryDaysAdapter(List<ItineraryDay> dayList, List<TopSpot> availableSpots, OnItemInteractionListener listener) {
        this.dayList = dayList;
        this.availableSpots = availableSpots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_itinerary_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        ItineraryDay day = dayList.get(position);
        holder.dayLabel.setText(day.getDateLabel());

        holder.spotsContainer.removeAllViews();
        holder.dropHint.setVisibility(day.getPlannedSpots().isEmpty() ? View.VISIBLE : View.GONE);

        for (ItinerarySpot itinerarySpot : day.getPlannedSpots()) {
            View spotView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_itinerary_spot, holder.spotsContainer, false);

            TextView spotName = spotView.findViewById(R.id.itinerary_spot_name);
            ImageButton findNearbyButton = spotView.findViewById(R.id.btn_find_nearby_spot);
            ImageButton removeButton = spotView.findViewById(R.id.btn_remove_spot);
            LinearLayout nearbyContainer = spotView.findViewById(R.id.nearby_places_container); // Find the new container

            spotName.setText(itinerarySpot.getTopSpot().getName());
            findNearbyButton.setOnClickListener(v -> listener.onFindNearbyClicked(itinerarySpot));
            removeButton.setOnClickListener(v -> listener.onSpotRemoved(day, itinerarySpot));

            // START: LOGIC TO DISPLAY SELECTED NEARBY PLACES (SUB-ITEMS)
            nearbyContainer.removeAllViews();
            if (itinerarySpot.getSelectedNearbyPlaces() != null && !itinerarySpot.getSelectedNearbyPlaces().isEmpty()) {
                for (NearbyPlace selectedPlace : itinerarySpot.getSelectedNearbyPlaces()) {
                    TextView selectedView = new TextView(holder.itemView.getContext());
                    // Style the sub-item text
                    selectedView.setText("└ " + selectedPlace.getName());
                    selectedView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                    selectedView.setTextSize(12f);
                    selectedView.setAlpha(0.8f);
                    nearbyContainer.addView(selectedView);
                }
            }
            // END: LOGIC TO DISPLAY SELECTED NEARBY PLACES (SUB-ITEMS)

            holder.spotsContainer.addView(spotView);
        }
        holder.itemView.setOnDragListener(new SpotDragListener(day, this, availableSpots, listener));
    }

    // ... (getItemCount, getDayPosition, and DayViewHolder remain unchanged) ...
    @Override
    public int getItemCount() {
        return dayList.size();
    }

    public int getDayPosition(ItineraryDay day) {
        return dayList.indexOf(day);
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayLabel, dropHint;
        LinearLayout spotsContainer;
        View dropTarget;

        DayViewHolder(View itemView) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.day_label);
            spotsContainer = itemView.findViewById(R.id.spots_container_for_day);
            dropTarget = itemView.findViewById(R.id.drop_target);
            dropHint = itemView.findViewById(R.id.drop_hint);
        }
    }
}