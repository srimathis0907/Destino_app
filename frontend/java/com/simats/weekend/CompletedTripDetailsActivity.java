package com.simats.weekend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
// import android.widget.Spinner; // No longer needed
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.recyclerview.widget.LinearLayoutManager; // No longer needed
// import androidx.recyclerview.widget.RecyclerView; // No longer needed
import com.bumptech.glide.Glide;
// import com.example.weekend.adapters.ReviewEntryAdapter; // No longer needed
import com.simats.weekend.models.SingleTripResponse;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.Trip;
import com.simats.weekend.models.TripReviewResponse;
import com.simats.weekend.utils.LoadingDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText; // NEW IMPORT

import org.json.JSONObject;

import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletedTripDetailsActivity extends AppCompatActivity {

    private int tripId;
    private Trip trip;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;

    private ImageView tripImage;
    private TextView tripTitle, tripDates, tripDuration, tripPeople;
    private TextView transportCost, hotelCost, foodCost, otherCost, totalBudget;
    private Button viewGalleryButton, leaveReviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_trip_details);
        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);

        if (getIntent().hasExtra("FINISHED_TRIP")) {
            trip = (Trip) getIntent().getSerializableExtra("FINISHED_TRIP");
            tripId = trip.getId();
        } else if (getIntent().hasExtra("TRIP_ID")) {
            tripId = getIntent().getIntExtra("TRIP_ID", -1);
        } else {
            Toast.makeText(this, "Error: Trip data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initializeViews();

        if (trip != null) {
            populateUI();
            checkIfUserHasReviewed();
        } else if (tripId != -1) {
            fetchTripDetails();
        }
    }

    private void initializeViews() {
        tripImage = findViewById(R.id.trip_image_detail);
        tripTitle = findViewById(R.id.trip_title_detail);
        tripDates = findViewById(R.id.trip_dates_detail);
        tripDuration = findViewById(R.id.trip_duration_text);
        tripPeople = findViewById(R.id.trip_people_text);
        transportCost = findViewById(R.id.transport_cost_text);
        hotelCost = findViewById(R.id.hotel_cost_text);
        foodCost = findViewById(R.id.food_cost_text);
        otherCost = findViewById(R.id.other_cost_text);
        totalBudget = findViewById(R.id.total_budget_text);
        viewGalleryButton = findViewById(R.id.view_gallery_button);
        leaveReviewButton = findViewById(R.id.leave_review_button);
    }

    private void fetchTripDetails() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<SingleTripResponse> call = apiService.getTripDetails(tripId);
        call.enqueue(new Callback<SingleTripResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleTripResponse> call, @NonNull Response<SingleTripResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    trip = response.body().getData();
                    if (trip != null) {
                        populateUI();
                        checkIfUserHasReviewed();
                    } else {
                        Toast.makeText(CompletedTripDetailsActivity.this, "Could not find trip details.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CompletedTripDetailsActivity.this, "Failed to load trip details.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(@NonNull Call<SingleTripResponse> call, @NonNull Throwable t) {
                Toast.makeText(CompletedTripDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateUI() {
        getSupportActionBar().setTitle("Summary: " + trip.getPlaceName());
        tripTitle.setText(trip.getPlaceName());
        String dateRange = trip.getStartDate() + " - " + trip.getEndDate();
        tripDates.setText(dateRange);

        // --- FIX 2: IMAGE URL BUG ---
        // The path from the server (trip.getPlaceImage()) already contains "uploads/".
        // Adding it again created a "double uploads" bug.
        if (trip.getPlaceImage() != null && !trip.getPlaceImage().isEmpty()) {
            String imageUrl = RetrofitClient.BASE_URL + trip.getPlaceImage(); // REMOVED "uploads/"
            Glide.with(this).load(imageUrl).into(tripImage);
        }
        // --- END OF FIX ---

        tripDuration.setText(String.format(Locale.getDefault(), "%d Days", trip.getNumDays()));
        tripPeople.setText(String.format(Locale.getDefault(), "%d People", trip.getNumPeople()));
        transportCost.setText(formatCurrency(trip.getTransportCost()));
        hotelCost.setText(formatCurrency(trip.getHotelCost()));
        foodCost.setText(formatCurrency(trip.getFoodCost()));
        otherCost.setText(formatCurrency(trip.getOtherCost()));
        totalBudget.setText(formatCurrency(trip.getTotalBudget()));

        viewGalleryButton.setOnClickListener(view -> {
            if (trip.getMediaFolder() != null && !trip.getMediaFolder().isEmpty()) {
                Intent intent = new Intent(this, FolderContentActivity.class);
                intent.putExtra("folder_name", trip.getMediaFolder());
                intent.putExtra("trip_title", trip.getPlaceName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No gallery exists for this trip.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserHasReviewed() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getTripReviews(tripId).enqueue(new Callback<TripReviewResponse>() {
            @Override
            public void onResponse(@NonNull Call<TripReviewResponse> call, @NonNull Response<TripReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    if (response.body().reviews != null && !response.body().reviews.isEmpty()) {
                        leaveReviewButton.setText("Review Submitted");
                        leaveReviewButton.setEnabled(false);
                    } else {
                        leaveReviewButton.setText("Leave a Review");
                        leaveReviewButton.setEnabled(true);
                        // UPDATED: Call the new, simpler dialog function
                        leaveReviewButton.setOnClickListener(v -> showAddSingleReviewDialog());
                    }
                } else {
                    leaveReviewButton.setVisibility(View.VISIBLE);
                    leaveReviewButton.setText("Review Not Available");
                    leaveReviewButton.setEnabled(false);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TripReviewResponse> call, @NonNull Throwable t) {
                leaveReviewButton.setVisibility(View.GONE);
            }
        });
    }

    // --- REWRITTEN: New, simpler review dialog ---
    private void showAddSingleReviewDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        // Use the new layout file you will create
        View dialogView = inflater.inflate(R.layout.dialog_add_single_review, null);
        builder.setView(dialogView);

        // Get the new views from the new layout
        RatingBar ratingBar = dialogView.findViewById(R.id.review_rating_bar);
        TextInputEditText etReviewText = dialogView.findViewById(R.id.et_review_text);

        builder.setTitle("Leave Your Feedback");
        builder.setPositiveButton("Submit", null); // Set to null to override click listener
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();

        // --- FIX 1: Keyboard Bug ---
        // Request focus and show keyboard when the dialog is fully shown
        dialog.setOnShowListener(dialogInterface -> {
            etReviewText.requestFocus();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etReviewText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100); // 100ms delay to ensure the view is ready
        });

        dialog.show();

        // Override the positive button to add validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String text = etReviewText.getText() != null ? etReviewText.getText().toString().trim() : "";

            if (rating == 0) {
                Toast.makeText(this, "Please provide a rating (1-5 stars).", Toast.LENGTH_SHORT).show();
            } else {
                // Call the new, simpler submit function
                submitSingleReviewToServer(rating, text);
                dialog.dismiss();
            }
        });
    }

    // --- REWRITTEN: New, simpler submit method ---
    private void submitSingleReviewToServer(float rating, String reviewText) {
        loadingDialog.startLoadingDialog();
        try {
            // Create a simple JSON object, not an array
            JSONObject payload = new JSONObject();
            payload.put("user_id", sessionManager.getUserId());
            payload.put("place_id", trip.getPlaceId());
            payload.put("trip_id", trip.getId());
            payload.put("category", "Trip"); // Hardcode a category as it's a general review
            payload.put("rating", rating);
            payload.put("review_text", reviewText);

            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

            // Assume addReviews.php is updated to handle this new simple object
            apiService.addReviews(body).enqueue(new Callback<StatusResponse>() {
                @Override
                public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                    loadingDialog.dismissDialog();
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(CompletedTripDetailsActivity.this, "Review submitted successfully!", Toast.LENGTH_LONG).show();
                        checkIfUserHasReviewed(); // Refresh button state
                    } else {
                        Toast.makeText(CompletedTripDetailsActivity.this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(CompletedTripDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            loadingDialog.dismissDialog();
            Log.e("ReviewSubmit", "Error creating JSON payload", e);
        }
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "₹%,.0f", amount);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}