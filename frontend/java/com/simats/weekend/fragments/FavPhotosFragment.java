package com.simats.weekend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.simats.weekend.FavContentAdapter;
import com.simats.weekend.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FavPhotosFragment extends Fragment {

    private FavContentAdapter favContentAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the new container layout
        return inflater.inflate(R.layout.fragment_fav_photos_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        favContentAdapter = new FavContentAdapter(this);
        viewPager = view.findViewById(R.id.fav_view_pager);
        viewPager.setAdapter(favContentAdapter);

        tabLayout = view.findViewById(R.id.fav_tab_layout);
        // Link the TabLayout and the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Photos" : "Videos")
        ).attach();
    }
}