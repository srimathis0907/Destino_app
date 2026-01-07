package com.simats.weekend.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.simats.weekend.fragments.FavFoldersFragment;
import com.simats.weekend.fragments.FavPhotosFragment;
import com.simats.weekend.fragments.FoldersFragment;

public class MemoriesPagerAdapter extends FragmentStateAdapter {

    public MemoriesPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new FavFoldersFragment();
            case 2:
                return new FavPhotosFragment();
            default: // case 0
                return new FoldersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // We now have 3 tabs
    }
}