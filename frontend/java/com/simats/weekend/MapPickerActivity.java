package com.simats.weekend;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.osmdroid.config.Configuration;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapPickerActivity extends AppCompatActivity {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_map_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select a Location");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mapView = findViewById(R.id.map_view);
        Button confirmButton = findViewById(R.id.btn_confirm_location);
        FloatingActionButton myLocationButton = findViewById(R.id.fab_my_location);
        SearchView searchView = findViewById(R.id.search_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupMap();
        setupSearch(searchView);

        myLocationButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        confirmButton.setOnClickListener(v -> returnSelectedLocation());
    }

    private void setupMap() {
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(5.0);
        mapView.getController().setCenter(new GeoPoint(20.5937, 78.9629)); // Center of India
    }

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void searchLocation(String query) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            GeocoderNominatim geocoder = new GeocoderNominatim("WeekendApp/1.0");
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", India", 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        GeoPoint location = new GeoPoint(address.getLatitude(), address.getLongitude());
                        mapView.getController().setZoom(14.0);
                        mapView.getController().animateTo(location);
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(MapPickerActivity.this, "Location not found.", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapView.getController().setZoom(15.0);
                mapView.getController().animateTo(startPoint);
            } else {
                Toast.makeText(this, "Could not get location. Is GPS enabled?", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- THIS METHOD IS UPDATED ---
    private void returnSelectedLocation() {
        GeoPoint center = (GeoPoint) mapView.getMapCenter();
        final double latitude = center.getLatitude();
        final double longitude = center.getLongitude();

        Toast.makeText(this, "Confirming location...", Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            GeocoderNominatim geocoder = new GeocoderNominatim(Locale.US, "WeekendApp/1.0");

            // Default values
            String placeName = "Selected Location";
            String locationName = "";

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    // Smartly build the place name and location
                    String city = address.getLocality();
                    String state = address.getAdminArea();
                    String country = address.getCountryName();

                    placeName = (city != null) ? city : "Unknown Area";
                    locationName = (state != null ? state + ", " : "") + (country != null ? country : "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Prepare the result to send back to AddplaceActivity
            String finalPlaceName = placeName;
            String finalLocationName = locationName;
            new Handler(Looper.getMainLooper()).post(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("placeName", finalPlaceName);
                resultIntent.putExtra("location", finalLocationName); // New data
                resultIntent.putExtra("latitude", latitude);
                resultIntent.putExtra("longitude", longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}