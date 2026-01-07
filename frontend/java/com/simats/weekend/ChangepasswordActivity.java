package com.simats.weekend;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.weekend.databinding.ChangepasswordBinding;
import com.simats.weekend.models.StatusResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangepasswordActivity extends AppCompatActivity {

    private ChangepasswordBinding binding;
    private SessionManager sessionManager;

    // --- START: NEW PASSWORD VALIDATION PATTERN ---
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[@#$%^&+=!*?])" +    //at least 1 special character
                    ".{6,8}" +              //between 6 and 8 characters
                    "$");
    // --- END: NEW PASSWORD VALIDATION PATTERN ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangepasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupToolbar();
        binding.btnUpdatePassword.setOnClickListener(v -> validateAndSaveChanges());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarChangePassword);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarChangePassword.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void showLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void validateAndSaveChanges() {
        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String currentPassword = Objects.requireNonNull(binding.etCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(binding.etNewPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();

        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError("Current password is required");
            return;
        }

        // --- START: UPDATED PASSWORD VALIDATION ---
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            binding.tilNewPassword.setError("Password must be 6-8 chars with uppercase, lowercase, number & special char");
            return;
        }
        // --- END: UPDATED PASSWORD VALIDATION ---

        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        // All local checks passed, now call the API
        updatePasswordOnServer(currentPassword, newPassword);
    }

    private void updatePasswordOnServer(String currentPassword, String newPassword) {
        showLoading(true);
        int userId = sessionManager.getUserId();

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("current_password", currentPassword);
        body.put("new_password", newPassword);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.changePassword(body);

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    StatusResponse statusResponse = response.body();
                    Toast.makeText(ChangepasswordActivity.this, statusResponse.getMessage(), Toast.LENGTH_LONG).show();

                    if (statusResponse.isStatus()) {
                        finish(); // Close the activity on success
                    } else {
                        // If the server says the current password was wrong, show the error
                        if (statusResponse.getMessage().toLowerCase().contains("incorrect")) {
                            binding.tilCurrentPassword.setError(statusResponse.getMessage());
                        }
                    }
                } else {
                    Toast.makeText(ChangepasswordActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ChangepasswordActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
