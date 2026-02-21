package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.weekend.adapters.SavedTripsAdapter;
import com.simats.weekend.databinding.ActivitySavedTripsBinding;
import com.simats.weekend.models.SavedTrip;
import com.simats.weekend.models.SavedTripsResponse;
import com.simats.weekend.models.StatusResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedTripsActivity extends AppCompatActivity implements SavedTripsAdapter.OnSavedTripListener {

    private ActivitySavedTripsBinding binding;
    private SavedTripsAdapter adapter;
    private List<SavedTrip> savedTripsList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedTripsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setSupportActionBar(binding.toolbarSavedTrips);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarSavedTrips.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        fetchSavedTrips();
    }

    private void setupRecyclerView() {
        binding.recyclerViewSaved.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedTripsAdapter(this, savedTripsList, this);
        binding.recyclerViewSaved.setAdapter(adapter);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.recyclerViewSaved.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateUIState() {
        if (savedTripsList.isEmpty()) {
            binding.recyclerViewSaved.setVisibility(View.GONE);
            binding.tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewSaved.setVisibility(View.VISIBLE);
            binding.tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    private void fetchSavedTrips() {
        showLoading(true);
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Please log in to see your saved trips.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            updateUIState();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getSavedTrips(userId).enqueue(new Callback<SavedTripsResponse>() {
            @Override
            public void onResponse(Call<SavedTripsResponse> call, Response<SavedTripsResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    savedTripsList.clear();
                    savedTripsList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SavedTripsActivity.this, "Could not load saved trips.", Toast.LENGTH_SHORT).show();
                }
                updateUIState();
            }

            @Override
            public void onFailure(Call<SavedTripsResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SavedTripsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateUIState();
            }
        });
    }

    @Override
    public void onTripClick(SavedTrip trip) {
        Intent intent = new Intent(this, UviewdetailsActivity.class);
        intent.putExtra("PLACE_ID", trip.getId());
        startActivity(intent);
    }

    @Override
    public void onUnsaveClick(SavedTrip trip, int position) {
        int userId = sessionManager.getUserId();
        Map<String, Integer> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("place_id", trip.getId());

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.removeSavedTrip(body).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(SavedTripsActivity.this, "Trip removed.", Toast.LENGTH_SHORT).show();
                    savedTripsList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUIState();
                } else {
                    Toast.makeText(SavedTripsActivity.this, "Failed to remove trip.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                Toast.makeText(SavedTripsActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}