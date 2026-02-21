package com.simats.weekend;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.simats.weekend.fragments.FavPhotosGridFragment;
import com.simats.weekend.fragments.FavVideosGridFragment;

public class FavContentAdapter extends FragmentStateAdapter {

    public FavContentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a new fragment instance for the given position.
        if (position == 0) {
            return new FavPhotosGridFragment();
        } else {
            return new FavVideosGridFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}
