package com.simats.weekend; // Make sure this matches your package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class UtraveltripsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The layout name here must match your XML file name
        setContentView(R.layout.activity_utraveltrips);

        // Initialize the toolbar
        toolbar = findViewById(R.id.toolbar);

        // Set up the toolbar
        setupToolbar();
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