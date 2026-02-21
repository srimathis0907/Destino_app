package com.simats.weekend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.weekend.adapters.AdminTripAdapter;
import com.simats.weekend.databinding.ActivityManageTripsBinding;
import com.simats.weekend.models.AdminTrip;
import com.simats.weekend.models.AdminTripListResponse;
import com.simats.weekend.models.StatusResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageTripsActivity extends AppCompatActivity {

    private ActivityManageTripsBinding binding;
    private AdminTripAdapter adapter;
    private List<AdminTrip> fullTripList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageTripsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getClient(this).create(ApiService.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Use onResume to refresh data every time the screen becomes visible
        refreshData();
    }

    // This method chains the API calls correctly to ensure data is always fresh
    private void refreshData() {
        binding.progressBarTrips.setVisibility(View.VISIBLE);
        binding.recyclerViewTrips.setVisibility(View.GONE);
        binding.tvNotFoundTrips.setVisibility(View.GONE);

        // Step 1: Update the trip statuses on the server
        Call<StatusResponse> updateCall = apiService.updateAllFinishedTrips();
        updateCall.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Log.d("ManageTrips", "Finished trips updated successfully.");
                } else {
                    Log.e("ManageTrips", "Failed to update finished trips.");
                }
                // Step 2: Now that the update is done, fetch the fresh trip list
                fetchTrips();
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                Log.e("ManageTrips", "Network error updating trips: " + t.getMessage());
                // Still try to fetch the list even if the update fails
                fetchTrips();
            }
        });
    }

    private void fetchTrips() {
        Call<AdminTripListResponse> call = apiService.getAllTripsAdmin();
        call.enqueue(new Callback<AdminTripListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminTripListResponse> call, @NonNull Response<AdminTripListResponse> response) {
                binding.progressBarTrips.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    fullTripList.clear();
                    fullTripList.addAll(response.body().trips);
                    adapter.filterList(new ArrayList<>(fullTripList));

                    if(fullTripList.isEmpty()){
                        binding.tvNotFoundTrips.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerViewTrips.setVisibility(View.VISIBLE);
                    }
                    filter(binding.searchViewTrips.getQuery().toString());
                } else {
                    Toast.makeText(ManageTripsActivity.this, "Failed to load trips", Toast.LENGTH_SHORT).show();
                    binding.tvNotFoundTrips.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminTripListResponse> call, @NonNull Throwable t) {
                binding.progressBarTrips.setVisibility(View.GONE);
                Toast.makeText(ManageTripsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                binding.tvNotFoundTrips.setVisibility(View.VISIBLE);
            }
        });
    }

    // Unchanged methods below
    private void setupToolbar() {
        binding.toolbarManageTrips.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new AdminTripAdapter(this, new ArrayList<>());
        binding.recyclerViewTrips.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTrips.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchViewTrips.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<AdminTrip> filteredList = new ArrayList<>();
        for (AdminTrip item : fullTripList) {
            if (item.placeName.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) ||
                    item.userName.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty() && !fullTripList.isEmpty()) {
            binding.recyclerViewTrips.setVisibility(View.GONE);
            binding.tvNotFoundTrips.setVisibility(View.VISIBLE);
        } else if (!fullTripList.isEmpty()) {
            binding.recyclerViewTrips.setVisibility(View.VISIBLE);
            binding.tvNotFoundTrips.setVisibility(View.GONE);
        }

        adapter.filterList(filteredList);
    }
}