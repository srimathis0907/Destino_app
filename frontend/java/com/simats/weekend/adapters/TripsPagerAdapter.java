package com.simats.weekend.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.simats.weekend.fragments.CancelledTripsFragment;
import com.simats.weekend.fragments.FinishedTripsFragment;
import com.simats.weekend.fragments.FutureTripsFragment;

public class TripsPagerAdapter extends FragmentStateAdapter {

    public TripsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // UPDATED: Reordered to match TripActivity's TabLayout
        switch (position) {
            case 0:
                return new FutureTripsFragment();
            case 1:
                return new FinishedTripsFragment();
            case 2:
                return new CancelledTripsFragment();
            default:
                // Fallback to the first fragment in case of an unexpected position
                return new FutureTripsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // We have 3 tabs
    }
}