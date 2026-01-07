package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.weekend.models.StatusResponse;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    private static final String TAG = "VerifyOtpActivity";
    private TextInputEditText otpEditText;
    private Button verifyButton;
    private ProgressBar progressBar;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Keep title empty if set in XML or not needed
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Go back if arrow is pressed

        otpEditText = findViewById(R.id.otpEditText);
        verifyButton = findViewById(R.id.verifyButton);
        progressBar = findViewById(R.id.progressBar);

        // Retrieve the email passed from ForgotPasswordActivity
        if (getIntent().hasExtra("USER_EMAIL")) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
            Log.d(TAG, "Received email: " + userEmail);
        } else {
            Log.e(TAG, "Error: USER_EMAIL extra not found in Intent.");
            Toast.makeText(this, "An error occurred (Email missing). Please try again.", Toast.LENGTH_LONG).show();
            finish(); // Finish if email is missing
            return;
        }

        verifyButton.setOnClickListener(v -> verifyOtpWithServer());
    }

    private void verifyOtpWithServer() {
        String otp = otpEditText.getText().toString().trim();
        if (otp.length() != 6 || !otp.matches("\\d{6}")) { // Added check for digits only
            otpEditText.setError("Please enter a valid 6-digit OTP");
            otpEditText.requestFocus();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Attempting to verify OTP: " + otp + " for email: " + userEmail);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.verifyOtp(userEmail, otp);

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                showLoading(false);
                // --- START: ADDED DETAILED LOGGING ---
                if (response.isSuccessful() && response.body() != null) {
                    StatusResponse statusResponse = response.body();
                    Log.d(TAG, "Verify OTP API Success. Status: " + statusResponse.isStatus() + ", Message: " + statusResponse.getMessage());
                    Toast.makeText(VerifyOtpActivity.this, statusResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    if (statusResponse.isStatus()) {
                        Log.i(TAG, "OTP Verified. Navigating to ResetPasswordActivity.");
                        // If OTP is correct, move to the Reset Password screen
                        Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("USER_EMAIL", userEmail);
                        intent.putExtra("USER_OTP", otp); // Pass the verified OTP
                        startActivity(intent);
                        finish(); // Finish this activity so user can't come back with back button
                    } else {
                        // Status is false (e.g., wrong OTP)
                        Log.w(TAG, "OTP Verification Failed (API returned status: false). Message: " + statusResponse.getMessage());
                        // Optional: Clear the OTP field
                        // otpEditText.setText("");
                    }
                } else {
                    // API call was not successful (e.g., 400 Bad Request, 500 Server Error)
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Verify OTP API Error. Code: " + response.code() + ", Body: " + errorBody);
                    Toast.makeText(VerifyOtpActivity.this, "Verification failed. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Optional: Clear the OTP field
                    // otpEditText.setText("");
                }
                // --- END: ADDED DETAILED LOGGING ---
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Verify OTP Network Failure: " + t.getMessage(), t); // Log the full exception
                Toast.makeText(VerifyOtpActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Optional: Clear the OTP field
                // otpEditText.setText("");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            verifyButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            verifyButton.setEnabled(true);
        }
    }
}