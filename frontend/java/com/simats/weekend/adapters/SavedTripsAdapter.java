package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.simats.weekend.R;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.models.SavedTrip;
import java.util.List;

public class SavedTripsAdapter extends RecyclerView.Adapter<SavedTripsAdapter.ViewHolder> {

    public interface OnSavedTripListener {
        void onTripClick(SavedTrip trip);
        void onUnsaveClick(SavedTrip trip, int position);
    }

    private final Context context;
    private final List<SavedTrip> savedTrips;
    private final OnSavedTripListener listener;

    public SavedTripsAdapter(Context context, List<SavedTrip> savedTrips, OnSavedTripListener listener) {
        this.context = context;
        this.savedTrips = savedTrips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedTrip trip = savedTrips.get(position);
        holder.tvPlaceName.setText(trip.getName());
        holder.tvPlaceLocation.setText(trip.getLocation());

        String fullUrl = RetrofitClient.BASE_URL + trip.getImageUrl();
        Glide.with(context)
                .load(fullUrl)
                .placeholder(R.drawable.default_placeholder)
                .error(R.drawable.default_placeholder)
                .centerCrop()
                .into(holder.ivPlaceImage);

        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
        holder.btnUnsave.setOnClickListener(v -> listener.onUnsaveClick(trip, position));
    }

    @Override
    public int getItemCount() {
        return savedTrips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        TextView tvPlaceName, tvPlaceLocation, tvViewDetails;
        ImageButton btnUnsave;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.iv_place_image);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvPlaceLocation = itemView.findViewById(R.id.tv_place_location);
            tvViewDetails = itemView.findViewById(R.id.tv_view_details);
            btnUnsave = itemView.findViewById(R.id.btn_unsave);
        }
    }
}