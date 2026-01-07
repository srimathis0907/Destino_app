package com.simats.weekend.adapters;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.View;
import com.simats.weekend.R;
import com.simats.weekend.models.ItineraryDay;
import com.simats.weekend.models.TopSpot;
import java.util.List;

public class SpotDragListener implements View.OnDragListener {

    private final ItineraryDay day;
    private final ItineraryDaysAdapter adapter;
    private final List<TopSpot> availableSpots;
    // UPDATED: Add a variable for the listener
    private final ItineraryDaysAdapter.OnItemInteractionListener listener;

    // UPDATED: The constructor now accepts the listener from the activity
    public SpotDragListener(ItineraryDay day, ItineraryDaysAdapter adapter, List<TopSpot> availableSpots, ItineraryDaysAdapter.OnItemInteractionListener listener) {
        this.day = day;
        this.adapter = adapter;
        this.availableSpots = availableSpots;
        this.listener = listener;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        final int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                return event.getClipDescription().hasMimeType("text/plain");

            case DragEvent.ACTION_DRAG_ENTERED:
                v.findViewById(R.id.drop_target).setBackgroundResource(R.drawable.drag_hover_background);
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                v.findViewById(R.id.drop_target).setBackgroundResource(R.drawable.drag_normal_background);
                return true;

            case DragEvent.ACTION_DROP:
                ClipData.Item item = event.getClipData().getItemAt(0);
                int position = Integer.parseInt(item.getText().toString());
                TopSpot spotToAdd = availableSpots.get(position);

                day.addSpot(spotToAdd);
                adapter.notifyItemChanged(adapter.getDayPosition(day));

                // UPDATED: Notify the activity that the itinerary has changed
                if (listener != null) {
                    listener.onItineraryChanged();
                }

                v.findViewById(R.id.drop_target).setBackgroundResource(R.drawable.drag_normal_background);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                v.findViewById(R.id.drop_target).setBackgroundResource(R.drawable.drag_normal_background);
                return true;

            default:
                break;
        }
        return false;
    }
}