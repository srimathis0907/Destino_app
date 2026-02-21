package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.TopSpot;
import java.util.List;

public class TopSpotAdapter extends RecyclerView.Adapter<TopSpotAdapter.TopSpotViewHolder> {

    private final Context context;
    private final List<TopSpot> topSpots;
    private int lastPosition = -1;

    public TopSpotAdapter(Context context, List<TopSpot> topSpots) {
        this.context = context;
        this.topSpots = topSpots;
    }

    @NonNull
    @Override
    public TopSpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_spot, parent, false);
        return new TopSpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopSpotViewHolder holder, int position) {
        if (topSpots != null && !topSpots.isEmpty()) {
            TopSpot spot = topSpots.get(position);
            holder.spotName.setText(spot.getName());
            holder.spotDescription.setText(spot.getDescription());

            // Set animation
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            holder.itemView.startAnimation(animation);
        }
    }

    @Override
    public int getItemCount() {
        return topSpots != null ? topSpots.size() : 0;
    }

    static class TopSpotViewHolder extends RecyclerView.ViewHolder {
        TextView spotName, spotDescription;
        TopSpotViewHolder(@NonNull View itemView) {
            super(itemView);
            spotName = itemView.findViewById(R.id.top_spot_name);
            spotDescription = itemView.findViewById(R.id.top_spot_description);
        }
    }
}