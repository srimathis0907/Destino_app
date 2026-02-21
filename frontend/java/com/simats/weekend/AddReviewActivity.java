package com.simats.weekend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.simats.weekend.models.StatusResponse;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReviewActivity extends AppCompatActivity {

    private int placeId;
    private RatingBar ratingBar;
    private EditText reviewTextInput;
    private Button submitButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        placeId = getIntent().getIntExtra("PLACE_ID", -1);
        if (placeId == -1) {
            Toast.makeText(this, "Error: Place ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ratingBar = findViewById(R.id.rating_bar_input);
        reviewTextInput = findViewById(R.id.review_text_input);
        submitButton = findViewById(R.id.submit_review_button);
        progressBar = findViewById(R.id.progress_bar);

        submitButton.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String reviewText = reviewTextInput.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please provide a star rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Replace with your actual logged-in user ID
        int userId = 1; // Placeholder for the real user ID

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.addReview(userId, placeId, rating, reviewText);

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(AddReviewActivity.this, "Review submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddReviewActivity.this, "Failed to submit review. You may have already reviewed this place.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(AddReviewActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!isLoading);
    }
}