package com.simats.weekend;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.simats.weekend.fragments.PhotosFragment;
import com.simats.weekend.fragments.VideosFragment;

public class FolderContentAdapter extends FragmentStateAdapter {
    private final String folderName;

    public FolderContentAdapter(@NonNull FragmentActivity fragmentActivity, String folderName) {
        super(fragmentActivity);
        this.folderName = folderName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putString("folder_name", folderName);

        Fragment fragment;
        if (position == 0) {
            fragment = new PhotosFragment();
        } else {
            fragment = new VideosFragment();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs: Photos and Videos
    }
}