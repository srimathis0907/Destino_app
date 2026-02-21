package com.simats.weekend;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.adapters.AvailableSpotsAdapter;
import com.simats.weekend.adapters.ItineraryDaysAdapter;
import com.simats.weekend.fragments.NearbyPickerBottomSheet;
import com.simats.weekend.models.ItineraryDay;
import com.simats.weekend.models.ItinerarySpot;
import com.simats.weekend.models.NearbyPlace;
import com.simats.weekend.models.PlaceDetails;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.TopSpot;
import com.simats.weekend.models.TripDataPayload;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstimateBudgetActivity extends AppCompatActivity
        implements ItineraryDaysAdapter.OnItemInteractionListener, NearbyPickerBottomSheet.OnNearbyItemSelectedListener {

    private int peopleCount = 1;
    private long dayCount = 1;
    private PlaceDetails placeDetails;
    private TextView tvPeopleCount, tvTotalBudget, tvTransportCost, tvHotelCost, tvFoodCost;
    private EditText etFromDate, etToDate;
    private String formattedStartDate = "";
    private String formattedEndDate = "";
    private RecyclerView rvAvailableSpots, rvItineraryDays;
    private AvailableSpotsAdapter spotsAdapter;
    private ItineraryDaysAdapter daysAdapter;
    private List<TopSpot> availableSpotsList = new ArrayList<>();
    private List<ItineraryDay> itineraryDayList = new ArrayList<>();
    private ItinerarySpot currentSpotForSelection;
    private RadioGroup rgHotelPreference, rgFoodTier, rgFoodPref;
    private SwitchMaterial switchReturnTrip, switchMedical;
    private TextInputEditText etOtherExpenses, etManualTransportCost;
    private MaterialButton btnCalculate, btnSaveTrip, btnFindTransportWebview;
    private CardView resultsCard;
    private LinearLayout totalAndSaveBar;

    private FrameLayout webViewOverlay;
    private WebView webView;
    private FloatingActionButton btnCloseWebview;

    private boolean isBudgetCalculated = false;
    private NumberFormat currencyFormat;

    private double finalTransportCost = 0;
    private double finalHotelCost = 0;
    private double finalFoodCost = 0;
    private double finalOtherCost = 0;

    @Override
    public void onItineraryChanged() {
        markBudgetAsDirty();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimate_budget);

        if (!loadIntentData()) return;

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        setupToolbar();
        findViews();
        setupInitialState();
        setupListeners();
        setupPlanner();
        setupWebView();
        updateItineraryDays(false);
    }

    private void initializeDefaultDates() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        formattedStartDate = serverFormat.format(calendar.getTime());
        formattedEndDate = serverFormat.format(calendar.getTime());

        etFromDate.setText(displayFormat.format(calendar.getTime()));
        etToDate.setText(displayFormat.format(calendar.getTime()));
    }

    private boolean loadIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PLACE_DETAILS")) {
            placeDetails = (PlaceDetails) intent.getSerializableExtra("PLACE_DETAILS");
            return true;
        }
        Toast.makeText(this, "Error: Place details not found.", Toast.LENGTH_LONG).show();
        finish();
        return false;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_budget);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void findViews() {
        View peopleCounterView = findViewById(R.id.counter_people);
        ((TextView) peopleCounterView.findViewById(R.id.counter_title)).setText("How many people?");
        tvPeopleCount = peopleCounterView.findViewById(R.id.tv_count);
        etFromDate = findViewById(R.id.et_from_date);
        etToDate = findViewById(R.id.et_to_date);
        rvAvailableSpots = findViewById(R.id.recycler_view_available_spots);
        rvItineraryDays = findViewById(R.id.recycler_view_itinerary_days);
        tvTotalBudget = findViewById(R.id.tv_total_budget);
        tvTransportCost = findViewById(R.id.tv_transport_cost);
        tvHotelCost = findViewById(R.id.tv_hotel_cost);
        tvFoodCost = findViewById(R.id.tv_food_cost);
        rgHotelPreference = findViewById(R.id.rg_hotel_preference);
        rgFoodTier = findViewById(R.id.rg_food_tier);
        rgFoodPref = findViewById(R.id.rg_food_pref);
        switchReturnTrip = findViewById(R.id.switch_return_trip);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnSaveTrip = findViewById(R.id.btn_save_trip);
        resultsCard = findViewById(R.id.results_card);
        totalAndSaveBar = findViewById(R.id.total_and_save_bar);
        switchMedical = findViewById(R.id.switch_medical);
        etOtherExpenses = findViewById(R.id.et_other_expenses);
        btnFindTransportWebview = findViewById(R.id.btn_find_transport_webview);
        etManualTransportCost = findViewById(R.id.et_manual_transport_cost);
        webViewOverlay = findViewById(R.id.webview_overlay);
        webView = findViewById(R.id.webview);
        btnCloseWebview = findViewById(R.id.btn_close_webview);
    }

    private void setupInitialState() {
        tvPeopleCount.setText(String.valueOf(peopleCount));
        resultsCard.setVisibility(View.GONE);
        totalAndSaveBar.setVisibility(View.GONE);
        initializeDefaultDates();
    }

    private void setupListeners() {
        View peopleCounterView = findViewById(R.id.counter_people);
        ImageButton btnMinusPeople = peopleCounterView.findViewById(R.id.btn_minus);
        ImageButton btnPlusPeople = peopleCounterView.findViewById(R.id.btn_plus);
        btnMinusPeople.setOnClickListener(v -> { if (peopleCount > 1) { peopleCount--; tvPeopleCount.setText(String.valueOf(peopleCount)); markBudgetAsDirty();} });
        btnPlusPeople.setOnClickListener(v -> { peopleCount++; tvPeopleCount.setText(String.valueOf(peopleCount)); markBudgetAsDirty();});

        etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        etToDate.setOnClickListener(v -> showDatePickerDialog(false));

        rgHotelPreference.setOnCheckedChangeListener((group, checkedId) -> markBudgetAsDirty());
        rgFoodTier.setOnCheckedChangeListener((group, checkedId) -> markBudgetAsDirty());
        rgFoodPref.setOnCheckedChangeListener((group, checkedId) -> markBudgetAsDirty());
        switchReturnTrip.setOnCheckedChangeListener((buttonView, isChecked) -> markBudgetAsDirty());
        switchMedical.setOnCheckedChangeListener((buttonView, isChecked) -> markBudgetAsDirty());
        etOtherExpenses.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { markBudgetAsDirty(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        etManualTransportCost.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { markBudgetAsDirty(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFindTransportWebview.setOnClickListener(v -> showTransportChoiceDialog());
        btnCloseWebview.setOnClickListener(v -> showPriceEntryDialog());

        btnCalculate.setOnClickListener(v -> calculateBudget());
        btnSaveTrip.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

            if (isStartDate) {
                formattedStartDate = serverFormat.format(calendar.getTime());
                etFromDate.setText(displayFormat.format(calendar.getTime()));
                if (etToDate.getText().toString().isEmpty()) {
                    etToDate.setText(displayFormat.format(calendar.getTime()));
                    formattedEndDate = serverFormat.format(calendar.getTime());
                }
            } else {
                formattedEndDate = serverFormat.format(calendar.getTime());
                etToDate.setText(displayFormat.format(calendar.getTime()));
            }
            updateItineraryDays(true);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateItineraryDays(boolean markDirty) {
        if (formattedStartDate.isEmpty() || formattedEndDate.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date fromDate = sdf.parse(formattedStartDate);
            Date toDate = sdf.parse(formattedEndDate);

            if (toDate.before(fromDate)) {
                Toast.makeText(this, "'To' date cannot be before 'From' date.", Toast.LENGTH_SHORT).show();
                return;
            }

            long diffInMillis = toDate.getTime() - fromDate.getTime();
            dayCount = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1;

            itineraryDayList.clear();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromDate);

            SimpleDateFormat dayFormat = new SimpleDateFormat("MMM dd", Locale.US);
            for (int i = 0; i < dayCount; i++) {
                String label = String.format("Day %d (%s)", i + 1, dayFormat.format(calendar.getTime()));
                itineraryDayList.add(new ItineraryDay(label));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            daysAdapter.notifyDataSetChanged();
            if (markDirty) {
                markBudgetAsDirty();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setupPlanner() {
        rvAvailableSpots.setLayoutManager(new LinearLayoutManager(this));
        if (placeDetails.getTopSpots() != null) {
            availableSpotsList.addAll(placeDetails.getTopSpots());
        }
        spotsAdapter = new AvailableSpotsAdapter(availableSpotsList);
        rvAvailableSpots.setAdapter(spotsAdapter);

        rvItineraryDays.setLayoutManager(new LinearLayoutManager(this));
        daysAdapter = new ItineraryDaysAdapter(itineraryDayList, availableSpotsList, this);
        rvItineraryDays.setAdapter(daysAdapter);
    }

    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
    }

    private void markBudgetAsDirty() {
        if (isBudgetCalculated) {
            btnCalculate.setText("Recalculate");
            btnCalculate.setIconResource(R.drawable.ic_refresh);
        } else {
            btnCalculate.setText("Calculate Budget");
            btnCalculate.setIconResource(R.drawable.ic_calculatebudget);
        }
        resultsCard.setVisibility(View.GONE);
        totalAndSaveBar.setVisibility(View.GONE);
        isBudgetCalculated = false;
    }

    private void calculateBudget() {
        if (placeDetails == null || dayCount <= 0) {
            Toast.makeText(this, "Please select valid dates first.", Toast.LENGTH_SHORT).show();
            return;
        }

        double transportCostPerPerson = 0.0;
        try {
            if (!TextUtils.isEmpty(etManualTransportCost.getText())) {
                transportCostPerPerson = Double.parseDouble(etManualTransportCost.getText().toString());
            }
        } catch (NumberFormatException e) { /* ignore */ }
        finalTransportCost = transportCostPerPerson * peopleCount;
        if(switchReturnTrip.isChecked()){
            finalTransportCost *= 2;
        }

        int hotelTier = 1;
        if (rgHotelPreference.getCheckedRadioButtonId() == R.id.rb_hotel_luxury) hotelTier = 0;
        else if (rgHotelPreference.getCheckedRadioButtonId() == R.id.rb_hotel_budget) hotelTier = 2;

        double baseHotelCost = placeDetails.getHotelCost(hotelTier);
        if (baseHotelCost == 0 && dayCount > 1) {
            Toast.makeText(this, "Note: Hotel cost for this tier is 0 in the database.", Toast.LENGTH_LONG).show();
        }
        int hotelNights = (dayCount > 1) ? (int)dayCount - 1 : 0;
        finalHotelCost = baseHotelCost * Math.ceil(peopleCount / 2.0) * hotelNights;

        int foodTier = 1;
        if (rgFoodTier.getCheckedRadioButtonId() == R.id.rb_food_luxury) foodTier = 0;
        else if (rgFoodTier.getCheckedRadioButtonId() == R.id.rb_food_budget) foodTier = 2;

        int foodPrefIndex = 0;
        if (rgFoodPref.getCheckedRadioButtonId() == R.id.rb_nonveg) foodPrefIndex = 1;
        else if (rgFoodPref.getCheckedRadioButtonId() == R.id.rb_both) foodPrefIndex = 2;
        finalFoodCost = placeDetails.getFoodCost(foodTier, foodPrefIndex) * peopleCount * dayCount;

        double medicalCost = switchMedical.isChecked() ? 1500.0 : 0.0;
        double otherExpensesValue = 0.0;
        try {
            if (!TextUtils.isEmpty(etOtherExpenses.getText())) {
                otherExpensesValue = Double.parseDouble(etOtherExpenses.getText().toString());
            }
        } catch (NumberFormatException e) { /* ignore */ }
        finalOtherCost = medicalCost + otherExpensesValue;

        double finalTotalBudget = finalTransportCost + finalHotelCost + finalFoodCost + finalOtherCost;

        tvTransportCost.setText(currencyFormat.format(finalTransportCost));
        tvHotelCost.setText(currencyFormat.format(finalHotelCost));
        tvFoodCost.setText(currencyFormat.format(finalFoodCost));
        tvTotalBudget.setText(currencyFormat.format(finalTotalBudget));

        isBudgetCalculated = true;
        btnCalculate.setText("Recalculate");
        btnCalculate.setIconResource(R.drawable.ic_refresh);

        if (resultsCard.getVisibility() == View.GONE) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            resultsCard.startAnimation(slideUp);
            totalAndSaveBar.startAnimation(slideUp);
        }
        resultsCard.setVisibility(View.VISIBLE);
        totalAndSaveBar.setVisibility(View.VISIBLE);
    }

    private void showTransportChoiceDialog() {
        final String[] transportOptions = {"Flights", "Trains", "Buses"};
        new AlertDialog.Builder(this)
                .setTitle("Find Prices On...")
                .setItems(transportOptions, (dialog, which) -> openTransportWebView(transportOptions[which].toLowerCase()))
                .show();
    }

    private void openTransportWebView(String mode) {
        String url = "";
        switch (mode) {
            case "flights":
                url = "https://www.makemytrip.com/flights/";
                break;
            case "trains":
                url = "https://www.irctc.co.in/nget/train-search";
                break;
            case "buses":
                url = "https://www.redbus.in/";
                break;
        }
        if (!url.isEmpty()) {
            webView.loadUrl(url);
            webViewOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void showPriceEntryDialog() {
        webViewOverlay.setVisibility(View.GONE);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_price_entry, null);
        final EditText input = view.findViewById(R.id.et_price_input);

        new AlertDialog.Builder(this)
                .setTitle("Set Transport Cost")
                .setView(view)
                .setPositiveButton("Set", (dialog, which) -> {
                    String price = input.getText().toString();
                    if (!TextUtils.isEmpty(price)) {
                        etManualTransportCost.setText(price);
                        markBudgetAsDirty();
                        calculateBudget();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void showConfirmationDialog() {
        if (!isBudgetCalculated) {
            Toast.makeText(this, "Please calculate budget first.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("TRIP TO: ").append(placeDetails.getName().toUpperCase()).append("\n\n");
        summary.append("Dates: ").append(etFromDate.getText()).append(" to ").append(etToDate.getText()).append("\n");
        summary.append("Passengers: ").append(peopleCount).append("\n\n");

        summary.append("ITINERARY:\n");
        for (ItineraryDay day : itineraryDayList) {
            summary.append("• ").append(day.getDateLabel()).append("\n");
            if (day.getPlannedSpots().isEmpty()) {
                summary.append("  - Rest Day\n");
            } else {
                for (ItinerarySpot spot : day.getPlannedSpots()) {
                    summary.append("  - Visit: ").append(spot.getTopSpot().getName()).append("\n");
                    if (spot.getSelectedNearbyPlaces() != null && !spot.getSelectedNearbyPlaces().isEmpty()) {
                        for (NearbyPlace nearby : spot.getSelectedNearbyPlaces()) {
                            summary.append("    - Add-on: ").append(nearby.getName()).append("\n");
                        }
                    }
                }
            }
        }

        summary.append("\nPREFERENCES:\n");
        summary.append("- Hotel: ").append(getSelectedRadioButtonText(rgHotelPreference)).append("\n");
        summary.append("- Food: ").append(getSelectedRadioButtonText(rgFoodTier))
                .append(" (").append(getSelectedRadioButtonText(rgFoodPref)).append(")\n");
        summary.append("- Return Trip Included: ").append(switchReturnTrip.isChecked() ? "Yes" : "No").append("\n");

        summary.append("\nADDITIONAL EXPENSES:\n");
        summary.append("- Medical Buffer: ").append(switchMedical.isChecked() ? "Yes" : "No").append("\n");
        if (!TextUtils.isEmpty(etOtherExpenses.getText()) && !etOtherExpenses.getText().toString().equals("0")) {
            summary.append("- Other: ").append(currencyFormat.format(finalOtherCost)).append("\n");
        }

        summary.append("\n---------------------\n");
        summary.append("TOTAL ESTIMATED BUDGET:\n");
        summary.append(tvTotalBudget.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Confirm Your Trip")
                .setMessage(summary.toString())
                .setPositiveButton("Save", (dialog, which) -> saveTrip())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Edit", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getSelectedRadioButtonText(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            return ((android.widget.RadioButton) findViewById(selectedId)).getText().toString();
        }
        return "N/A";
    }

    private void saveTrip() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving your trip...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SessionManager sessionManager = new SessionManager(getApplicationContext());

        // *** FIX ADDED HERE: VALIDATE THE USER ID ***
        // We now check if the user is logged in AND if we have a valid user ID (> 0)
        int userId = sessionManager.getUserId();
        if (!sessionManager.isLoggedIn() || userId == -1) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: User session is invalid. Please log out and log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        double totalBudget = finalTransportCost + finalHotelCost + finalFoodCost + finalOtherCost;

        // Create the new payload object with all the data
        TripDataPayload payload = new TripDataPayload(
                userId,
                placeDetails.getId(),
                placeDetails.getName(),
                placeDetails.getLocation(),
                formattedStartDate,
                formattedEndDate,
                peopleCount,
                (int) dayCount,
                finalTransportCost,
                finalFoodCost,
                finalHotelCost,
                finalOtherCost,
                totalBudget,
                itineraryDayList
        );

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.addTrip(payload); // Send the payload object

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(getApplicationContext(), "Trip saved successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), TripActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = (response.body() != null) ? response.body().getMessage() : "Failed to save trip. Server error.";
                    Toast.makeText(getApplicationContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSpotRemoved(ItineraryDay day, ItinerarySpot spot) {
        day.getPlannedSpots().remove(spot);
        daysAdapter.notifyDataSetChanged();
        markBudgetAsDirty();
    }

    @Override
    public void onFindNearbyClicked(ItinerarySpot spot) {
        if (spot.getTopSpot().getLatitude() == 0.0 || spot.getTopSpot().getLongitude() == 0.0) {
            Toast.makeText(this, "Location data not available for this spot. Update place data in admin panel.", Toast.LENGTH_LONG).show();
            return;
        }

        currentSpotForSelection = spot;

        final String[] categories = {"Hotels", "Restaurants", "Things to Do"};
        final String[] categorySlugs = {"hotel", "restaurant", "attraction"};

        new AlertDialog.Builder(this)
                .setTitle("Find Nearby...")
                .setItems(categories, (dialog, which) -> {
                    String selectedType = categorySlugs[which];
                    NearbyPickerBottomSheet bottomSheet = NearbyPickerBottomSheet.newInstance(
                            spot.getTopSpot().getLatitude(),
                            spot.getTopSpot().getLongitude(),
                            selectedType
                    );
                    bottomSheet.show(getSupportFragmentManager(), "NearbyPickerBottomSheet");
                })
                .show();
    }

    @Override
    public void onNearbyItemSelected(NearbyPlace selectedPlace) {
        if (currentSpotForSelection != null) {
            currentSpotForSelection.addSelectedNearbyPlace(selectedPlace);
            daysAdapter.notifyDataSetChanged();
            currentSpotForSelection = null;
            markBudgetAsDirty();
        }
    }

    @Override
    public void onBackPressed() {
        if (webViewOverlay.getVisibility() == View.VISIBLE) {
            webViewOverlay.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}