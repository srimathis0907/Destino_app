package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.ItineraryDayDetails;
import com.simats.weekend.models.ItinerarySpotDetails;
import java.util.List;

public class ItineraryDetailsAdapter extends RecyclerView.Adapter<ItineraryDetailsAdapter.DayViewHolder> {

    private final Context context;
    private final List<ItineraryDayDetails> dayList;
    private final LayoutInflater inflater;

    public ItineraryDetailsAdapter(Context context, List<ItineraryDayDetails> dayList) {
        this.context = context;
        this.dayList = dayList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_itinerary_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        ItineraryDayDetails day = dayList.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayLabel;
        private final LinearLayout spotsContainer;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.text_view_day_label);
            spotsContainer = itemView.findViewById(R.id.spots_container);
        }

        void bind(ItineraryDayDetails day) {
            dayLabel.setText(day.getDayLabel());

            // Clear any old views before adding new ones
            spotsContainer.removeAllViews();

            if (day.getSpots().isEmpty()) {
                // If there are no spots, show a "Rest Day" message
                TextView restDayView = createSpotTextView("Rest Day", false);
                spotsContainer.addView(restDayView);
            } else {
                // Add a view for each spot and its add-ons
                for (ItinerarySpotDetails spot : day.getSpots()) {
                    TextView spotView = createSpotTextView("📍 " + spot.getSpotName(), false);
                    spotsContainer.addView(spotView);

                    for (String addon : spot.getAddons()) {
                        TextView addonView = createSpotTextView("•  " + addon, true);
                        spotsContainer.addView(addonView);
                    }
                }
            }
        }

        // Helper method to create TextViews for spots and add-ons dynamically
        private TextView createSpotTextView(String text, boolean isAddon) {
            TextView textView = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

// INSIDE createSpotTextView method...

// INSIDE createSpotTextView method...

            if (isAddon) {
                params.setMargins(48, 8, 0, 8); // Indent add-ons
                // Use the universal AppCompat style for smaller, secondary text
                textView.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
            } else {
                params.setMargins(0, 12, 0, 4);
                // Use the universal AppCompat style for larger, primary text
                textView.setTextAppearance(android.R.style.TextAppearance_Material_Body2);
            }

            textView.setLayoutParams(params);
            textView.setText(text);
            return textView;
        }
    }
}