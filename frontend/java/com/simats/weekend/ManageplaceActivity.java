package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simats.weekend.adapters.ManagePlaceAdapter;
import com.simats.weekend.databinding.ActivityManagePlaceBinding;
import com.simats.weekend.models.AdminPlace;
import com.simats.weekend.models.PlacesResponse;
import com.simats.weekend.models.StatusResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageplaceActivity extends AppCompatActivity implements ManagePlaceAdapter.OnItemClickListener {

    private ActivityManagePlaceBinding binding;
    private ManagePlaceAdapter adapter;
    private List<AdminPlace> placeList = new ArrayList<>();
    private ActivityResultLauncher<Intent> editPlaceLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagePlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupBottomNavigation();
        setupSearch();
        setupActivityLauncher();

        fetchPlaces();
    }

    private void setupActivityLauncher() {
        editPlaceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // The user saved changes in AeditActivity, so we refresh the list.
                        fetchPlaces();
                    }
                }
        );
    }

    private void setupRecyclerView() {
        adapter = new ManagePlaceAdapter(this, placeList, this);
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewPlaces.setAdapter(adapter);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.recyclerViewPlaces.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showMessage(String message) {
        binding.recyclerViewPlaces.setVisibility(View.GONE);
        binding.tvMessage.setVisibility(View.VISIBLE);
        binding.tvMessage.setText(message);
    }

    private void hideMessage() {
        binding.recyclerViewPlaces.setVisibility(View.VISIBLE);
        binding.tvMessage.setVisibility(View.GONE);
    }

    private void fetchPlaces() {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<PlacesResponse> call = apiService.getAllPlaces();

        call.enqueue(new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    placeList.clear();
                    placeList.addAll(response.body().getData());
                    adapter.filterList(new ArrayList<>(placeList)); // Pass a copy for filtering
                    if (placeList.isEmpty()) {
                        showMessage("No places have been added yet.");
                    } else {
                        hideMessage();
                    }
                } else {
                    showMessage(response.body() != null ? response.body().getMessage() : "No places found.");
                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                showLoading(false);
                showMessage("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onEditClick(int placeId) {
        Intent intent = new Intent(this, AeditActivity.class);
        intent.putExtra("PLACE_ID", placeId);
        editPlaceLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(int placeId, String placeName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Place")
                .setMessage("Are you sure you want to delete '" + placeName + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePlace(placeId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlace(int placeId) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Integer> body = new HashMap<>();
        body.put("place_id", placeId);

        Call<StatusResponse> call = apiService.deletePlace(body);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(ManageplaceActivity.this, "Place deleted successfully", Toast.LENGTH_SHORT).show();
                    fetchPlaces(); // Refresh the list
                } else {
                    showLoading(false);
                    Toast.makeText(ManageplaceActivity.this, "Failed to delete place.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ManageplaceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<AdminPlace> filteredList = new ArrayList<>();
        for (AdminPlace item : placeList) {
            if (item.getName().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            if (!placeList.isEmpty()) { // Only show "not found" if there are items to search
                showMessage("Place not found");
            }
        } else {
            hideMessage();
        }
        adapter.filterList(filteredList);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_manage_place);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(getApplicationContext(), AdminhomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_add_place) {
                startActivity(new Intent(getApplicationContext(), AddplaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_manage_place) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), AdminprofileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}