package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.AdminReview;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AreviewAdapter extends RecyclerView.Adapter<AreviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<AdminReview> reviewList;
    private final SimpleDateFormat outputDateFormat; // Reuse formatter

    public AreviewAdapter(Context context, List<AdminReview> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
        // Initialize the date formatter once
        this.outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        AdminReview review = reviewList.get(position);
        holder.bind(review, outputDateFormat); // Pass formatter to bind method
    }

    @Override
    public int getItemCount() {
        // Handle null list gracefully
        return reviewList == null ? 0 : reviewList.size();
    }

    // Renamed for clarity and ensuring a new list is passed
    public void updateList(List<AdminReview> newList) {
        this.reviewList = new ArrayList<>(newList); // Create a new list
        notifyDataSetChanged();
    }

    // Keep filterList if you still need separate filtering logic
    public void filterList(List<AdminReview> filteredList) {
        this.reviewList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }


    // --- ViewHolder Class ---
    static class ReviewViewHolder extends RecyclerView.ViewHolder { // Made static
        TextView tvPlaceName, tvReviewText, tvUserName, tvReviewDate;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar = itemView.findViewById(R.id.rating_bar_display);
        }

        // Updated bind method
        void bind(AdminReview review, SimpleDateFormat dateFormat) {
            // --- FIX IS HERE: Use fullLocation directly ---
            // Check if fullLocation is available and not empty, otherwise fallback gracefully
            if (review.fullLocation != null && !review.fullLocation.isEmpty()) {
                tvPlaceName.setText(review.fullLocation);
            } else if (review.placeName != null) {
                tvPlaceName.setText(review.placeName); // Fallback to just place name
            } else {
                tvPlaceName.setText("Unknown Location"); // Fallback if both are missing
            }
            // --- END OF FIX ---

            // Use null checks for other text fields too
            tvReviewText.setText(review.reviewText != null ? review.reviewText : "");
            tvUserName.setText(review.userName != null ? review.userName : "Unknown User");

            // Set rating
            ratingBar.setRating(review.rating);

            // Format and set date
            if (review.createdAt != null) {
                tvReviewDate.setText(dateFormat.format(review.createdAt));
            } else {
                tvReviewDate.setText(""); // Clear text if date is null
            }
        }
    }
}