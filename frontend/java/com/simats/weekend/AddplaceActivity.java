package com.simats.weekend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import com.simats.weekend.models.StatusResponse;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.simats.weekend.databinding.ActivityAddPlaceBinding;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddplaceActivity extends AppCompatActivity {

    private ActivityAddPlaceBinding binding;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private final List<Uri> selectedImageUris = new ArrayList<>();
    private final String[] TRANSPORT_TYPES = {"Select Type", "Bus", "Train", "Flight", "Taxi", "Auto", "Metro"};
    private AlertDialog progressDialog;
    private View currentSpotViewForMap = null;

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        double latitude = extras.getDouble("latitude");
                        double longitude = extras.getDouble("longitude");

                        if (currentSpotViewForMap != null) {
                            EditText etSpotLat = currentSpotViewForMap.findViewById(R.id.spot_latitude_edit_text);
                            EditText etSpotLon = currentSpotViewForMap.findViewById(R.id.spot_longitude_edit_text);
                            etSpotLat.setText(String.valueOf(latitude));
                            etSpotLon.setText(String.valueOf(longitude));
                            currentSpotViewForMap = null;
                        } else {
                            String placeName = extras.getString("placeName");
                            String location = extras.getString("location");
                            binding.etPlaceName.setText(placeName);
                            binding.etPlaceLocation.setText(location);
                            binding.etLatitude.setText(String.valueOf(latitude));
                            binding.etLongitude.setText(String.valueOf(longitude));
                            binding.manualInputContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        buildProgressDialog();
        setupImagePicker();
        setupDynamicFeatures();
        populateMonthsGrid();
        setupButtonClickListeners();
        setupBottomNavigation();
    }

    private void buildProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        progressDialog = builder.create();
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse("text/plain"), descriptionString);
    }

    private void savePlaceData() {
        if (!validateEditTexts(
                binding.etPlaceName, binding.etPlaceLocation, binding.etLatitude, binding.etLongitude,
                binding.etTollCost, binding.etParkingCost, binding.etHotelCostStandard,
                binding.etHotelCostHigh, binding.etHotelCostLow, binding.etFoodStdVeg,
                binding.etFoodStdNonVeg, binding.etFoodStdCombo, binding.etFoodHighVeg,
                binding.etFoodHighNonVeg, binding.etFoodHighCombo, binding.etFoodLowVeg,
                binding.etFoodLowNonVeg, binding.etFoodLowCombo, binding.etAvgBudget, binding.etLocalLanguage
        )) {
            Toast.makeText(this, "Please fill all required text fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        Map<String, RequestBody> dataMap = new HashMap<>();
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
            String desc = ((EditText) spotView.findViewById(R.id.spot_desc_edit_text)).getText().toString();
            String lat = ((EditText) spotView.findViewById(R.id.spot_latitude_edit_text)).getText().toString();
            String lon = ((EditText) spotView.findViewById(R.id.spot_longitude_edit_text)).getText().toString();

            if (!name.isEmpty()) {
                Map<String, String> spot = new HashMap<>();
                spot.put("name", name);
                spot.put("description", desc);
                spot.put("latitude", lat);
                spot.put("longitude", lon);
                spotsList.add(spot);
            }
        }
        dataMap.put("topSpots", createPartFromString(new Gson().toJson(spotsList)));

        List<Map<String, String>> transportList = new ArrayList<>();
        for (int i = 0; i < binding.transportContainer.getChildCount(); i++) {
            View transportView = binding.transportContainer.getChildAt(i);
            String type = ((Spinner) transportView.findViewById(R.id.transport_type_spinner)).getSelectedItem().toString();
            String info = ((EditText) transportView.findViewById(R.id.transport_info_edit_text)).getText().toString();
            if (!type.equals("Select Type")) {
                Map<String, String> transportOption = new HashMap<>();
                transportOption.put("type", type);
                transportOption.put("info", info);
                transportOption.put("icon", "ic_" + type.toLowerCase());
                transportList.add(transportOption);
            }
        }
        dataMap.put("transportOptions", createPartFromString(new Gson().toJson(transportList)));

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (int i = 0; i < selectedImageUris.size(); i++) {
            try {
                Uri uri = selectedImageUris.get(i);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] fileBytes = getBytesFromInputStream(inputStream);
                RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), fileBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("images[]", "image" + i + ".jpg", requestFile);
                imageParts.add(body);
            } catch (Exception e) {
                Log.e("AddPlaceActivity", "File preparation error", e);
                progressDialog.dismiss();
                Toast.makeText(this, "Error preparing an image for upload.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.addPlace(dataMap, imageParts);

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    StatusResponse statusResponse = response.body();
                    // AFTER (New Code)
                    if (statusResponse.isStatus()) {
                        Toast.makeText(AddplaceActivity.this, statusResponse.getMessage(), Toast.LENGTH_LONG).show();
                        clearForm(); // ✅ This line clears the form for the next entry.
                    }else {
                        Toast.makeText(AddplaceActivity.this, "Server Error: " + statusResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorBody = "An unknown error occurred.";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AddPlaceActivity", "Error parsing error body", e);
                    }
                    Toast.makeText(AddplaceActivity.this, "Request Failed. Code: " + response.code() + "\nMessage: " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AddplaceActivity.this, "Network Failure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        inputStream.close();
        return byteBuffer.toByteArray();
    }

    private void clearForm() {
        binding.etPlaceName.setText("");
        binding.etPlaceLocation.setText("");
        binding.etLatitude.setText("");
        binding.etLongitude.setText("");
        binding.etTollCost.setText("");
        binding.etParkingCost.setText("");
        binding.etHotelCostStandard.setText("");
        binding.etHotelCostHigh.setText("");
        binding.etHotelCostLow.setText("");
        binding.etFoodStdVeg.setText("");
        binding.etFoodStdNonVeg.setText("");
        binding.etFoodStdCombo.setText("");
        binding.etFoodHighVeg.setText("");
        binding.etFoodHighNonVeg.setText("");
        binding.etFoodHighCombo.setText("");
        binding.etFoodLowVeg.setText("");
        binding.etFoodLowNonVeg.setText("");
        binding.etFoodLowCombo.setText("");
        binding.etAvgBudget.setText("");
        binding.etLocalLanguage.setText("");

        for (int i = 0; i < binding.monthsGrid.getChildCount(); i++) {
            ((CheckBox) binding.monthsGrid.getChildAt(i)).setChecked(false);
        }
        binding.switchMonsoon.setChecked(false);
        selectedImageUris.clear();
        binding.imageContainer.removeAllViews();
        binding.spotsContainer.removeAllViews();
        addSpotView();
        binding.transportContainer.removeAllViews();
        addTransportView();
        binding.scrollViewContent.fullScroll(ScrollView.FOCUS_UP);
        binding.etPlaceName.requestFocus();
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    private boolean validateEditTexts(EditText... editTexts) {
        for (EditText editText : editTexts) {
            if (editText.getVisibility() == View.VISIBLE && TextUtils.isEmpty(editText.getText().toString().trim())) {
                editText.setError("This field cannot be empty");
                editText.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void addImageToGallery(Uri imageUri) {
        View thumbnailView = LayoutInflater.from(this).inflate(R.layout.item_image_thumbnail, binding.imageContainer, false);
        ImageView imageView = thumbnailView.findViewById(R.id.image_thumbnail);
        ImageView deleteIcon = thumbnailView.findViewById(R.id.image_delete_icon);
        imageView.setImageURI(imageUri);
        deleteIcon.setOnClickListener(v -> {
            binding.imageContainer.removeView(thumbnailView);
            selectedImageUris.remove(imageUri);
        });
        binding.imageContainer.addView(thumbnailView, 0);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                for (Uri uri : uris) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedImageUris.add(uri);
                        addImageToGallery(uri);
                    } catch (SecurityException e) {
                        Log.e("AddPlaceActivity", "Permission error for URI", e);
                        Toast.makeText(this, "Failed to get permission for an image.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setupDynamicFeatures() {
        binding.btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        binding.btnAddSpot.setOnClickListener(v -> addSpotView());
        addSpotView();
        binding.btnAddTransport.setOnClickListener(v -> addTransportView());
        addTransportView();
    }

    private void populateMonthsGrid() {
        binding.monthsGrid.removeAllViews();
        for (String month : MONTHS) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(month);
            binding.monthsGrid.addView(checkBox);
        }
    }

    private void addSpotView() {
        View spotView = LayoutInflater.from(this).inflate(R.layout.item_spot_edit, binding.spotsContainer, false);
        spotView.findViewById(R.id.spot_delete_button).setOnClickListener(v -> binding.spotsContainer.removeView(spotView));

        Button selectOnMapButton = spotView.findViewById(R.id.btn_select_spot_on_map);
        selectOnMapButton.setOnClickListener(v -> {
            currentSpotViewForMap = spotView;
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        binding.spotsContainer.addView(spotView);
    }


    private void addTransportView() {
        View transportView = LayoutInflater.from(this).inflate(R.layout.item_transport_edit, binding.transportContainer, false);
        transportView.findViewById(R.id.transport_delete_button).setOnClickListener(v -> binding.transportContainer.removeView(transportView));
        Spinner transportSpinner = transportView.findViewById(R.id.transport_type_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TRANSPORT_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportSpinner.setAdapter(adapter);
        binding.transportContainer.addView(transportView);
    }

    private void setupButtonClickListeners() {
        binding.btnSavePlace.setOnClickListener(v -> savePlaceData());
        binding.btnClearForm.setOnClickListener(v -> clearForm());
        binding.btnSelectOnMap.setOnClickListener(v -> {
            currentSpotViewForMap = null;
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });
        binding.btnEnterManually.setOnClickListener(v -> {
            binding.manualInputContainer.setVisibility(
                    binding.manualInputContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_add_place);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(getApplicationContext(), AdminhomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_add_place) {
                return true;
            } else if (id == R.id.nav_manage_place) {
                startActivity(new Intent(getApplicationContext(), ManageTripsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), AdminprofileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}