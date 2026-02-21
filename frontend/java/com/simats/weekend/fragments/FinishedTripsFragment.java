package com.simats.weekend.fragments;

import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import com.simats.weekend.ApiService;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.SessionManager;
import com.simats.weekend.adapters.BaseTripsAdapter;
import com.simats.weekend.adapters.CompletedTripsAdapter;
import com.simats.weekend.models.StatusResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinishedTripsFragment extends BaseTripsFragment {

    public FinishedTripsFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        // --- FIX: Only fetch if we haven't loaded data yet ---
        if (!hasLoadedOnce) {
            runTripStatusUpdateAndFetch();
        }
    }

    @Override
    protected String getTripType() {
        return "Finished";
    }

    @Override
    protected BaseTripsAdapter createAdapter() {
        return new CompletedTripsAdapter(getActivity(), tripList, this);
    }

    private void runTripStatusUpdateAndFetch() {
        // --- FIX: This check is now handled in the base fetchTrips() ---
        // We just need to make sure the load happens.

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (messageTextView != null) messageTextView.setVisibility(View.GONE);

        SessionManager sessionManager = new SessionManager(getContext());
        if (!sessionManager.isLoggedIn()) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (messageTextView != null) {
                messageTextView.setText("Please log in to see your trips.");
                messageTextView.setVisibility(View.VISIBLE);
            }
            return;
        }
        int userId = sessionManager.getUserId();
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);

        Call<StatusResponse> updateCall = apiService.updateFinishedTrips(userId);
        updateCall.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                Log.d("FinishedTrips", "Status update check completed.");
                fetchTrips();
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                Log.e("FinishedTrips", "Status update check failed: " + t.getMessage());
                // Still try to fetch, the base method will handle the failure.
                fetchTrips();
            }
        });
    }

    // --- FIX: Removed empty override methods ---
}