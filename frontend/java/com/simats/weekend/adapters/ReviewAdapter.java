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
import com.simats.weekend.models.Review; // Make sure this is your updated Review model
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This assumes you are using a layout named 'item_review.xml' for displaying
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        // Ensure your item_review.xml has these IDs
        TextView tvReviewerName, tvReviewText;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            // In your item_review.xml, the user's name TextView has the ID 'tv_user_name'
            tvReviewerName = itemView.findViewById(R.id.tv_user_name);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            ratingBar = itemView.findViewById(R.id.rating_bar_display);
        }

        void bind(Review review) {
            // CORRECTED: Accessing public fields directly, not with getter methods
            tvReviewerName.setText(review.userName);
            tvReviewText.setText(review.reviewText);
            ratingBar.setRating(review.rating);
        }
    }
}