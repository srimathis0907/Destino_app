package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private Button sendResetLinkButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Setup Toolbar with your custom back icon
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // No title needed in the toolbar itself
        }
        // This makes your ic_back button work
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Views
        emailEditText = findViewById(R.id.emailEditText);
        sendResetLinkButton = findViewById(R.id.sendResetLinkButton);
        progressBar = findViewById(R.id.progressBar);

        // Set listener for the button
        sendResetLinkButton.setOnClickListener(v -> sendOtpRequest());
    }

    private void sendOtpRequest() {
        String email = emailEditText.getText().toString().trim();

        // --- Input Validation ---
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        showLoading(true);

        // --- API Call to your forgot_password.php ---
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.forgotPassword(email);

        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    StatusResponse statusResponse = response.body();
                    Toast.makeText(ForgotPasswordActivity.this, statusResponse.getMessage(), Toast.LENGTH_LONG).show();

                    if (statusResponse.isStatus()) {
                        // If OTP was sent successfully, move to the Verify OTP screen
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("USER_EMAIL", email); // Pass the email to the next screen
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: Could not process request.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Log.e("ForgotPassword", "API Failure: " + t.getMessage());
                Toast.makeText(ForgotPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            sendResetLinkButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            sendResetLinkButton.setEnabled(true);
        }
    }
}