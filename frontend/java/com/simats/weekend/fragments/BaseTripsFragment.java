package com.simats.weekend.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.adapters.BaseTripsAdapter;
import com.simats.weekend.models.Trip;
import com.simats.weekend.models.TripResponse;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.ApiService;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.SessionManager;
import com.google.gson.Gson;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseTripsFragment extends Fragment implements IActionModeController {

    protected RecyclerView recyclerView;
    protected ProgressBar progressBar;
    protected TextView messageTextView;
    protected BaseTripsAdapter adapter;
    protected List<Trip> tripList = new ArrayList<>();
    protected ActionMode actionMode;

    // --- FIX: Add this flag to prevent reloading on every swipe ---
    protected boolean hasLoadedOnce = false;

    protected abstract String getTripType();
    protected abstract BaseTripsAdapter createAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_common_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.tripsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        messageTextView = view.findViewById(R.id.messageTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = createAdapter();
        recyclerView.setAdapter(adapter);

        // --- FIX: Data will be loaded by subclasses in onResume() *once* ---
    }

    // --- REMOVED onResume() ---
    // We let the subclasses handle their own data loading logic.

    protected void fetchTrips() {
        // --- FIX: Prevent re-fetching if data is already loaded ---
        if (hasLoadedOnce) {
            // If we've loaded before, just ensure the UI is in the correct state
            // This handles the case where one tab times out but another shouldn't be hidden
            if (adapter.getItemCount() > 0) {
                recyclerView.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                messageTextView.setText("No " + getTripType() + " trips found.");
                messageTextView.setVisibility(View.VISIBLE);
            }
            return; // Don't fetch again
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        messageTextView.setVisibility(View.GONE);

        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            messageTextView.setText("Please log in to see your trips.");
            messageTextView.setVisibility(View.VISIBLE);
            return;
        }

        int userId = session.getUserId();
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        Call<TripResponse> call = api.getTrips(userId, getTripType());
        call.enqueue(new Callback<TripResponse>() {
            @Override
            public void onResponse(@NonNull Call<TripResponse> call, @NonNull Response<TripResponse> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                // --- FIX: Set the flag on successful load ---
                hasLoadedOnce = true;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Trip> trips = response.body().getData();
                    if (trips == null || trips.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        messageTextView.setText("No " + getTripType() + " trips found.");
                        messageTextView.setVisibility(View.VISIBLE);
                        adapter.setTrips(new ArrayList<>());
                    } else {
                        messageTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        onTripsLoaded(trips);
                        adapter.setTrips(trips);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    messageTextView.setText("Failed to load trips.");
                    messageTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TripResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                messageTextView.setText("Network Error. Please try again.");
                messageTextView.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onTripsLoaded(List<Trip> trips) {
        // Subclasses can override this
    }

    protected final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
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
            if (adapter != null) {
                adapter.finishSelectionMode();
            }
        }
    };

    protected void deleteSelectedTrips() {
        List<Integer> selectedIds = adapter.getSelectedTripIds();
        if (selectedIds.isEmpty()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Delete " + selectedIds.size() + " trip(s)? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> performDelete(selectedIds))
                .setNegativeButton("Cancel", null)
                .show();
    }

    protected void performDelete(List<Integer> tripIds) {
        SessionManager session = new SessionManager(requireContext());
        int userId = session.getUserId();
        String tripIdsJson = new Gson().toJson(tripIds);
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        Call<StatusResponse> call = api.deleteTrips(userId, tripIdsJson);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    finishActionMode();
                    hasLoadedOnce = false; // Force refresh after deletion
                    fetchTrips();
                } else {
                    Toast.makeText(getContext(), "Delete failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void startActionMode() {
        if (actionMode == null && getActivity() instanceof AppCompatActivity) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
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