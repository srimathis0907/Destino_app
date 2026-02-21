package com.simats.weekend;

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
import com.bumptech.glide.Glide;
import com.simats.weekend.models.Place;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private final Context context;
    private List<Place> placeList;

    public PlaceAdapter(Context context, List<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.placeNameTextView.setText(place.getName());
        holder.bestTimeTag.setText(place.getSuitableMonths());

        // --- THIS IS THE UPDATED PART ---
        // Changed getFirstImageName() to getFirstImageUrl()
        String imageName = place.getFirstImageUrl();
        if (imageName != null) {
            // If there's an image name, it's from the server. Build the full URL.
            String imageUrl = RetrofitClient.BASE_URL + imageName;
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.default_placeholder) // Use the consistent placeholder
                    .error(R.drawable.default_placeholder)
                    .into(holder.placeImageView);
        } else if (place.getImageResourceId() != 0) {
            // Otherwise, if there's a resource ID, it's local dummy data.
            Glide.with(context)
                    .load(place.getImageResourceId())
                    .centerCrop()
                    .placeholder(R.drawable.default_placeholder)
                    .into(holder.placeImageView);
        }
        // --- END OF UPDATE ---

        holder.viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UviewdetailsActivity.class);
            intent.putExtra("PLACE_ID", place.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public void updateList(List<Place> newList) {
        this.placeList = newList;
        notifyDataSetChanged();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImageView;
        TextView placeNameTextView;
        TextView bestTimeTag;
        Button viewDetailsButton;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImageView = itemView.findViewById(R.id.placeImageView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            bestTimeTag = itemView.findViewById(R.id.bestTimeTag);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}