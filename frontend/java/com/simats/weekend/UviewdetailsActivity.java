package com.simats.weekend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.simats.weekend.adapters.ImageSliderAdapter;
import com.simats.weekend.adapters.TopSpotAdapter;
import com.simats.weekend.adapters.TransportAdapter;
import com.simats.weekend.models.NearbyPlacesResponse;
import com.simats.weekend.models.PlaceDetails;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.UPlaceDetailsResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UviewdetailsActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView placeName, placeLocation;
    private RecyclerView topSpotsRecyclerView, transportRecyclerView;
    private CollapsingToolbarLayout collapsingToolbar;
    private FrameLayout loadingOverlay;
    private ImageView loadingIndicator;
    private int placeId;
    private PlaceDetails currentPlaceDetails;
    private SessionManager sessionManager; // ADD THIS

    private FusedLocationProviderClient fusedLocationProviderClient;
    private FrameLayout locationFetchingOverlay;
    private ImageView locationFetchingIcon;
    private Class<?> pendingNavigation;

    private LinearLayout nearbyHotelsButton, nearbyRestaurantsButton, nearbyAttractionsButton;
    private TextView tvBestSeason, tvAvgBudget, tvLocalLanguage;
    private String pendingNearbySearchType = null;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (pendingNearbySearchType != null) {
                        findNearbyPlaces(pendingNearbySearchType);
                    } else {
                        getCurrentLocationAndNavigate();
                    }
                } else {
                    Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_LONG).show();
                    showLocationFetchingAnimation(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uviewdetails);

        sessionManager = new SessionManager(this); // INITIALIZE THIS

        placeId = getIntent().getIntExtra("PLACE_ID", -1);
        if (placeId == -1) {
            Toast.makeText(this, "Error: Place ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setupToolbar();
        initViews();
        setupClickListeners();
        fetchPlaceDetails();
    }

    private void setupClickListeners() {
        Button estimateButton = findViewById(R.id.estimate_budget_button);
        estimateButton.setOnClickListener(v -> {
            if (currentPlaceDetails != null) {
                pendingNearbySearchType = null;
                showTransportChoiceDialog();
            } else {
                Toast.makeText(this, "Place details are still loading.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- START: FULLY UPDATED SAVE BUTTON LOGIC ---
        Button saveButton = findViewById(R.id.save_to_trips_button);
        saveButton.setOnClickListener(v -> {
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                Toast.makeText(this, "You must be logged in to save a trip.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentPlaceDetails == null) {
                Toast.makeText(this, "Details are still loading.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Integer> body = new HashMap<>();
            body.put("user_id", userId);
            body.put("place_id", currentPlaceDetails.getId());

            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
            apiService.saveTrip(body).enqueue(new Callback<StatusResponse>() {
                @Override
                public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(UviewdetailsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        // If successfully saved, disable the button to prevent multiple saves
                        if (response.body().isStatus()) {
                            saveButton.setText("Saved");
                            saveButton.setEnabled(false);
                            saveButton.setAlpha(0.7f);
                        }
                    } else {
                        Toast.makeText(UviewdetailsActivity.this, "Failed to save trip.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StatusResponse> call, Throwable t) {
                    Toast.makeText(UviewdetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        // --- END: FULLY UPDATED SAVE BUTTON LOGIC ---

        nearbyHotelsButton.setOnClickListener(v -> findNearbyPlaces("hotel"));
        nearbyRestaurantsButton.setOnClickListener(v -> findNearbyPlaces("restaurant"));
        nearbyAttractionsButton.setOnClickListener(v -> findNearbyPlaces("attraction"));
    }

    // --- NO CHANGES to other methods ---
    // The rest of your UviewdetailsActivity code remains the same. I am including it for completeness.
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        viewPager = findViewById(R.id.image_slider_view_pager);
        placeName = findViewById(R.id.place_name);
        placeLocation = findViewById(R.id.place_location);
        topSpotsRecyclerView = findViewById(R.id.top_spots_recycler_view);
        transportRecyclerView = findViewById(R.id.transport_recycler_view);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        loadingIndicator = findViewById(R.id.loading_indicator);
        locationFetchingOverlay = findViewById(R.id.location_fetching_overlay);
        locationFetchingIcon = findViewById(R.id.location_fetching_icon);
        topSpotsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transportRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nearbyHotelsButton = findViewById(R.id.nearby_hotels_button);
        nearbyRestaurantsButton = findViewById(R.id.nearby_restaurants_button);
        nearbyAttractionsButton = findViewById(R.id.nearby_attractions_button);
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvBestSeason = findViewById(R.id.tv_best_season);
        tvAvgBudget = findViewById(R.id.tv_avg_budget);
        tvLocalLanguage = findViewById(R.id.tv_local_language);
    }

    private void findNearbyPlaces(String type) {
        if (currentPlaceDetails == null || currentPlaceDetails.getLatitude() == 0.0 || currentPlaceDetails.getLongitude() == 0.0) {
            Toast.makeText(this, "Location data for this place is not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            pendingNearbySearchType = type;
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        pendingNearbySearchType = null;
        showLoading(true);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                showLoading(false);
                Toast.makeText(this, "Could not get your current location. Please enable GPS.", Toast.LENGTH_SHORT).show();
                return;
            }
            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
            Call<NearbyPlacesResponse> call = apiService.getNearbyPlaces(currentPlaceDetails.getLatitude(), currentPlaceDetails.getLongitude(), type);
            call.enqueue(new Callback<NearbyPlacesResponse>() {
                @Override
                public void onResponse(@NonNull Call<NearbyPlacesResponse> call, @NonNull Response<NearbyPlacesResponse> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Intent intent = new Intent(UviewdetailsActivity.this, NearbyPlacesActivity.class);
                        intent.putParcelableArrayListExtra("NEARBY_PLACES", new ArrayList<>(response.body().getData()));
                        intent.putExtra("USER_LAT", location.getLatitude());
                        intent.putExtra("USER_LNG", location.getLongitude());
                        String categoryTitle = "Nearby " + type.substring(0, 1).toUpperCase() + type.substring(1);
                        intent.putExtra("CATEGORY_TITLE", categoryTitle);
                        startActivity(intent);
                    } else {
                        Toast.makeText(UviewdetailsActivity.this, "Could not find nearby places.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<NearbyPlacesResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    Toast.makeText(UviewdetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showTransportChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transport_choice, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        LinearLayout ownTransportButton = dialogView.findViewById(R.id.option_own_transport);
        LinearLayout publicTransportButton = dialogView.findViewById(R.id.option_public_transport);
        ownTransportButton.setOnClickListener(v -> {
            startLocationFetchAndNavigate(EstimateOwnbudgetActivity.class);
            dialog.dismiss();
        });
        publicTransportButton.setOnClickListener(v -> {
            startLocationFetchAndNavigate(EstimateBudgetActivity.class);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void startLocationFetchAndNavigate(Class<?> destinationActivity) {
        this.pendingNavigation = destinationActivity;
        showLocationFetchingAnimation(true);
        checkLocationPermissionAndProceed();
    }

    private void checkLocationPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndNavigate();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocationAndNavigate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showLocationFetchingAnimation(false);
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showLocationFetchingAnimation(false);
                        if (location != null && pendingNavigation != null) {
                            Intent intent = new Intent(UviewdetailsActivity.this, pendingNavigation);
                            intent.putExtra("PLACE_DETAILS", currentPlaceDetails);
                            intent.putExtra("USER_LATITUDE", location.getLatitude());
                            intent.putExtra("USER_LONGITUDE", location.getLongitude());
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Could not get your current location. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }, 1500);
                });
    }

    private void showLocationFetchingAnimation(boolean show) {
        if (show) {
            locationFetchingOverlay.setVisibility(View.VISIBLE);
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            locationFetchingIcon.startAnimation(pulse);
        } else {
            locationFetchingOverlay.setVisibility(View.GONE);
            locationFetchingIcon.clearAnimation();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingOverlay.setVisibility(View.VISIBLE);
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
            loadingIndicator.startAnimation(rotation);
        } else {
            loadingOverlay.setVisibility(View.GONE);
            loadingIndicator.clearAnimation();
        }
    }

    private void fetchPlaceDetails() {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<UPlaceDetailsResponse> call = apiService.getUserPlaceDetails(placeId);
        call.enqueue(new Callback<UPlaceDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UPlaceDetailsResponse> call, @NonNull Response<UPlaceDetailsResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    populateUI(response.body().getData());
                } else {
                    Toast.makeText(UviewdetailsActivity.this, "Failed to load details.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<UPlaceDetailsResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(UviewdetailsActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(PlaceDetails place) {
        this.currentPlaceDetails = place;
        collapsingToolbar.setTitle(place.getName());
        placeName.setText(place.getName());
        placeLocation.setText(place.getLocation());
        ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(this, place.getImages());
        viewPager.setAdapter(imageSliderAdapter);
        TopSpotAdapter topSpotAdapter = new TopSpotAdapter(this, place.getTopSpots());
        topSpotsRecyclerView.setAdapter(topSpotAdapter);
        TransportAdapter transportAdapter = new TransportAdapter(this, place.getTransportOptions());
        transportRecyclerView.setAdapter(transportAdapter);
        tvBestSeason.setText(place.getSuitableMonths() != null ? place.getSuitableMonths() : "N/A");
        tvAvgBudget.setText(place.getAvgBudget() != null ? place.getAvgBudget() : "N/A");
        tvLocalLanguage.setText(place.getLocalLanguage() != null ? place.getLocalLanguage() : "N/A");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}