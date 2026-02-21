package com.simats.weekend; // Make sure this matches your package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
// REMOVED: Imports for Toast and SwitchMaterial are no longer needed.

public class UserNotificationActivity extends AppCompatActivity {

    // Declare the Toolbar view
    private Toolbar toolbar;
    // REMOVED: The SwitchMaterial variable is no longer needed.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notification);

        // Initialize the Toolbar from the layout file
        toolbar = findViewById(R.id.toolbar_notifications);
        // REMOVED: The line finding the switch is gone.

        // Set up the toolbar
        setupToolbar();

        // REMOVED: The listener for the switch is gone.
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Enable the back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Handle the back arrow click
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed(); // This acts like the device's back button
        });
    }
}