package com.simats.weekend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

// --- FIX: Import MaterialCardView ---
import com.google.android.material.card.MaterialCardView;
// --- END FIX ---

import com.simats.weekend.adapters.ImageSliderAdapter;
import com.simats.weekend.adapters.PopularPlacesAdapter;
import com.simats.weekend.models.HomeDataResponse;
import com.simats.weekend.models.Place;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements PopularPlacesAdapter.OnPlaceClickListener, FilterBottomSheetFragment.OnFiltersAppliedListener {

    private static final String TAG = "HomeActivity";

    // --- UI VIEWS ---
    private BottomNavigationView bottomNavigationView;
    private ImageView settingsIcon;
    private View photoLoadingLayout; // Assuming this is the LinearLayout container
    private ImageView photoLoadingSpinner; // Assuming this is the ProgressBar/ImageView inside

    // --- FIX: Change variable types ---
    private MaterialCardView takePhotoLayout, myGalleryLayout, favoritesLayout;
    // --- END FIX ---

    private Button exploreNowButton;
    private MaterialButton filterButton;
    private TextView greetingText, recommendedPlaceName, recommendedPlaceDetails, recommendedPlaceDescription, popularThisMonthTitle;
    private ViewPager2 recommendedPlaceImagePager;
    private RecyclerView popularPlacesRecyclerView;
    private View recommendedPlaceCard; // Assuming this ID exists and is a View/ViewGroup

    // --- DATA & ADAPTERS ---
    private Place recommendedPlace;
    private PopularPlacesAdapter popularPlacesAdapter;
    private List<Place> popularPlacesList = new ArrayList<>();

    // --- LOCATION & PERMISSIONS ---
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchNearbyPlaces();
                } else {
                    Toast.makeText(this, "Location permission is required to find nearby places.", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Media saved to gallery!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Media capture was cancelled by user.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findViews(); // Call findViews where variables are assigned
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        setupClickListeners();
        setupPopularPlacesRecyclerView();

        fetchHomeData("current_month", null, null, null);
    }

    // Method where the fix is applied
    private void findViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        settingsIcon = findViewById(R.id.settingsIcon);
        photoLoadingLayout = findViewById(R.id.loadingLayout); // Make sure this ID exists
        // Assuming loading_spinner is the ID for the spinner inside loadingLayout
        // If not, adjust this ID. If photoLoadingSpinner is not used, remove it.
        // photoLoadingSpinner = findViewById(R.id.loading_spinner);

        // --- FIX: Cast to MaterialCardView ---
        takePhotoLayout = findViewById(R.id.takePhotoLayout);
        myGalleryLayout = findViewById(R.id.myGalleryLayout);
        favoritesLayout = findViewById(R.id.favoritesLayout);
        // --- END FIX ---

        exploreNowButton = findViewById(R.id.exploreNowButton);
        greetingText = findViewById(R.id.greetingText);
        recommendedPlaceCard = findViewById(R.id.recommended_place_card); // Make sure this ID exists
        recommendedPlaceImagePager = findViewById(R.id.recommendedPlaceImagePager);
        recommendedPlaceName = findViewById(R.id.recommendedPlaceName);
        recommendedPlaceDetails = findViewById(R.id.recommendedPlaceDetails);
        recommendedPlaceDescription = findViewById(R.id.recommendedPlaceDescription);
        popularPlacesRecyclerView = findViewById(R.id.popular_places_recycler_view);
        popularThisMonthTitle = findViewById(R.id.popular_this_month_title);
        filterButton = findViewById(R.id.filter_button);
    }


    private void setupClickListeners() {
        settingsIcon.setOnClickListener(v -> launchActivity(UsettingsActivity.class));

        // Click listeners work the same for MaterialCardView as LinearLayout
        takePhotoLayout.setOnClickListener(v -> {
            showLoading(true);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                cameraLauncher.launch(intent);
                // Hide loading overlay shortly after launching, assuming CameraActivity shows quickly
                new Handler(Looper.getMainLooper()).postDelayed(() -> showLoading(false), 500);
            }, 200); // Short delay before launch
        });
        myGalleryLayout.setOnClickListener(v -> launchActivity(MyMemoriesActivity.class));
        favoritesLayout.setOnClickListener(v -> launchActivity(SavedTripsActivity.class));


        filterButton.setOnClickListener(v -> {
            FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();
            filterSheet.show(getSupportFragmentManager(), filterSheet.getTag());
        });

        // Use recommendedPlaceCard (the TextView title) to check visibility if needed,
        // exploreNowButton click listener remains the same.
        if (exploreNowButton != null) {
            exploreNowButton.setOnClickListener(v -> {
                if (recommendedPlace != null) {
                    onPlaceClick(recommendedPlace); // Navigate using place object
                } else {
                    Toast.makeText(HomeActivity.this, "Place details not available yet.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true; // Already here

            Class<?> destinationActivity = null;
            if (id == R.id.nav_explore) destinationActivity = ExploreActivity.class;
            else if (id == R.id.nav_trip) destinationActivity = TripActivity.class;
            else if (id == R.id.nav_name) destinationActivity = ProfileActivity.class;

            if (destinationActivity != null) {
                launchActivityAndFinish(destinationActivity); // Use helper to finish current
                return true; // Return true as item selection is handled
            }
            return false; // Item not handled
        });
    }

    private void setupPopularPlacesRecyclerView() {
        popularPlacesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Initialize adapter only once
        if (popularPlacesAdapter == null) {
            popularPlacesAdapter = new PopularPlacesAdapter(this, popularPlacesList, this);
        }
        popularPlacesRecyclerView.setAdapter(popularPlacesAdapter);
    }

    private void fetchHomeData(String filterType, String month, Double userLat, Double userLng) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<HomeDataResponse> call = apiService.getPlaces(filterType, month, userLat, userLng);
        call.enqueue(new Callback<HomeDataResponse>() {
            @Override
            public void onResponse(@NonNull Call<HomeDataResponse> call, @NonNull Response<HomeDataResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    HomeDataResponse data = response.body();

                    // Update Recommended Place only if filter type allows it
                    if (("current_month".equals(filterType) || "nearby".equals(filterType)) && data.getRecommendedPlace() != null) {
                        updateRecommendedPlaceUI(data.getRecommendedPlace());
                        if (recommendedPlaceCard != null) recommendedPlaceCard.setVisibility(View.VISIBLE);
                        if (exploreNowButton != null) exploreNowButton.setVisibility(View.VISIBLE);
                    } else {
                        // Hide recommended section if no specific recommendation for this filter
                        recommendedPlace = null; // Clear old data
                        if (recommendedPlaceCard != null) recommendedPlaceCard.setVisibility(View.GONE);
                        recommendedPlaceName.setText(""); // Clear text
                        recommendedPlaceDetails.setText("");
                        recommendedPlaceDescription.setText("");
                        if (exploreNowButton != null) exploreNowButton.setVisibility(View.GONE);
                        // Clear image pager if needed
                        if (recommendedPlaceImagePager.getAdapter() != null) {
                            ((ImageSliderAdapter)recommendedPlaceImagePager.getAdapter()).updateImages(new ArrayList<>());
                        }
                    }

                    // Always update popular places based on response
                    updatePopularPlacesUI(data.getPopularPlaces());

                } else {
                    Toast.makeText(HomeActivity.this, "No places found for this filter.", Toast.LENGTH_SHORT).show();
                    updatePopularPlacesUI(new ArrayList<>()); // Show empty state for popular places
                    // Hide recommended section as well on general failure/empty response
                    recommendedPlace = null;
                    if (recommendedPlaceCard != null) recommendedPlaceCard.setVisibility(View.GONE);
                    recommendedPlaceName.setText("");
                    recommendedPlaceDetails.setText("");
                    recommendedPlaceDescription.setText("");
                    if (exploreNowButton != null) exploreNowButton.setVisibility(View.GONE);
                    if (recommendedPlaceImagePager.getAdapter() != null) {
                        ((ImageSliderAdapter)recommendedPlaceImagePager.getAdapter()).updateImages(new ArrayList<>());
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<HomeDataResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed: " + t.getMessage(), t); // Log the full stack trace
                Toast.makeText(HomeActivity.this, "Network Error. Please check connection.", Toast.LENGTH_LONG).show();
                // Consider showing an error state in the UI
                updatePopularPlacesUI(new ArrayList<>());
                recommendedPlace = null;
                if (recommendedPlaceCard != null) recommendedPlaceCard.setVisibility(View.GONE);
                recommendedPlaceName.setText("Error Loading");
                recommendedPlaceDetails.setText("");
                recommendedPlaceDescription.setText("Could not connect to server.");
                if (exploreNowButton != null) exploreNowButton.setVisibility(View.GONE);
                if (recommendedPlaceImagePager.getAdapter() != null) {
                    ((ImageSliderAdapter)recommendedPlaceImagePager.getAdapter()).updateImages(new ArrayList<>());
                }
            }
        });
    }

    @Override
    public void onFiltersApplied(FilterBottomSheetFragment.FilterOptions options) {
        // Update title based on filter
        if ("nearby".equals(options.filterType)) {
            popularThisMonthTitle.setText("Places Nearby");
            checkLocationPermissionAndFetch();
        } else if ("monsoon".equals(options.filterType)) {
            popularThisMonthTitle.setText("Monsoon Destinations");
            fetchHomeData(options.filterType, options.month, null, null);
        } else if (options.month != null && !options.month.isEmpty()){
            popularThisMonthTitle.setText("Popular in " + options.month);
            fetchHomeData(options.filterType, options.month, null, null);
        } else {
            popularThisMonthTitle.setText("Popular Places"); // Default title
            fetchHomeData(options.filterType, options.month, null, null);
        }
    }


    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchNearbyPlaces();
        } else {
            // Consider showing rationale before requesting if needed
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchNearbyPlaces() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission needed for nearby places.", Toast.LENGTH_SHORT).show();
            return; // Exit if permission somehow isn't granted here
        }
        showLoading(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Location can be null if location is turned off or not available
                    if (location != null) {
                        fetchHomeData("nearby", null, location.getLatitude(), location.getLongitude());
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Could not get your location. Please ensure location services are enabled.", Toast.LENGTH_LONG).show();
                        // Reset title if location fails
                        popularThisMonthTitle.setText("Popular Places");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Failed to get location.", e);
                    Toast.makeText(this, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show();
                    popularThisMonthTitle.setText("Popular Places");
                });
    }

    private void showLoading(boolean isLoading) {
        if(photoLoadingLayout != null) {
            photoLoadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        // Handle spinner animation if you have one
        /*
        if(isLoading && photoLoadingSpinner != null) {
             photoLoadingSpinner.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_loading));
        } else if (photoLoadingSpinner != null){
             photoLoadingSpinner.clearAnimation();
        }
        */
    }


    private void updateRecommendedPlaceUI(Place place) {
        this.recommendedPlace = place; // Store the current recommended place

        // Check if views are null before setting text/visibility
        if (recommendedPlaceName != null) {
            recommendedPlaceName.setText(place.getName() + ", " + place.getLocation());
        }
        if (recommendedPlaceDetails != null) {
            recommendedPlaceDetails.setText("Best Time: " + (place.getSuitableMonths() != null ? place.getSuitableMonths() : "N/A"));
        }
        if (recommendedPlaceDescription != null) {
            // Assuming you add a description field to your Place model/API response
            // recommendedPlaceDescription.setText(place.getDescription());
            recommendedPlaceDescription.setText("A wonderful destination to explore."); // Placeholder
        }

        // Setup image slider
        if (recommendedPlaceImagePager != null && place.getImages() != null && !place.getImages().isEmpty()) {
            ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(HomeActivity.this, place.getImages());
            recommendedPlaceImagePager.setAdapter(sliderAdapter);
            recommendedPlaceImagePager.setVisibility(View.VISIBLE);
        } else if (recommendedPlaceImagePager != null) {
            recommendedPlaceImagePager.setVisibility(View.GONE); // Hide pager if no images
        }
    }


    private void updatePopularPlacesUI(List<Place> popularPlaces) {
        if (popularPlaces == null || popularPlaces.isEmpty()) {
            if (popularThisMonthTitle != null) {
                // Keep the title relevant to the filter applied
                // popularThisMonthTitle.setText("No Places Found");
            }
            if (popularPlacesRecyclerView != null) popularPlacesRecyclerView.setVisibility(View.GONE);
        } else {
            // Title is already set in onFiltersApplied or default
            if (popularPlacesRecyclerView != null) popularPlacesRecyclerView.setVisibility(View.VISIBLE);
        }
        popularPlacesList.clear();
        if(popularPlaces != null) {
            popularPlacesList.addAll(popularPlaces);
        }
        // Ensure adapter exists before notifying
        if (popularPlacesAdapter != null) {
            popularPlacesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPlaceClick(Place place) {
        Intent intent = new Intent(HomeActivity.this, UviewdetailsActivity.class);
        intent.putExtra("PLACE_ID", place.getId());
        startActivity(intent);
        // Don't override transition here, let default apply
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoading(false); // Ensure loading overlay is hidden on resume
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Keep Home selected
    }

    // Simplified navigation helper
    private void launchActivity(Class<?> activityClass) {
        Intent intent = new Intent(HomeActivity.this, activityClass);
        startActivity(intent);
        // Don't finish HomeActivity, allow user to navigate back
        // No transition override unless specifically desired
    }

    // Helper for bottom nav where we DO want to finish the current activity
    private void launchActivityAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(HomeActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0); // No animation for tab switching
        finish(); // Close HomeActivity
    }
}