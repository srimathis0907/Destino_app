package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.simats.weekend.R;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.models.Place;
import java.util.List;

public class PopularPlacesAdapter extends RecyclerView.Adapter<PopularPlacesAdapter.ViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    private final Context context;
    private final List<Place> places;
    private final OnPlaceClickListener clickListener;

    public PopularPlacesAdapter(Context context, List<Place> places, OnPlaceClickListener listener) {
        this.context = context;
        this.places = places;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = places.get(position);

        holder.name.setText(place.getName());
        holder.location.setText(place.getLocation());

        // --- START: UPDATED PART ---
        // We now use the getFirstImageUrl() helper method from the updated Place model
        String firstImageUrl = place.getFirstImageUrl();
        if (firstImageUrl != null) {
            String fullUrl = RetrofitClient.BASE_URL + firstImageUrl;
            Glide.with(context)
                    .load(fullUrl)
                    .centerCrop()
                    .placeholder(R.drawable.default_placeholder) // Use consistent placeholder
                    .error(R.drawable.default_placeholder)
                    .into(holder.image);
        } else {
            // Set a default image if there are no images for the place
            holder.image.setImageResource(R.drawable.default_placeholder);
        }
        // --- END: UPDATED PART ---

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPlaceClick(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, location;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.popular_place_image);
            name = itemView.findViewById(R.id.popular_place_name);
            location = itemView.findViewById(R.id.popular_place_location);
        }
    }
}