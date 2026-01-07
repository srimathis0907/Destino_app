package com.simats.weekend.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.simats.weekend.NotificationScheduler;
import com.simats.weekend.adapters.BaseTripsAdapter;
import com.simats.weekend.adapters.FutureTripsAdapter;
import com.simats.weekend.models.Trip;
import java.util.List;

// No changes needed in this file
public class FutureTripsFragment extends BaseTripsFragment {

    private ActivityResultLauncher<Intent> tripDetailsLauncher;

    public FutureTripsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // This logic is already correct. If details activity sends RESULT_OK,
                    // we force a refresh which will fetch the updated list (including active trip).
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        hasLoadedOnce = false; // Force a refresh
                        fetchTrips();
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasLoadedOnce) {
            fetchTrips();
        }
    }

    @Override
    protected String getTripType() {
        // This remains 'future' because the PHP script now handles including 'active' trips
        // when 'future' is requested.
        return "future";
    }

    @Override
    protected BaseTripsAdapter createAdapter() {
        return new FutureTripsAdapter(getContext(), tripList, tripDetailsLauncher, this);
    }

    @Override
    protected void onTripsLoaded(List<Trip> trips) {
        if (getContext() != null) {
            for (Trip trip : trips) {
                // Only schedule notifications for truly future trips, not active ones
                if ("future".equalsIgnoreCase(trip.getStatus())) {
                    NotificationScheduler.scheduleNotification(getContext(), trip);
                }
            }
        }
    }
}