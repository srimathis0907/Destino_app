package com.simats.weekend.fragments;

import com.simats.weekend.adapters.BaseTripsAdapter;
import com.simats.weekend.adapters.CancelledTripsAdapter;

public class CancelledTripsFragment extends BaseTripsFragment {

    public CancelledTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        // --- FIX: Only fetch if we haven't loaded data yet ---
        if (!hasLoadedOnce) {
            fetchTrips();
        }
    }

    @Override
    protected String getTripType() {
        return "cancelled";
    }

    @Override
    protected BaseTripsAdapter createAdapter() {
        return new CancelledTripsAdapter(getActivity(), tripList, this);
    }

    // --- FIX: Removed empty override methods ---
}