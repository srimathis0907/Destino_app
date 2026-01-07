package com.simats.weekend.adapters;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.TopSpot;
import java.util.List;

public class AvailableSpotsAdapter extends RecyclerView.Adapter<AvailableSpotsAdapter.SpotViewHolder> {

    private final List<TopSpot> spotList;

    public AvailableSpotsAdapter(List<TopSpot> spotList) {
        this.spotList = spotList;
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_spot_draggable, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        TopSpot spot = spotList.get(position);
        holder.spotName.setText(spot.getName());
        holder.itemView.setOnLongClickListener(v -> {
            // Create a ClipData object to hold the spot's position
            ClipData.Item item = new ClipData.Item(String.valueOf(position));
            ClipData dragData = new ClipData("spot", new String[]{ "text/plain" }, item);
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(holder.itemView);
            v.startDragAndDrop(dragData, myShadow, null, 0);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }

    static class SpotViewHolder extends RecyclerView.ViewHolder {
        TextView spotName;
        SpotViewHolder(View itemView) {
            super(itemView);
            spotName = itemView.findViewById(R.id.spot_name);
        }
    }
}