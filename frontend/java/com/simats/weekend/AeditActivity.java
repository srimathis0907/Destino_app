package com.simats.weekend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.simats.weekend.databinding.AeditBinding;
import com.simats.weekend.models.PlaceDetailsResponse;
import com.simats.weekend.models.StatusResponse;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AeditActivity extends AppCompatActivity {

    private AeditBinding binding;
    private int placeId = -1;
    private PlaceDetailsResponse.PlaceDetails originalPlaceDetails; // For the "Reset" functionality

    private final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private final String[] TRANSPORT_TYPES = {"Select Type", "Bus", "Train", "Flight", "Taxi", "Auto", "Metro"};
    private View currentSpotViewForMap = null;

    // --- Image Management Lists ---
    private final List<Uri> newImageUris = new ArrayList<>(); // For newly selected images
    private final List<Integer> imagesToDelete = new ArrayList<>(); // IDs of existing images to delete

    // --- ActivityResultLaunchers ---
    private final ActivityResultLauncher<String[]> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            newImageUris.add(uri);
                            addImageThumbnail(uri, -1); // -1 indicates a new image
                        } catch (SecurityException e) {
                            Log.e("AeditActivity", "Permission error for URI", e);
                            Toast.makeText(this, "Failed to get permission for an image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        double latitude = extras.getDouble("latitude");
                        double longitude = extras.getDouble("longitude");
                        if (currentSpotViewForMap != null) {
                            ((EditText) currentSpotViewForMap.findViewById(R.id.spot_latitude_edit_text)).setText(String.valueOf(latitude));
                            ((EditText) currentSpotViewForMap.findViewById(R.id.spot_longitude_edit_text)).setText(String.valueOf(longitude));
                            currentSpotViewForMap = null;
                        } else {
                            binding.etLatitude.setText(String.valueOf(latitude));
                            binding.etLongitude.setText(String.valueOf(longitude));
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AeditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        placeId = getIntent().getIntExtra("PLACE_ID", -1);
        if (placeId == -1) {
            Toast.makeText(this, "Error: Place ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupToolbar();
        setupButtonClickListeners();
        setupDynamicFeatures();
        populateMonthsGrid();
        fetchPlaceDetails();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarEdit);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarEdit.setNavigationOnClickListener(v -> finish());
    }

    private void showLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void fetchPlaceDetails() {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<PlaceDetailsResponse> call = apiService.getPlaceDetails(placeId);

        call.enqueue(new Callback<PlaceDetailsResponse>() {
            @Override
            public void onResponse(Call<PlaceDetailsResponse> call, Response<PlaceDetailsResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    originalPlaceDetails = response.body().getData(); // Save for 'Reset'
                    populateForm(originalPlaceDetails);
                } else {
                    Toast.makeText(AeditActivity.this, "Failed to load place details.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PlaceDetailsResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AeditActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // START: *** METHOD FULLY UPDATED ***
    private void populateForm(PlaceDetailsResponse.PlaceDetails details) {
        // Clear any previous state
        resetFormState();

        // --- Basic Info ---
        binding.etPlaceName.setText(details.getName());
        binding.etPlaceLocation.setText(details.getLocation());

        // --- Visit Details ---
        binding.etAvgBudget.setText(details.getAvgBudget());
        binding.etLocalLanguage.setText(details.getLocalLanguage());
        binding.switchMonsoon.setChecked(details.isMonsoonDestination() == 1);

        // Add a null check for suitableMonths to prevent crashes
        if (details.getSuitableMonths() != null && !details.getSuitableMonths().isEmpty()) {
            List<String> months = Arrays.asList(details.getSuitableMonths().split(","));
            for (int i = 0; i < binding.monthsGrid.getChildCount(); i++) {
                CheckBox cb = (CheckBox) binding.monthsGrid.getChildAt(i);
                if (months.contains(cb.getText().toString())) {
                    cb.setChecked(true);
                }
            }
        }

        // --- Images (FIX: Added null check) ---
        if (details.getImages() != null) {
            for (PlaceDetailsResponse.PlaceImage image : details.getImages()) {
                addImageThumbnail(null, image.getId(), image.getImageUrl());
            }
        }

        // --- Spots (FIX: Added null check) ---
        if (details.getSpots() != null) {
            for (PlaceDetailsResponse.TopSpot spot : details.getSpots()) {
                addSpotView(spot.getName(), spot.getDescription(), String.valueOf(spot.getLatitude()), String.valueOf(spot.getLongitude()));
            }
        }

        // --- Transport (FIX: Added null check) ---
        if (details.getTransportOptions() != null) {
            for (PlaceDetailsResponse.TransportOption option : details.getTransportOptions()) {
                addTransportView(option.getType(), option.getInfo());
            }
        }

        // --- Costs ---
        binding.etLatitude.setText(String.valueOf(details.getLatitude()));
        binding.etLongitude.setText(String.valueOf(details.getLongitude()));
        binding.etTollCost.setText(String.format(Locale.US, "%.0f", details.getTollCost()));
        binding.etParkingCost.setText(String.format(Locale.US, "%.0f", details.getParkingCost()));
        binding.etHotelCostStandard.setText(String.format(Locale.US, "%.0f", details.getHotelStdCost()));
        binding.etHotelCostHigh.setText(String.format(Locale.US, "%.0f", details.getHotelHighCost()));
        binding.etHotelCostLow.setText(String.format(Locale.US, "%.0f", details.getHotelLowCost()));
        binding.etFoodStdVeg.setText(String.format(Locale.US, "%.0f", details.getFoodStdVeg()));
        binding.etFoodStdNonVeg.setText(String.format(Locale.US, "%.0f", details.getFoodStdNonveg()));
        binding.etFoodStdCombo.setText(String.format(Locale.US, "%.0f", details.getFoodStdCombo()));
        binding.etFoodHighVeg.setText(String.format(Locale.US, "%.0f", details.getFoodHighVeg()));
        binding.etFoodHighNonVeg.setText(String.format(Locale.US, "%.0f", details.getFoodHighNonveg()));
        binding.etFoodHighCombo.setText(String.format(Locale.US, "%.0f", details.getFoodHighCombo()));
        binding.etFoodLowVeg.setText(String.format(Locale.US, "%.0f", details.getFoodLowVeg()));
        binding.etFoodLowNonVeg.setText(String.format(Locale.US, "%.0f", details.getFoodLowNonveg()));
        binding.etFoodLowCombo.setText(String.format(Locale.US, "%.0f", details.getFoodLowCombo()));
    }
    // END: *** METHOD FULLY UPDATED ***

    private void resetFormState() {
        binding.imageContainer.removeAllViews();
        binding.imageContainer.addView(binding.btnAddImage); // Re-add the plus button
        binding.spotsContainer.removeAllViews();
        binding.transportContainer.removeAllViews();
        newImageUris.clear();
        imagesToDelete.clear();
        for (int i = 0; i < binding.monthsGrid.getChildCount(); i++) {
            ((CheckBox) binding.monthsGrid.getChildAt(i)).setChecked(false);
        }
    }


    private void setupButtonClickListeners() {
        binding.btnSaveChanges.setOnClickListener(v -> updatePlaceData());
        binding.btnResetForm.setOnClickListener(v -> {
            if (originalPlaceDetails != null) {
                populateForm(originalPlaceDetails);
                Toast.makeText(this, "Form has been reset to its original state.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDynamicFeatures() {
        binding.btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        binding.btnAddSpot.setOnClickListener(v -> addSpotView(null, null, null, null));
        binding.btnAddTransport.setOnClickListener(v -> addTransportView(null, null));
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse("text/plain"), descriptionString);
    }

    private void updatePlaceData() {
        // --- Validation ---
        if (TextUtils.isEmpty(binding.etPlaceName.getText())) {
            Toast.makeText(this, "Place Name is required.", Toast.LENGTH_SHORT).show();
            binding.etPlaceName.requestFocus();
            return;
        }

        showLoading(true);

        // --- Prepare Data Map (like in AddplaceActivity) ---
        Map<String, RequestBody> dataMap = new HashMap<>();
        dataMap.put("placeId", createPartFromString(String.valueOf(placeId))); // CRUCIAL
        dataMap.put("placeName", createPartFromString(binding.etPlaceName.getText().toString()));
        dataMap.put("placeLocation", createPartFromString(binding.etPlaceLocation.getText().toString()));
        dataMap.put("latitude", createPartFromString(binding.etLatitude.getText().toString()));
        dataMap.put("longitude", createPartFromString(binding.etLongitude.getText().toString()));
        dataMap.put("tollCost", createPartFromString(binding.etTollCost.getText().toString()));
        dataMap.put("parkingCost", createPartFromString(binding.etParkingCost.getText().toString()));
        dataMap.put("hotelCostStandard", createPartFromString(binding.etHotelCostStandard.getText().toString()));
        dataMap.put("hotelCostHigh", createPartFromString(binding.etHotelCostHigh.getText().toString()));
        dataMap.put("hotelCostLow", createPartFromString(binding.etHotelCostLow.getText().toString()));
        dataMap.put("foodStdVeg", createPartFromString(binding.etFoodStdVeg.getText().toString()));
        dataMap.put("foodStdNonVeg", createPartFromString(binding.etFoodStdNonVeg.getText().toString()));
        dataMap.put("foodStdCombo", createPartFromString(binding.etFoodStdCombo.getText().toString()));
        dataMap.put("foodHighVeg", createPartFromString(binding.etFoodHighVeg.getText().toString()));
        dataMap.put("foodHighNonVeg", createPartFromString(binding.etFoodHighNonVeg.getText().toString()));
        dataMap.put("foodHighCombo", createPartFromString(binding.etFoodHighCombo.getText().toString()));
        dataMap.put("foodLowVeg", createPartFromString(binding.etFoodLowVeg.getText().toString()));
        dataMap.put("foodLowNonVeg", createPartFromString(binding.etFoodLowNonVeg.getText().toString()));
        dataMap.put("foodLowCombo", createPartFromString(binding.etFoodLowCombo.getText().toString()));
        dataMap.put("isMonsoon", createPartFromString(String.valueOf(binding.switchMonsoon.isChecked())));
        dataMap.put("avg_budget", createPartFromString(binding.etAvgBudget.getText().toString()));
        dataMap.put("local_language", createPartFromString(binding.etLocalLanguage.getText().toString()));

        List<String> selectedMonthsList = new ArrayList<>();
        for (int i = 0; i < binding.monthsGrid.getChildCount(); i++) {
            CheckBox cb = (CheckBox) binding.monthsGrid.getChildAt(i);
            if (cb.isChecked()) selectedMonthsList.add(cb.getText().toString());
        }
        dataMap.put("suitableMonths", createPartFromString(String.join(",", selectedMonthsList)));

        List<Map<String, String>> spotsList = new ArrayList<>();
        for (int i = 0; i < binding.spotsContainer.getChildCount(); i++) {
            View spotView = binding.spotsContainer.getChildAt(i);
            String name = ((EditText) spotView.findViewById(R.id.spot_name_edit_text)).getText().toString();
            if (!name.isEmpty()) {
                Map<String, String> spot = new HashMap<>();
                spot.put("name", name);
                spot.put("description", ((EditText) spotView.findViewById(R.id.spot_desc_edit_text)).getText().toString());
                spot.put("latitude", ((EditText) spotView.findViewById(R.id.spot_latitude_edit_text)).getText().toString());
                spot.put("longitude", ((EditText) spotView.findViewById(R.id.spot_longitude_edit_text)).getText().toString());
                spotsList.add(spot);
            }
        }
        dataMap.put("topSpots", createPartFromString(new Gson().toJson(spotsList)));

        List<Map<String, String>> transportList = new ArrayList<>();
        for (int i = 0; i < binding.transportContainer.getChildCount(); i++) {
            View transportView = binding.transportContainer.getChildAt(i);
            String type = ((Spinner) transportView.findViewById(R.id.transport_type_spinner)).getSelectedItem().toString();
            if (!type.equals("Select Type")) {
                Map<String, String> transport = new HashMap<>();
                transport.put("type", type);
                transport.put("info", ((EditText) transportView.findViewById(R.id.transport_info_edit_text)).getText().toString());
                transport.put("icon", "ic_" + type.toLowerCase());
                transportList.add(transport);
            }
        }
        dataMap.put("transportOptions", createPartFromString(new Gson().toJson(transportList)));

        // Add list of images to delete
        dataMap.put("imagesToDelete", createPartFromString(new Gson().toJson(imagesToDelete)));

        // --- Prepare New Image Files ---
        List<MultipartBody.Part> newImageParts = new ArrayList<>();
        for (int i = 0; i < newImageUris.size(); i++) {
            try {
                Uri uri = newImageUris.get(i);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] fileBytes = getBytesFromInputStream(inputStream);
                RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), fileBytes);
                newImageParts.add(MultipartBody.Part.createFormData("images[]", "image" + i + ".jpg", requestFile));
            } catch (Exception e) {
                showLoading(false);
                Toast.makeText(this, "Error preparing a new image.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // --- Retrofit Call ---
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.updatePlace(dataMap, newImageParts);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(AeditActivity.this, "Place updated successfully!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK); // To notify ManageplaceActivity to refresh
                    finish();
                } else {
                    Toast.makeText(AeditActivity.this, "Update failed: " + (response.body() != null ? response.body().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AeditActivity.this, "Network failure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        inputStream.close();
        return byteBuffer.toByteArray();
    }

    // --- Dynamic View Methods ---
    private void populateMonthsGrid() {
        for (String month : MONTHS) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(month);
            binding.monthsGrid.addView(checkBox);
        }
    }

    private void addImageThumbnail(Uri imageUri, int imageId, String... imageUrl) {
        View thumbnailView = LayoutInflater.from(this).inflate(R.layout.item_image_thumbnail, binding.imageContainer, false);
        ImageView imageView = thumbnailView.findViewById(R.id.image_thumbnail);
        ImageView deleteIcon = thumbnailView.findViewById(R.id.image_delete_icon);

        if (imageUri != null) { // It's a new image
            imageView.setImageURI(imageUri);
        } else if (imageUrl.length > 0) { // It's an existing image
            String fullUrl = RetrofitClient.BASE_URL + imageUrl[0];
            Glide.with(this).load(fullUrl).centerCrop().into(imageView);
        }

        deleteIcon.setOnClickListener(v -> {
            if (imageUri != null) {
                newImageUris.remove(imageUri);
            } else {
                imagesToDelete.add(imageId);
            }
            binding.imageContainer.removeView(thumbnailView);
        });
        binding.imageContainer.addView(thumbnailView, binding.imageContainer.getChildCount() - 1); // Add before the '+' button
    }


    private void addSpotView(String name, String desc, String lat, String lon) {
        View spotView = LayoutInflater.from(this).inflate(R.layout.item_spot_edit, binding.spotsContainer, false);
        ((EditText) spotView.findViewById(R.id.spot_name_edit_text)).setText(name);
        ((EditText) spotView.findViewById(R.id.spot_desc_edit_text)).setText(desc);
        ((EditText) spotView.findViewById(R.id.spot_latitude_edit_text)).setText(lat);
        ((EditText) spotView.findViewById(R.id.spot_longitude_edit_text)).setText(lon);
        spotView.findViewById(R.id.spot_delete_button).setOnClickListener(v -> binding.spotsContainer.removeView(spotView));
        spotView.findViewById(R.id.btn_select_spot_on_map).setOnClickListener(v -> {
            currentSpotViewForMap = spotView;
            mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class));
        });
        binding.spotsContainer.addView(spotView);
    }

    private void addTransportView(String type, String info) {
        View transportView = LayoutInflater.from(this).inflate(R.layout.item_transport_edit, binding.transportContainer, false);
        Spinner transportSpinner = transportView.findViewById(R.id.transport_type_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TRANSPORT_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportSpinner.setAdapter(adapter);
        if (type != null) {
            int spinnerPosition = adapter.getPosition(type);
            transportSpinner.setSelection(spinnerPosition);
        }
        ((EditText) transportView.findViewById(R.id.transport_info_edit_text)).setText(info);
        transportView.findViewById(R.id.transport_delete_button).setOnClickListener(v -> binding.transportContainer.removeView(transportView));
        binding.transportContainer.addView(transportView);
    }
}