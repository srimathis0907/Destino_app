package com.simats.weekend;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.simats.weekend.adapters.ItineraryDetailsAdapter;
import com.simats.weekend.databinding.ActivityAdminTripDetailsBinding;
import com.simats.weekend.models.ItineraryDayDetails;
import com.simats.weekend.models.ItinerarySpotDetails;
import com.simats.weekend.models.SingleTripResponse;
import com.simats.weekend.models.Trip;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTripDetailsActivity extends AppCompatActivity {

    private ActivityAdminTripDetailsBinding binding;
    private int tripId;
    private String userName;
    private Trip trip;
    private ItineraryDetailsAdapter itineraryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminTripDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get tripId from the AdminTrip object
        if (getIntent().hasExtra("TRIP_DETAILS")) {
            com.simats.weekend.models.AdminTrip adminTrip = (com.simats.weekend.models.AdminTrip) getIntent().getSerializableExtra("TRIP_DETAILS");
            tripId = adminTrip.tripId;
            userName = adminTrip.userName;
        } else {
            Toast.makeText(this, "Error: Trip data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        binding.progressBarDetails.setVisibility(View.VISIBLE);
        binding.contentLayoutDetails.setVisibility(View.GONE);
        fetchTripDetails();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void fetchTripDetails() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<SingleTripResponse> call = apiService.getTripDetails(tripId);
        call.enqueue(new Callback<SingleTripResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleTripResponse> call, @NonNull Response<SingleTripResponse> response) {
                binding.progressBarDetails.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    trip = response.body().getData();
                    if (trip != null) {
                        populateUI();
                        binding.contentLayoutDetails.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(AdminTripDetailsActivity.this, "Could not find trip details.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminTripDetailsActivity.this, "Failed to load trip details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleTripResponse> call, @NonNull Throwable t) {
                binding.progressBarDetails.setVisibility(View.GONE);
                Toast.makeText(AdminTripDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI() {
        // ========== FIXES APPLIED HERE ==========
        binding.collapsingToolbar.setTitle(trip.getPlaceName()); // FIX: Was getTitle()
        binding.tripTitleDetail.setText(trip.getPlaceName() + ", " + trip.getPlaceLocation()); // FIX: Was getTitle(), getLocation()
        binding.tvUserNameLabel.setText("Trip by " + userName);

        String dateRange = formatDate(trip.getStartDate()) + " - " + formatDate(trip.getEndDate()); // FIX: Was getFromDate(), getToDate()
        binding.tripDatesDetail.setText(dateRange);

        String peopleDaysText = String.format(Locale.US, "%d People, %d Days", trip.getNumPeople(), trip.getNumDays());
        binding.tripPeopleDaysDetail.setText(peopleDaysText);

        if (trip.getPlaceImage() != null && !trip.getPlaceImage().isEmpty()) { // FIX: Was getImageUrl()
            // Assuming your images are in an 'uploads' folder relative to the BASE_URL
            String imageUrl = RetrofitClient.BASE_URL + "uploads/" + trip.getPlaceImage();
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.image_error)
                    .into(binding.tripImageDetail);
        }
        // ========== END OF FIXES ==========

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        binding.budgetTransport.setText(currencyFormat.format(trip.getTransportCost()));
        binding.budgetFood.setText(currencyFormat.format(trip.getFoodCost()));
        binding.budgetHotel.setText(currencyFormat.format(trip.getHotelCost()));
        binding.budgetOther.setText(currencyFormat.format(trip.getOtherCost()));
        binding.budgetTotal.setText(currencyFormat.format(trip.getTotalBudget()));

        processAndDisplayItinerary();
    }

    private void processAndDisplayItinerary() {
        if (trip.getItineraryDetails() == null || trip.getItineraryDetails().isEmpty()) {
            binding.itinerarySection.setVisibility(View.GONE);
            return;
        }

        binding.itinerarySection.setVisibility(View.VISIBLE);
        Map<Integer, ItineraryDayDetails> dayMap = new LinkedHashMap<>();
        Map<String, ItinerarySpotDetails> spotMap = new LinkedHashMap<>();

        for (int i = 1; i <= trip.getNumDays(); i++) {
            dayMap.put(i, new ItineraryDayDetails("Day " + i));
        }

        for (Trip.ItineraryItem item : trip.getItineraryDetails()) {
            ItineraryDayDetails currentDay = dayMap.get(item.getDayNumber());
            if (currentDay == null) continue;

            if (item.getParentSpotName() == null) {
                ItinerarySpotDetails spot = new ItinerarySpotDetails(item.getItemName());
                currentDay.addSpot(spot);
                spotMap.put(item.getItemName(), spot);
            } else {
                ItinerarySpotDetails parentSpot = spotMap.get(item.getParentSpotName());
                if (parentSpot != null) parentSpot.addAddon(item.getItemName());
            }
        }

        List<ItineraryDayDetails> processedList = new ArrayList<>(dayMap.values());
        itineraryAdapter = new ItineraryDetailsAdapter(this, processedList);
        binding.recyclerViewItinerary.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewItinerary.setAdapter(itineraryAdapter);
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat destFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date date = sourceFormat.parse(dateString);
            return destFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}