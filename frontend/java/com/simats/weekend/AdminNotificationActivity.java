package com.simats.weekend;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// ===============================================================
// == UPDATED: IMPORT STATEMENT NOW USES "adapters" (PLURAL) ==
// ===============================================================
import com.simats.weekend.adapters.NotificationAdapter;
import com.simats.weekend.models.NotificationListItem;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification);

        // --- Toolbar Setup ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- RecyclerView Setup ---
        RecyclerView recyclerView = findViewById(R.id.notificationsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // --- Visual Improvement: Add a divider between items ---
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Create and set the adapter with the new data model
        List<NotificationListItem> notifications = createSampleData();
        // ===============================================================
        // == UPDATED: USE THE SIMPLE CLASS NAME AFTER IMPORTING ==
        // ===============================================================
        NotificationAdapter adapter = new NotificationAdapter(this, notifications);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper method updated to use the new sealed interface and records
    private List<NotificationListItem> createSampleData() {
        List<NotificationListItem> items = new ArrayList<>();

        items.add(new NotificationListItem.NotificationHeader("Today"));
        items.add(new NotificationListItem.NotificationItem("New destination added: Amsterdam, Netherlands", "Added to database", "10:45 AM", R.drawable.ic_location, R.color.icon_green));
        items.add(new NotificationListItem.NotificationItem("Weather Alert: Rain predicted in Manali this weekend", "Might affect weekend trips", "8:30 AM", R.drawable.ic_weather, R.color.icon_orange));

        items.add(new NotificationListItem.NotificationHeader("Yesterday"));
        items.add(new NotificationListItem.NotificationItem("Monthly analysis report is ready", "Click to view insights", "July 1, 2025", R.drawable.analysis, R.color.icon_blue));
        items.add(new NotificationListItem.NotificationItem("A trip was auto-cancelled due to missing transport info", "Check Manage Trips > Cancelled", "June 30, 6:10 PM", R.drawable.ic_warning, R.color.icon_red));
        items.add(new NotificationListItem.NotificationItem("You received 8 new reviews this week", "Top destination: Goa", "June 28, 3:00 PM", R.drawable.star, R.color.icon_teal));

        return items;
    }
}