package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.simats.weekend.adapters.TripsPagerAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TripActivity extends AppCompatActivity {

    // These constants are kept in case other activities need to launch this one with a specific filter.
    public static final String TRIP_TYPE_KEY = "TRIP_TYPE";
    public static final String TYPE_FINISHED = "FINISHED";
    public static final String TYPE_CANCELLED = "CANCELLED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        // --- Toolbar, TabLayout, and ViewPager Setup ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        // NOTE: Ensure your TripsPagerAdapter is updated to match the new tab order.
        TripsPagerAdapter pagerAdapter = new TripsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // UPDATED: Reordered for better user experience
            switch (position) {
                case 0:
                    tab.setText("Future");
                    break;
                case 1:
                    tab.setText("Finished"); // Moved to 2nd position
                    break;
                case 2:
                    tab.setText("Cancelled"); // Moved to 3rd position
                    break;
            }
        }).attach();

        // REMOVED: The checkForFinishedTrips() call is no longer needed here.
        // The FinishedTripsFragment now handles this logic internally.

        // --- Bottom Navigation Logic (Unchanged) ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_trip);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_trip) {
                return true;
            } else if (itemId == R.id.nav_explore) {
                startActivity(new Intent(getApplicationContext(), ExploreActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_name) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // REMOVED: The entire checkForFinishedTrips() method is no longer necessary in this activity.

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}