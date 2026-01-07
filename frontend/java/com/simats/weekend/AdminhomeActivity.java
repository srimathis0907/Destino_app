package com.simats.weekend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.simats.weekend.databinding.ActivityAdminHomeBinding;
import com.simats.weekend.models.PopularPlaceStat;
import com.simats.weekend.models.PopularPlaceStatsResponse;
import com.simats.weekend.models.StatusResponse;
// UPDATED: Make sure this model has fields for future_trips, ongoing_trips, completed_trips, cancelled_trips
import com.simats.weekend.models.TripStatsResponse;
import com.simats.weekend.models.UserStatsResponse;
import android.view.LayoutInflater;

import com.simats.weekend.models.AdminReview;
import com.simats.weekend.models.AdminReviewListResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import Locale

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminhomeActivity extends AppCompatActivity {

    private ActivityAdminHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            // Adjust ScrollView padding to avoid overlap with bottom navigation
            binding.scrollViewContent.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        setupButtonClickListeners();
        setupBottomNavigation();

        binding.barChart.setNoDataText("Loading chart data...");
        binding.barChart.invalidate();

        // Start loading all dashboard data
        updateFinishedTrips(); // This now chains the loading calls
    }

    private void updateFinishedTrips() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.updateAllFinishedTrips();

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                Log.d("AdminHome", "Finished trips update check completed.");
                // After update completes (success or fail), load all dashboard data
                loadUserStats();
                loadTripStats();
                loadBarChartData();
                loadLatestReviews();
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                Log.e("AdminHome", "Network error during trip status update: " + t.getMessage());
                // Still load dashboard data even if update fails
                loadUserStats();
                loadTripStats();
                loadBarChartData();
                loadLatestReviews();
            }
        });
    }

    private void loadLatestReviews() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getLatestReviews().enqueue(new Callback<AdminReviewListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminReviewListResponse> call, @NonNull Response<AdminReviewListResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    List<AdminReview> reviews = response.body().reviews;
                    updateLatestReviewsUI(reviews);
                } else {
                    binding.tvNoReviews.setText("Failed to load reviews.");
                    binding.tvNoReviews.setVisibility(View.VISIBLE);
                    binding.latestReviewsContainer.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<AdminReviewListResponse> call, @NonNull Throwable t) {
                binding.tvNoReviews.setText("Network error.");
                binding.tvNoReviews.setVisibility(View.VISIBLE);
                binding.latestReviewsContainer.setVisibility(View.GONE);
            }
        });
    }

    private void updateLatestReviewsUI(List<AdminReview> reviews) {
        binding.latestReviewsContainer.removeAllViews(); // Clear old views

        if (reviews == null || reviews.isEmpty()) {
            binding.tvNoReviews.setVisibility(View.VISIBLE);
            binding.latestReviewsContainer.setVisibility(View.GONE);
        } else {
            binding.tvNoReviews.setVisibility(View.GONE);
            binding.latestReviewsContainer.setVisibility(View.VISIBLE);

            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < reviews.size(); i++) {
                AdminReview review = reviews.get(i);
                View reviewView = inflater.inflate(R.layout.item_latest_review, binding.latestReviewsContainer, false);

                TextView placeName = reviewView.findViewById(R.id.review_place_name);
                TextView ratingText = reviewView.findViewById(R.id.review_rating_text);
                TextView userName = reviewView.findViewById(R.id.review_user_name);
                TextView reviewContent = reviewView.findViewById(R.id.review_text);
                View divider = reviewView.findViewById(R.id.divider);

                // Use full_location if available, otherwise fallback to place_name
                placeName.setText(review.fullLocation != null ? review.fullLocation : review.placeName);
                userName.setText(review.userName);
                reviewContent.setText(review.reviewText);
                ratingText.setText(getStarsFromRating(review.rating));

                // Hide the divider for the last item
                divider.setVisibility(i == reviews.size() - 1 ? View.GONE : View.VISIBLE);

                binding.latestReviewsContainer.addView(reviewView);
            }
        }
    }

    private String getStarsFromRating(float rating) {
        int fullStars = (int) rating;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★"); // Filled star
        }
        // Handle potential half star if needed, otherwise just pad
        while(stars.length() < 5){
            stars.append("☆"); // Empty star
        }
        return stars.toString();
    }

    private void loadUserStats() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<UserStatsResponse> call = apiService.getUserStats();

        call.enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserStatsResponse> call, @NonNull Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    UserStatsResponse stats = response.body();
                    binding.tvTotalUsersStat.setText(String.valueOf(stats.totalUsers));
                    binding.tvActiveUsersStat.setText(String.valueOf(stats.activeUsers));
                    binding.tvBlockedUsersStat.setText(String.valueOf(stats.blockedUsers));
                } else {
                    Toast.makeText(AdminhomeActivity.this, "Failed to load user stats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserStatsResponse> call, @NonNull Throwable t) {
                Toast.makeText(AdminhomeActivity.this, "Network Error loading user stats: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminHome", "User stats API call failed", t);
            }
        });
    }

    private void loadTripStats() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<TripStatsResponse> call = apiService.getTripStats();

        call.enqueue(new Callback<TripStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TripStatsResponse> call, @NonNull Response<TripStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    TripStatsResponse stats = response.body();
                    // --- UPDATED: Set text for all four counts ---
                    binding.tvFutureTripsStat.setText(String.valueOf(stats.futureTrips));
                    binding.tvOngoingTripsStat.setText(String.valueOf(stats.ongoingTrips));
                    binding.tvCompletedTripsStat.setText(String.valueOf(stats.completedTrips));
                    binding.tvCancelledTripsStat.setText(String.valueOf(stats.cancelledTrips));
                    // --- END OF UPDATE ---
                } else {
                    Toast.makeText(AdminhomeActivity.this, "Failed to load trip stats", Toast.LENGTH_SHORT).show();
                    // Set default text on failure
                    binding.tvFutureTripsStat.setText("0");
                    binding.tvOngoingTripsStat.setText("0");
                    binding.tvCompletedTripsStat.setText("0");
                    binding.tvCancelledTripsStat.setText("0");
                }
            }

            @Override
            public void onFailure(@NonNull Call<TripStatsResponse> call, @NonNull Throwable t) {
                Log.e("AdminHome", "Trip stats API call failed", t);
                Toast.makeText(AdminhomeActivity.this, "Network Error loading trip stats: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Set default text on failure
                binding.tvFutureTripsStat.setText("-");
                binding.tvOngoingTripsStat.setText("-");
                binding.tvCompletedTripsStat.setText("-");
                binding.tvCancelledTripsStat.setText("-");
            }
        });
    }

    private void loadBarChartData() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getPopularPlaceStats().enqueue(new Callback<PopularPlaceStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PopularPlaceStatsResponse> call, @NonNull Response<PopularPlaceStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    setupBarChart(response.body().stats);
                } else {
                    Log.e("AdminHome", "Failed to load bar chart data");
                    binding.barChart.clear();
                    binding.barChart.setNoDataText("Could not load chart data.");
                    binding.barChart.invalidate();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PopularPlaceStatsResponse> call, @NonNull Throwable t) {
                Log.e("AdminHome", "Network error loading bar chart data", t);
                binding.barChart.clear();
                binding.barChart.setNoDataText("Network Error.");
                binding.barChart.invalidate();
            }
        });
    }

    private void setupBarChart(List<PopularPlaceStat> stats) {
        if (stats == null || stats.isEmpty()) {
            binding.barChart.clear();
            binding.barChart.setNoDataText("No trip data available to display.");
            binding.barChart.invalidate();
            return;
        }

        BarChart barChart = binding.barChart;
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            PopularPlaceStat stat = stats.get(i);
            entries.add(new BarEntry(i, stat.tripCount));
            labels.add(stat.placeName); // Use place name for labels
        }

        BarDataSet dataSet = new BarDataSet(entries, "Trip Count"); // Changed label
        dataSet.setColor(ContextCompat.getColor(this, R.color.teal_700));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        // Format values as integers
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChart.setData(barData);

        // Configure X-Axis (Labels)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setGranularity(1f); // Ensure labels align with bars
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-30); // Rotate labels if they overlap

        // General Chart Styling
        barChart.getDescription().setEnabled(false); // Hide description label
        barChart.getLegend().setEnabled(false); // Hide legend (only one dataset)
        barChart.getAxisRight().setEnabled(false); // Hide right Y-axis
        barChart.getAxisLeft().setDrawGridLines(false); // Hide horizontal grid lines
        barChart.getAxisLeft().setDrawAxisLine(true); // Show left Y-axis line
        barChart.getAxisLeft().setAxisMinimum(0f); // Start Y-axis at 0
        barChart.getAxisLeft().setTextColor(Color.DKGRAY);
        barChart.setExtraBottomOffset(10f); // Add padding below labels
        barChart.setFitBars(true); // Make bars fit within chart area
        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(true); // Allow pinch zoom
        barChart.setScaleEnabled(true); // Allow scaling
        barChart.animateY(1200); // Animate bars appearing
        barChart.invalidate(); // Refresh chart
    }

    private void setupButtonClickListeners() {
        binding.btnViewAllUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminhomeActivity.this, ManageUserActivity.class));
        });
        binding.btnViewAllTrips.setOnClickListener(v -> {
            startActivity(new Intent(AdminhomeActivity.this, ManageTripsActivity.class));
        });
        binding.btnViewAllReviews.setOnClickListener(v -> {
            startActivity(new Intent(AdminhomeActivity.this, AreviewsActivity.class));
        });
        binding.settingsIcon.setOnClickListener(v -> {
            startActivity(new Intent(AdminhomeActivity.this, AdminSettingActivity.class));
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_admin_dashboard);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dashboard) {
                return true; // Already on this screen
            } else if (id == R.id.nav_add_place) {
                startActivity(new Intent(getApplicationContext(), AddplaceActivity.class));
                overridePendingTransition(0, 0); // No animation
                finish(); // Close this activity
                return true;
            } else if (id == R.id.nav_manage_place) {
                startActivity(new Intent(getApplicationContext(), ManageplaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), AdminprofileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false; // Item not handled
        });
    }
}