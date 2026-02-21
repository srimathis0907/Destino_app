package com.simats.weekend.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.Review;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReviewEntryAdapter extends RecyclerView.Adapter<ReviewEntryAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<Review> reviewEntries;
    private final List<String> categoryOptions = new ArrayList<>(Arrays.asList("Place", "Hotel", "Restaurant", "Things to do"));

    public ReviewEntryAdapter(Context context, List<Review> reviewEntries) {
        this.context = context;
        this.reviewEntries = reviewEntries;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_entry, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviewEntries.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewEntries.size();
    }

    // CORRECTED: The class is now public
    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        public Spinner spinnerCategory;
        public RatingBar ratingBar;
        public EditText etReviewText;
        ImageView removeButton;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            spinnerCategory = itemView.findViewById(R.id.spinner_category);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            etReviewText = itemView.findViewById(R.id.et_review_text);
            removeButton = itemView.findViewById(R.id.btn_remove_review);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);
        }

        void bind(Review review) {
            removeButton.setVisibility(reviewEntries.size() > 1 ? View.VISIBLE : View.GONE);
            ratingBar.setRating(review.rating);
            etReviewText.setText(review.reviewText);

            int selection = categoryOptions.indexOf(review.category);
            spinnerCategory.setSelection(Math.max(selection, 0));

            ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
                if(fromUser) {
                    reviewEntries.get(getAdapterPosition()).rating = rating;
                }
            });

            etReviewText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    reviewEntries.get(getAdapterPosition()).reviewText = s.toString();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            removeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    reviewEntries.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, reviewEntries.size());
                }
            });
        }
    }
}