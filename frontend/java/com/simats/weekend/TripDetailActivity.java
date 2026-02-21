package com.simats.weekend; // Make sure this matches your package name

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TripDetailActivity extends AppCompatActivity {

    // Declare the views
    private Toolbar toolbar;
    private TextView tripTitle, tripLocation, tripDate;
    private ImageView tripImage;
    private Button cancelTripButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // Initialize the views
        toolbar = findViewById(R.id.toolbar_detail);
        tripTitle = findViewById(R.id.trip_detail_title);
        tripLocation = findViewById(R.id.trip_detail_location);
        tripDate = findViewById(R.id.trip_detail_date);
        tripImage = findViewById(R.id.trip_detail_image);
        cancelTripButton = findViewById(R.id.cancel_trip_button);

        // Set up the toolbar
        setupToolbar();

        // Populate data (in a real app, this would come from an Intent)
        populateDummyData();

        // Set listener for the cancel button
        cancelTripButton.setOnClickListener(v -> {
            // Add logic for what happens when the button is clicked
            Toast.makeText(this, "Cancel Trip Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Handle the click of the back button
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed(); // This will act like the device's back button
        });
    }

    private void populateDummyData() {
        tripTitle.setText("Tokyo City Break");
        tripLocation.setText("Tokyo, Japan");
        tripDate.setText("18 Aug - 20 Aug 2025");
        // In a real app you would load an image from a URL or resource ID
        // tripImage.setImageResource(R.drawable.tokyo);
    }
}