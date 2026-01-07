package com.simats.weekend;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// UPDATED: Import the new BaseTripsAdapter
import com.simats.weekend.adapters.BaseTripsAdapter;
import com.simats.weekend.adapters.CancelledTripsAdapter;
import com.simats.weekend.adapters.CompletedTripsAdapter;
import com.simats.weekend.fragments.IActionModeController;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.Trip;
import com.simats.weekend.models.TripResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilteredTripsActivity extends AppCompatActivity implements IActionModeController {

    private RecyclerView recyclerView;
    // UPDATED: Use the base adapter type for simplicity
    private BaseTripsAdapter adapter;
    private ArrayList<Trip> tripList;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextView noTripsText;

    private ActionMode actionMode;
    private String tripType;

    // These constants should be defined in a central place, like TripActivity or a Constants class
    public static final String TRIP_TYPE_KEY = "TRIP_TYPE_KEY";
    public static final String TYPE_FINISHED = "FINISHED_TRIPS";
    public static final String TYPE_CANCELLED = "CANCELLED_TRIPS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_trips);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.filtered_trips_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        noTripsText = findViewById(R.id.text_no_trips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tripList = new ArrayList<>();

        tripType = getIntent().getStringExtra(TRIP_TYPE_KEY);

        if (tripType != null) {
            if (tripType.equals(TYPE_FINISHED)) {
                getSupportActionBar().setTitle("Finished Trips");
                adapter = new CompletedTripsAdapter(this, tripList, this);
                // BUG FIX: Use "Finished" (capital F)
                fetchTrips("Finished");
            } else if (tripType.equals(TYPE_CANCELLED)) {
                getSupportActionBar().setTitle("Cancelled Trips");
                adapter = new CancelledTripsAdapter(this, tripList, this);
                fetchTrips("cancelled");
            }
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Could not determine trip type.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchTrips(String status) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        noTripsText.setVisibility(View.GONE);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            noTripsText.setText("Please log in to see your trips.");
            noTripsText.setVisibility(View.VISIBLE);
            return;
        }
        int userId = sessionManager.getUserId();

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<TripResponse> call = apiService.getTrips(userId, status);
        call.enqueue(new Callback<TripResponse>() {
            @Override
            public void onResponse(@NonNull Call<TripResponse> call, @NonNull Response<TripResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Trip> trips = response.body().getData();
                    // UPDATED: Use the adapter's setTrips method
                    if (trips == null || trips.isEmpty()) {
                        noTripsText.setVisibility(View.VISIBLE);
                        tripList.clear(); // Clear the list for the adapter
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tripList.clear();
                        tripList.addAll(trips);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    noTripsText.setText("Failed to load trips.");
                    noTripsText.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TripResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                noTripsText.setText("Network Error.");
                noTripsText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Action Mode Implementation ---

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                deleteSelectedTrips();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            // SIMPLIFIED: No need for instanceof check
            if (adapter != null) {
                adapter.finishSelectionMode();
            }
        }
    };

    private void deleteSelectedTrips() {
        // SIMPLIFIED: No need for instanceof check
        List<Integer> selectedTripIds = adapter.getSelectedTripIds();

        if (selectedTripIds.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to permanently delete " + selectedTripIds.size() + " trip(s)?")
                .setPositiveButton("Delete", (dialog, which) -> performDelete(selectedTripIds))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(List<Integer> selectedTripIds) {
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();
        String tripIdsJson = new Gson().toJson(selectedTripIds);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.deleteTrips(userId, tripIdsJson);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(FilteredTripsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finishActionMode();
                    // Refresh the list
                    if (tripType != null) {
                        // BUG FIX: Use "Finished" (capital F)
                        String statusToFetch = tripType.equals(TYPE_FINISHED) ? "Finished" : "cancelled";
                        fetchTrips(statusToFetch);
                    }
                } else {
                    Toast.makeText(FilteredTripsActivity.this, "Failed to delete trips.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                Toast.makeText(FilteredTripsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void startActionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
            // SIMPLIFIED: No need for instanceof check
            if (adapter != null) {
                adapter.startSelectionMode();
            }
        }
    }

    @Override
    public void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    public void updateActionModeTitle(int count) {
        if (actionMode != null) {
            actionMode.setTitle(count + " selected");
        }
    }
}