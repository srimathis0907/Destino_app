package com.simats.weekend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.adapters.AreviewAdapter;
import com.simats.weekend.models.AdminReview;
import com.simats.weekend.models.AdminReviewListResponse;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AreviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AreviewAdapter adapter;
    private List<AdminReview> fullReviewList = new ArrayList<>();
    private SearchView searchView;
    private Button buttonFilterRating;
    private Button buttonFilterDate;
    private ProgressBar progressBar;
    private TextView tvNoReviews;

    private String currentSearchQuery = "";
    private Integer currentRatingFilter = null;
    private Long startDateFilter = null;
    private Long endDateFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_areviews);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Reviews");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initializeViews();
        setupRecyclerView();
        setupSearchListener();
        setupFilterListeners();
        fetchReviews();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.reviews_recycler_view);
        searchView = findViewById(R.id.search_view);
        buttonFilterRating = findViewById(R.id.button_filter_rating);
        buttonFilterDate = findViewById(R.id.button_filter_date);
        progressBar = findViewById(R.id.progress_bar_reviews);
        tvNoReviews = findViewById(R.id.tv_no_reviews);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AreviewAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void fetchReviews() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoReviews.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getReviewsAdmin().enqueue(new Callback<AdminReviewListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminReviewListResponse> call, @NonNull Response<AdminReviewListResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    List<AdminReview> reviews = response.body().reviews;
                    fullReviewList.clear();

                    if (reviews != null && !reviews.isEmpty()) {
                        fullReviewList.addAll(reviews);
                        applyAllFilters();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvNoReviews.setText("No reviews found yet.");
                        tvNoReviews.setVisibility(View.VISIBLE);
                        adapter.filterList(new ArrayList<>());
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    tvNoReviews.setText("Failed to load reviews.");
                    tvNoReviews.setVisibility(View.VISIBLE);
                    adapter.filterList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminReviewListResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                tvNoReviews.setText("Network Error.");
                tvNoReviews.setVisibility(View.VISIBLE);
                adapter.filterList(new ArrayList<>());
                Toast.makeText(AreviewsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- REMOVED: The parseReviewDates() method is no longer needed ---
    // Gson will now handle this automatically in RetrofitClient.

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyAllFilters();
                return true;
            }
        });
    }

    private void setupFilterListeners() {
        buttonFilterRating.setOnClickListener(v -> showRatingFilterDialog());
        buttonFilterDate.setOnClickListener(v -> showDateRangeFilterDialog());
    }

    private void showRatingFilterDialog() {
        final CharSequence[] items = {"All Ratings", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"};
        int checkedItem = (currentRatingFilter == null) ? 0 : (6 - currentRatingFilter);

        new AlertDialog.Builder(this)
                .setTitle("Filter by Rating")
                .setSingleChoiceItems(items, checkedItem, (dialog, item) -> {
                    currentRatingFilter = (item == 0) ? null : (6 - item);
                    applyAllFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDateRangeFilterDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a Date Range");

        if (startDateFilter != null && endDateFilter != null) {
            builder.setSelection(new Pair<>(startDateFilter, endDateFilter));
        }

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            startDateFilter = selection.first;
            endDateFilter = selection.second + (24 * 60 * 60 * 1000 - 1000); // End of day
            applyAllFilters();
        });

        picker.addOnNegativeButtonClickListener(v -> {
            startDateFilter = null;
            endDateFilter = null;
            applyAllFilters();
            Toast.makeText(this, "Date filter cleared", Toast.LENGTH_SHORT).show();
        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void applyAllFilters() {
        List<AdminReview> filteredList = new ArrayList<>(fullReviewList);

        if (!currentSearchQuery.isEmpty()) {
            String lowerCaseQuery = currentSearchQuery.toLowerCase();
            filteredList = filteredList.stream()
                    .filter(review -> (review.userName != null && review.userName.toLowerCase().contains(lowerCaseQuery)) ||
                            (review.reviewText != null && review.reviewText.toLowerCase().contains(lowerCaseQuery)) ||
                            (review.placeName != null && review.placeName.toLowerCase().contains(lowerCaseQuery)) ||
                            (review.fullLocation != null && review.fullLocation.toLowerCase().contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
        }

        if (currentRatingFilter != null) {
            filteredList = filteredList.stream()
                    .filter(review -> Math.round(review.rating) == currentRatingFilter)
                    .collect(Collectors.toList());
        }

        // --- UPDATED: This logic now works because review.createdAt is a valid Date object ---
        if (startDateFilter != null && endDateFilter != null) {
            filteredList = filteredList.stream()
                    .filter(review -> review.createdAt != null &&
                            !review.createdAt.before(new Date(startDateFilter)) &&
                            !review.createdAt.after(new Date(endDateFilter)))
                    .collect(Collectors.toList());
        }

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (!currentSearchQuery.isEmpty() || currentRatingFilter != null || startDateFilter != null) {
                tvNoReviews.setText("No matching reviews found for the current filters.");
            } else if (fullReviewList.isEmpty()) {
                tvNoReviews.setText("No reviews found yet.");
            } else {
                tvNoReviews.setText("No reviews found.");
            }
            tvNoReviews.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoReviews.setVisibility(View.GONE);
        }
        adapter.filterList(filteredList);
    }
}