package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.R;
import com.simats.weekend.models.ActivityItem;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {

    private Context context;
    private List<ActivityItem> itemList;

    public RecentActivityAdapter(Context context, List<ActivityItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = itemList.get(position);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);

        holder.subtitle.setVisibility(View.VISIBLE);
        holder.rating.setVisibility(View.GONE);

        switch (item.type) {
            case FAVORITE:
                holder.icon.setImageResource(R.drawable.ic_favorite_filled);
                holder.icon.setBackgroundResource(R.drawable.circle_background_light_teal);
                break;
            case REVIEW:
                holder.icon.setImageResource(R.drawable.ic_star_filled);
                holder.icon.setBackgroundResource(R.drawable.circle_background_light_yellow);
                holder.rating.setVisibility(View.VISIBLE);
                holder.rating.setText(item.rating);
                break;
            // Add other cases here for NEW_TRIP, CANCELLED, etc.
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    // --- THIS IS THE MISSING PART ---
    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, subtitle, rating;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_activity_icon);
            title = itemView.findViewById(R.id.tv_activity_title);
            subtitle = itemView.findViewById(R.id.tv_activity_subtitle);
            rating = itemView.findViewById(R.id.tv_activity_rating);
        }
    }
}