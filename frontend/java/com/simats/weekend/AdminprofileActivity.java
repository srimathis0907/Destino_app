package com.simats.weekend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.simats.weekend.databinding.ActivityAdminProfileBinding;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.UserProfileResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminprofileActivity extends AppCompatActivity {

    private ActivityAdminProfileBinding binding;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private SessionManager sessionManager;
    private Uri selectedImageUri = null;

    // --- START: NEW, STRICTER VALIDATION PATTERNS ---
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@(gmail\\.com|mail\\.com)$");
    // --- END: NEW, STRICTER VALIDATION PATTERNS ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupImagePickers();
        setupClickListeners();
        setupBottomNavigation();
        fetchProfileData();
    }

    private boolean validateInput() {
        binding.profileNameLayout.setError(null);
        binding.profileUsernameLayout.setError(null);
        binding.profileEmailLayout.setError(null);
        binding.profilePhoneLayout.setError(null);

        String name = binding.etProfileName.getText().toString().trim();
        String username = binding.etProfileUsername.getText().toString().trim();
        String email = binding.etProfileEmail.getText().toString().trim();
        String phone = binding.etProfilePhone.getText().toString().trim();

        // --- START: UPDATED VALIDATION LOGIC ---
        if (TextUtils.isEmpty(name) || !NAME_PATTERN.matcher(name).matches()) {
            binding.profileNameLayout.setError("Name must contain only letters and spaces.");
            return false;
        }
        if (TextUtils.isEmpty(username) || !USERNAME_PATTERN.matcher(username).matches()) {
            binding.profileUsernameLayout.setError("Username must be letters and numbers only.");
            return false;
        }
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            binding.profileEmailLayout.setError("Please use a valid gmail.com or mail.com address.");
            return false;
        }
        // --- END: UPDATED VALIDATION LOGIC ---

        if (!TextUtils.isEmpty(phone) && phone.length() != 10) {
            binding.profilePhoneLayout.setError("Phone number must be exactly 10 digits.");
            return false;
        }
        return true;
    }

    // --- NO CHANGES to other methods ---
    // The rest of your AdminprofileActivity.java code remains exactly the same.
    private void fetchProfileData() {
        showLoading(true);
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Login session not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<UserProfileResponse> call = apiService.getUserProfile(userId);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    populateForm(response.body().getData());
                } else {
                    Toast.makeText(AdminprofileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AdminprofileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateForm(UserProfileResponse.User user) {
        binding.etProfileName.setText(user.getFullname());
        binding.etProfileUsername.setText(user.getUsername());
        binding.etProfileEmail.setText(user.getEmail());
        binding.etProfilePhone.setText(user.getPhone());
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(RetrofitClient.BASE_URL + user.getProfileImage())
                    .circleCrop()
                    .into(binding.ivProfileImage);
        }
    }

    private void setupClickListeners() {
        binding.btnEditImage.setOnClickListener(v -> showImagePickerDialog());
        binding.btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangepasswordActivity.class)));
        binding.btnSaveChanges.setOnClickListener(v -> {
            if (validateInput()) {
                saveChanges();
            }
        });
    }

    private void saveChanges() {
        showLoading(true);
        int userId = sessionManager.getUserId();
        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("user_id", createPartFromString(String.valueOf(userId)));
        fields.put("fullname", createPartFromString(binding.etProfileName.getText().toString()));
        fields.put("username", createPartFromString(binding.etProfileUsername.getText().toString()));
        fields.put("email", createPartFromString(binding.etProfileEmail.getText().toString()));
        fields.put("phone", createPartFromString(binding.etProfilePhone.getText().toString()));
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call;
        if (selectedImageUri != null) {
            MultipartBody.Part imagePart = prepareFilePart("profile_image", selectedImageUri);
            if (imagePart == null) {
                showLoading(false);
                Toast.makeText(this, "Could not prepare image for upload.", Toast.LENGTH_SHORT).show();
                return;
            }
            call = apiService.updateUserProfile(fields, imagePart);
        } else {
            call = apiService.updateUserProfile(fields);
        }
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AdminprofileActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.body().isStatus()) {
                        fetchProfileData();
                    }
                } else {
                    Toast.makeText(AdminprofileActivity.this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AdminprofileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse("text/plain"), descriptionString);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] fileBytes = getBytesFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), fileBytes);
            return MultipartBody.Part.createFormData(partName, "profile.jpg", requestFile);
        } catch (IOException e) {
            Log.e("FilePrep", "Failed to prepare file part", e);
            return null;
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setupImagePickers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            binding.ivProfileImage.setImageBitmap(imageBitmap);
                            selectedImageUri = getImageUri(imageBitmap);
                        }
                    }
                });
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.ivProfileImage.setImageURI(uri);
                    }
                });
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(takePictureIntent);
                    } else {
                        Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Set Profile Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndLaunch();
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(getApplicationContext(), AdminhomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_add_place) {
                startActivity(new Intent(getApplicationContext(), AddplaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_manage_place) {
                startActivity(new Intent(getApplicationContext(), ManageplaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}