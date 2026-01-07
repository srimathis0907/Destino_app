package com.simats.weekend.adapters;

import android.content.Context;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.simats.weekend.R;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.models.PlaceImage;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
    private final Context context;
    // --- START: UPDATED PART ---
    // Changed the list type from List<String> to List<PlaceImage>
    // Changed to ArrayList to allow easy modification
    private List<PlaceImage> images;

    public ImageSliderAdapter(Context context, List<PlaceImage> images) {
        this.context = context;
        // Initialize with a new ArrayList to prevent modification issues if the original list is immutable
        this.images = new ArrayList<>(images);
    }
    // --- END: UPDATED PART ---

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceImage image = images.get(position);

        // Check if image or URL is null before building the full URL
        if (image != null && image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
            String imageUrl = RetrofitClient.BASE_URL + image.getImageUrl();
            Log.d("ImageSliderAdapter", "Loading image: " + imageUrl); // Add logging

            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.default_placeholder)
                    .error(R.drawable.default_placeholder)
                    .into(holder.imageView);
        } else {
            Log.w("ImageSliderAdapter", "Image or URL is null/empty at position: " + position); // Add logging
            // Set placeholder if URL is invalid
            holder.imageView.setImageResource(R.drawable.default_placeholder);
        }
    }


    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    // --- NEW METHOD ADDED ---
    /**
     * Updates the list of images displayed by the adapter.
     * @param newImages The new list of PlaceImage objects to display.
     */
    public void updateImages(List<PlaceImage> newImages) {
        images.clear(); // Clear the existing images
        if (newImages != null) {
            images.addAll(newImages); // Add all new images
        }
        notifyDataSetChanged(); // Notify the RecyclerView to refresh
        Log.d("ImageSliderAdapter", "Adapter updated with " + getItemCount() + " images."); // Add logging
    }
    // --- END NEW METHOD ---


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image_view);
        }
    }
}