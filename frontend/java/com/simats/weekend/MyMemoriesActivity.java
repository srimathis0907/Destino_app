package com.simats.weekend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.simats.weekend.adapters.MemoriesPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyMemoriesActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MemoriesPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_memories);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_memories);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager_memories);
        TabLayout tabLayout = findViewById(R.id.tab_layout_memories);

        pagerAdapter = new MemoriesPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Link tabs with the pager for three tabs
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Folders");
                    break;
                case 1:
                    tab.setText("Fav Folders");
                    break;
                case 2:
                    tab.setText("Fav Photos");
                    break;
            }
        }).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This is important: it refreshes the lists every time you return to this screen.
        // It tells the adapter that the data might have changed.
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
            // A more efficient way is to notify the specific fragments, but this works well for now.
        }
    }
}