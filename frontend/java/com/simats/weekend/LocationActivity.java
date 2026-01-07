package com.simats.weekend;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

// In a full MVP implementation, this would be split into Contract, Presenter, Model, and View.
// For simplicity in this example, the logic is kept within the Activity.
public class LocationActivity extends AppCompatActivity {

    private MapView map = null;
    private EditText searchEditText;
    private Button autoFetchButton;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permission granted. Click auto-fetch again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is a required step for OSMDroid to function
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_location);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Set Your Location");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        map = findViewById(R.id.map);
        searchEditText = findViewById(R.id.searchLocationEditText);
        autoFetchButton = findViewById(R.id.autoFetchButton);

        setupMap();

        autoFetchButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Implement GPS location fetching logic here
                Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        // TODO: Implement GeocoderNominatim for search functionality
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        // Default to a central point in India
        GeoPoint startPoint = new GeoPoint(20.5937, 78.9629);
        map.getController().setCenter(startPoint);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}