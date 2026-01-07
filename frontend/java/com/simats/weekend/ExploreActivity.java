package com.simats.weekend;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.models.HomeDataResponse;
import com.simats.weekend.models.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreActivity extends AppCompatActivity implements LocationBottomSheetFragment.LocationListener {

    private static final String TAG = "ExploreActivity";
    private RecyclerView placesRecyclerView;
    private PlaceAdapter placeAdapter;
    private List<Place> allPlacesList = new ArrayList<>();
    private EditText searchEditText;
    private TextView locationAddressTextView;
    private TextView locationTitleTextView;
    private Button nearbyButton;
    private Button allPlacesButton;
    private ProgressBar progressBar;
    private Location currentUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        setupViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupSearch();
        updateButtonStyles(true);

        fetchPlacesFromApi();
    }

    private void setupViews() {
        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        locationAddressTextView = findViewById(R.id.locationAddress);
        locationTitleTextView = findViewById(R.id.locationTitle);
        nearbyButton = findViewById(R.id.nearbyButton);
        allPlacesButton = findViewById(R.id.allPlacesButton);
        progressBar = findViewById(R.id.progressBar);

        LinearLayout locationHeader = findViewById(R.id.locationHeader);
        locationHeader.setOnClickListener(v -> {
            LocationBottomSheetFragment bottomSheet = new LocationBottomSheetFragment();
            bottomSheet.show(getSupportFragmentManager(), "LocationBottomSheetFragment");
        });

        allPlacesButton.setOnClickListener(v -> showAllPlaces());
        nearbyButton.setOnClickListener(v -> filterNearbyPlaces());
    }

    @Override
    public void onLocationSelected(Location location) {
        currentUserLocation = location;
        String locationString = String.format(Locale.US, "Lat: %.4f, Lng: %.4f",
                location.getLatitude(),
                location.getLongitude());
        locationTitleTextView.setText("Current Location");
        locationAddressTextView.setText(locationString);
    }

    private void setupRecyclerView() {
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Assuming PlaceAdapter has a constructor like this and an updateList method
        placeAdapter = new PlaceAdapter(this, new ArrayList<>());
        placesRecyclerView.setAdapter(placeAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        if (allPlacesList == null) return;

        List<Place> filteredList;
        if (text.isEmpty()) {
            filteredList = new ArrayList<>(allPlacesList);
        } else {
            String lowerCaseQuery = text.toLowerCase();
            filteredList = allPlacesList.stream()
                    .filter(place -> place.getName().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        placeAdapter.updateList(filteredList);
    }

    private void showAllPlaces() {
        if (placeAdapter != null) {
            placeAdapter.updateList(allPlacesList);
        }
        updateButtonStyles(true);
    }

    private void filterNearbyPlaces() {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Please set your location first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final float NEARBY_DISTANCE_METERS = 1000 * 1000; // 1000 km

        List<Place> nearbyList = new ArrayList<>();
        for (Place place : allPlacesList) {
            if (place.getLatitude() != 0 && place.getLongitude() != 0) {
                Location placeLocation = new Location("place");
                placeLocation.setLatitude(place.getLatitude());
                placeLocation.setLongitude(place.getLongitude());

                float distance = currentUserLocation.distanceTo(placeLocation);
                if (distance <= NEARBY_DISTANCE_METERS) {
                    nearbyList.add(place);
                }
            }
        }
        placeAdapter.updateList(nearbyList);
        updateButtonStyles(false);
    }

    private void updateButtonStyles(boolean isAllPlacesSelected) {
        if (isAllPlacesSelected) {
            allPlacesButton.setBackgroundResource(R.drawable.button_background_selected);
            allPlacesButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            nearbyButton.setBackgroundResource(R.drawable.button_background_unselected);
            nearbyButton.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
        } else {
            nearbyButton.setBackgroundResource(R.drawable.button_background_selected);
            nearbyButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            allPlacesButton.setBackgroundResource(R.drawable.button_background_unselected);
            allPlacesButton.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_explore);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explore) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_trip) {
                startActivity(new Intent(getApplicationContext(), TripActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_name) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void fetchPlacesFromApi() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        // UPDATED: Added a null parameter for the 'month' field
        Call<HomeDataResponse> call = apiService.getPlaces("all", null, null, null);

        call.enqueue(new Callback<HomeDataResponse>() {
            @Override
            public void onResponse(@NonNull Call<HomeDataResponse> call, @NonNull Response<HomeDataResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getPopularPlaces() != null) {
                    allPlacesList = new ArrayList<>(response.body().getPopularPlaces());
                    placeAdapter.updateList(allPlacesList);
                } else {
                    Toast.makeText(ExploreActivity.this, "Could not load places.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HomeDataResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(ExploreActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}