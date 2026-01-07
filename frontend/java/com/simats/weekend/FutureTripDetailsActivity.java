package com.simats.weekend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simats.weekend.adapters.ItineraryDetailsAdapter;
import com.simats.weekend.models.ItineraryDayDetails;
import com.simats.weekend.models.ItinerarySpotDetails;
import com.simats.weekend.models.SingleTripResponse;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.Trip;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FutureTripDetailsActivity extends AppCompatActivity {

    private static final String TAG = "FutureTripDetails";
    private int tripId;
    private Trip trip;

    // Views
    private ProgressBar progressBar;
    private NestedScrollView contentLayout;
    private ImageView tripImage;
    private TextView tripTitle, tripDates, tripPeopleDays;
    private TextView budgetTransport, budgetFood, budgetHotel, budgetOther, budgetTotal;
    private Button letsBeginButton, cancelTripButton, captureMomentsButton;
    private CollapsingToolbarLayout collapsingToolbar;
    private RecyclerView itineraryRecyclerView;
    private ItineraryDetailsAdapter itineraryAdapter;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<Uri> recordVideoLauncher;
    private Uri currentMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future_trip_details);

        if (getIntent().hasExtra("TRIP_DETAILS")) {
            trip = (Trip) getIntent().getSerializableExtra("TRIP_DETAILS");
            tripId = trip.getId();
        } else if (getIntent().hasExtra("TRIP_ID")) {
            tripId = getIntent().getIntExtra("TRIP_ID", -1);
        }

        if (tripId == -1) {
            Toast.makeText(this, "Error: Trip data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        findViews();
        initializeLaunchers();
        setupClickListeners();
        setupItineraryRecyclerView();

        if (trip != null) {
            populateUI();
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
            fetchTripDetails();
        }
    }

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
            if (cameraGranted) {
                showMediaChoiceDialog();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                Toast.makeText(this, "Image saved to MyMemories/" + trip.getMediaFolder(), Toast.LENGTH_LONG).show();
                galleryAddPic(currentMediaUri);
            } else {
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            }
        });

        recordVideoLauncher = registerForActivityResult(new ActivityResultContracts.CaptureVideo(), success -> {
            if (success) {
                Toast.makeText(this, "Video saved to MyMemories/" + trip.getMediaFolder(), Toast.LENGTH_LONG).show();
                galleryAddPic(currentMediaUri);
            } else {
                Toast.makeText(this, "Failed to save video.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        cancelTripButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Cancel Trip")
                .setMessage("Are you sure you want to cancel this trip?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> updateTripStatusOnServer("cancelled"))
                .setNegativeButton("No", null)
                .show());

        letsBeginButton.setOnClickListener(view -> updateTripStatusOnServer("active"));

        captureMomentsButton.setOnClickListener(view -> {
            if (trip.getMediaFolder() == null || trip.getMediaFolder().isEmpty()) {
                showCreateFolderDialog();
            } else {
                checkPermissionsAndShowMediaDialog();
            }
        });
    }

    private void updateButtonVisibility() {
        if (trip == null || trip.getStatus() == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();

        Date tripStartDate = parseDate(trip.getStartDate());
        boolean isTripDateTodayOrPast = tripStartDate != null && !tripStartDate.after(today);

        switch (trip.getStatus().toLowerCase()) {
            case "future":
                if (isTripDateTodayOrPast) {
                    letsBeginButton.setVisibility(View.VISIBLE);
                    cancelTripButton.setVisibility(View.VISIBLE);
                } else {
                    letsBeginButton.setVisibility(View.GONE);
                    cancelTripButton.setVisibility(View.VISIBLE);
                }
                captureMomentsButton.setVisibility(View.GONE);
                break;
            case "active":
                letsBeginButton.setVisibility(View.GONE);
                cancelTripButton.setVisibility(View.GONE);
                captureMomentsButton.setVisibility(View.VISIBLE);
                break;
            default:
                letsBeginButton.setVisibility(View.GONE);
                cancelTripButton.setVisibility(View.GONE);
                captureMomentsButton.setVisibility(View.GONE);
                break;
        }
    }

    private void checkPermissionsAndShowMediaDialog() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showMediaChoiceDialog();
        } else {
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void showMediaChoiceDialog() {
        final CharSequence[] options = {"Take Photo", "Record Video", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Capture a Moment");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                openCamera(false);
            } else if (options[item].equals("Record Video")) {
                openCamera(true);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openCamera(boolean isVideo) {
        try {
            File mediaFile = isVideo ? createVideoFile() : createImageFile();
            currentMediaUri = FileProvider.getUriForFile(this, "com.example.weekend.fileprovider", mediaFile);

            if (isVideo) {
                recordVideoLauncher.launch(currentMediaUri);
            } else {
                takePictureLauncher.launch(currentMediaUri);
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error creating media file", ex);
            Toast.makeText(this, "Error: Could not create media file.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File tripAlbumDir = getTripAlbumDirectory();
        return File.createTempFile(videoFileName, ".mp4", tripAlbumDir);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File tripAlbumDir = getTripAlbumDirectory();
        return File.createTempFile(imageFileName, ".jpg", tripAlbumDir);
    }

    private File getTripAlbumDirectory() throws IOException {
        File appSpecificStorageDir = getExternalFilesDir(null);
        if (appSpecificStorageDir == null) {
            throw new IOException("App-specific storage is not available.");
        }
        File memoriesDir = new File(appSpecificStorageDir, "MyMemories");
        File tripAlbumDir = new File(memoriesDir, trip.getMediaFolder());

        if (!tripAlbumDir.exists()) {
            if (!tripAlbumDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory structure: " + tripAlbumDir.getAbsolutePath());
                throw new IOException("Failed to create memory directories.");
            }
        }
        return tripAlbumDir;
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void findViews() {
        progressBar = findViewById(R.id.progress_bar_details);
        contentLayout = findViewById(R.id.content_layout_details);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        tripImage = findViewById(R.id.trip_image_detail);
        tripTitle = findViewById(R.id.trip_title_detail);
        tripDates = findViewById(R.id.trip_dates_detail);
        tripPeopleDays = findViewById(R.id.trip_people_days_detail);
        budgetTransport = findViewById(R.id.budget_transport);
        budgetFood = findViewById(R.id.budget_food);
        budgetHotel = findViewById(R.id.budget_hotel);
        budgetOther = findViewById(R.id.budget_other);
        budgetTotal = findViewById(R.id.budget_total);
        letsBeginButton = findViewById(R.id.lets_begin_button);
        cancelTripButton = findViewById(R.id.cancel_trip_button);
        captureMomentsButton = findViewById(R.id.capture_moments_button);
        itineraryRecyclerView = findViewById(R.id.recycler_view_itinerary);
    }

    private void setupItineraryRecyclerView() {
        itineraryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchTripDetails() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<SingleTripResponse> call = apiService.getTripDetails(tripId);
        call.enqueue(new Callback<SingleTripResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleTripResponse> call, @NonNull Response<SingleTripResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    trip = response.body().getData();
                    if (trip != null) {
                        populateUI();
                        contentLayout.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(FutureTripDetailsActivity.this, "Could not find trip details.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(FutureTripDetailsActivity.this, "Failed to load trip details.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(@NonNull Call<SingleTripResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FutureTripDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateUI() {
        collapsingToolbar.setTitle(trip.getPlaceName());
        tripTitle.setText(trip.getPlaceName() + ", " + trip.getPlaceLocation());
        String dateRange = formatDate(trip.getStartDate()) + " - " + formatDate(trip.getEndDate());
        tripDates.setText(dateRange);
        String peopleDaysText = String.format(Locale.US, "%d People, %d Days", trip.getNumPeople(), trip.getNumDays());
        tripPeopleDays.setText(peopleDaysText);

        if (trip.getPlaceImage() != null && !trip.getPlaceImage().isEmpty()) {
            String imageUrl = RetrofitClient.BASE_URL + trip.getPlaceImage();
            Glide.with(this).load(imageUrl).placeholder(R.drawable.placeholder_image).error(R.drawable.image_error).into(tripImage);
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        budgetTransport.setText(currencyFormat.format(trip.getTransportCost()));
        budgetFood.setText(currencyFormat.format(trip.getFoodCost()));
        budgetHotel.setText(currencyFormat.format(trip.getHotelCost()));
        budgetOther.setText(currencyFormat.format(trip.getOtherCost()));
        budgetTotal.setText(currencyFormat.format(trip.getTotalBudget()));
        processAndDisplayItinerary();
        updateButtonVisibility();
    }

    private void processAndDisplayItinerary() {
        if (trip.getItineraryDetails() == null || trip.getItineraryDetails().isEmpty()) return;
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
        itineraryRecyclerView.setAdapter(itineraryAdapter);
    }

    private void updateTripStatusOnServer(String newStatus) {
        final android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage(newStatus.equals("active") ? "Starting trip..." : "Cancelling trip...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        letsBeginButton.setEnabled(false);
        cancelTripButton.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.updateTripStatus(tripId, newStatus);

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    String successMessage = newStatus.equals("active") ? "Trip started!" : "Trip cancelled.";
                    Toast.makeText(FutureTripDetailsActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                    // --- FIX: Logic when status changes ---
                    if (newStatus.equals("active")) {
                        trip.setStatus("active"); // Update the local trip object
                        updateButtonVisibility(); // Show "Capture Moments", hide others
                        setResult(RESULT_OK);     // Tell the fragment to refresh
                        // REMOVED finish(); - Stay on this screen
                    } else { // Handle cancelled (or any other status update)
                        setResult(RESULT_OK); // Tell the fragment to refresh
                        finish();             // Go back to the list
                    }
                    // --- END OF FIX ---

                } else {
                    Toast.makeText(FutureTripDetailsActivity.this, "Failed to update trip.", Toast.LENGTH_SHORT).show();
                    // Re-enable buttons on failure
                    letsBeginButton.setEnabled(true);
                    cancelTripButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(FutureTripDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Re-enable buttons on failure
                letsBeginButton.setEnabled(true);
                cancelTripButton.setEnabled(true);
            }
        });
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a Photo Album");
        builder.setMessage("Enter a name for this trip's photo album. All captured moments will be saved in 'My Memories'.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(trip.getPlaceName().replaceAll("[^a-zA-Z0-9]+", "") + "_" + trip.getId());
        builder.setView(input);
        builder.setPositiveButton("Create & Open Camera", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (folderName.isEmpty()) {
                Toast.makeText(this, "Folder name cannot be empty.", Toast.LENGTH_SHORT).show();
            } else {
                saveFolderNameToDatabase(folderName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveFolderNameToDatabase(String folderName) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.saveMediaFolder(tripId, folderName);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    trip.setMediaFolder(folderName);
                    Toast.makeText(FutureTripDetailsActivity.this, "Album created!", Toast.LENGTH_SHORT).show();
                    checkPermissionsAndShowMediaDialog();
                } else {
                    Toast.makeText(FutureTripDetailsActivity.this, "Error creating album on server.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                Toast.makeText(FutureTripDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void galleryAddPic(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat destFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date date = sourceFormat.parse(dateString);
            return destFormat.format(date);
        } catch (ParseException e) { return dateString; }
    }

    private Date parseDate(String dateString) {
        if (dateString == null) return null;
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return sourceFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // --- FIX: Handle back press ---
            // If the trip became active, we need to signal the list to refresh
            if ("active".equalsIgnoreCase(trip.getStatus())) {
                setResult(RESULT_OK);
            }
            finish(); // Now finish regardless
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- NEW: Handle back press explicitly ---
    @Override
    public void onBackPressed() {
        // If the trip became active, we need to signal the list to refresh
        if (trip != null && "active".equalsIgnoreCase(trip.getStatus())) {
            setResult(RESULT_OK);
        }
        super.onBackPressed(); // Perform the default back action
    }
}