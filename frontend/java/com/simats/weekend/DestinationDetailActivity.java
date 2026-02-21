package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.weekend.R;

public class DestinationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_destination);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Find both buttons
        Button estimateBudgetButton = findViewById(R.id.estimate_budget_button);
        Button saveToTripsButton = findViewById(R.id.saveToTripsButton);

        // Set click listener for the estimate budget button
        estimateBudgetButton.setOnClickListener(v -> {
            // This intent starts the new activity
            Intent intent = new Intent(DestinationDetailActivity.this, EstimateBudgetActivity.class);
            startActivity(intent);
        });

        saveToTripsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Saved to Trips!", Toast.LENGTH_SHORT).show();
        });
    }
}