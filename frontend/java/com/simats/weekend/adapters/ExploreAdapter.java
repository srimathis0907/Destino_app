package com.simats.weekend.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.UviewdetailsActivity;
import com.simats.weekend.models.ExploreDestination;
import java.util.List;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {

    private Context context;
    private List<ExploreDestination> destinationList;

    public ExploreAdapter(Context context, List<ExploreDestination> destinationList) {
        this.context = context;
        this.destinationList = destinationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_explore_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExploreDestination destination = destinationList.get(position);
        holder.ivImage.setImageResource(destination.getImageResId());
        holder.tvTitle.setText(destination.getTitle());
        holder.tvLocation.setText(destination.getLocation());
        holder.tvTemperature.setText(destination.getTemperature());
        holder.tvDuration.setText(destination.getDuration());
        holder.tvBestTime.setText(destination.getBestTime());

        // --- THIS IS THE UPDATED PART ---
        holder.btnViewDetails.setOnClickListener(v -> {
            // Change the destination from DestinationDetailActivity to UviewdetailsActivity
            Intent intent = new Intent(context, UviewdetailsActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvLocation, tvTemperature, tvDuration, tvBestTime;
        Button btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_destination_image);
            tvTitle = itemView.findViewById(R.id.tv_destination_title);
            tvLocation = itemView.findViewById(R.id.tv_destination_location);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvBestTime = itemView.findViewById(R.id.tv_best_time);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}