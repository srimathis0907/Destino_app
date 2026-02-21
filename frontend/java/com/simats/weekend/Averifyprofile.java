package com.simats.weekend;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull; // Added for @NonNull annotation
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.simats.weekend.databinding.ActivityAverifyProfileBinding;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.User;
import com.simats.weekend.models.UserTripCountsResponse;
import com.simats.weekend.utils.LoadingDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Averifyprofile extends AppCompatActivity {

    private ActivityAverifyProfileBinding binding;
    private User currentUser;
    private ApiService apiService;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAverifyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);
        apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // Retrieve the user object passed via Intent
        if (getIntent().hasExtra("USER_OBJECT")) {
            currentUser = (User) getIntent().getSerializableExtra("USER_OBJECT");
        }

        if (currentUser == null) {
            Toast.makeText(this, "Error: User data not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupUI();
        loadTripCounts();

        binding.toolbarVerifyProfile.setNavigationOnClickListener(v -> onBackPressed());

        binding.btnBlockUser.setOnClickListener(v -> {
            String newStatus = currentUser.isActive() ? "blocked" : "active";
            updateUserStatus(newStatus);
        });
    }

    private void setupUI() {
        binding.tvProfileInitials.setText(currentUser.getInitials());
        binding.tvProfileName.setText(currentUser.fullname);
        binding.tvProfileEmail.setText(currentUser.email);
        updateStatusUI();
    }

    private void updateStatusUI() {
        if (currentUser.isActive()) {
            binding.tvProfileStatus.setText("Active");
            binding.tvProfileStatus.setBackgroundResource(R.drawable.status_bg_active);
            binding.btnBlockUser.setText("Block This User");
            binding.btnBlockUser.setBackgroundColor(ContextCompat.getColor(this, R.color.red)); // Ensure block is red
        } else {
            binding.tvProfileStatus.setText("Blocked");
            binding.tvProfileStatus.setBackgroundResource(R.drawable.status_bg_blocked);
            binding.btnBlockUser.setText("Unblock This User");
            binding.btnBlockUser.setBackgroundColor(ContextCompat.getColor(this, R.color.green)); // Ensure unblock is green
        }
    }

    private void loadTripCounts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<UserTripCountsResponse> call = apiService.getUserTripCounts(currentUser.id);
        call.enqueue(new Callback<UserTripCountsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserTripCountsResponse> call, @NonNull Response<UserTripCountsResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    UserTripCountsResponse counts = response.body();
                    // UPDATED: Set text for all four counts
                    binding.tvFutureTripsCount.setText(String.valueOf(counts.futureTrips));
                    binding.tvOngoingTripsCount.setText(String.valueOf(counts.ongoingTrips));
                    binding.tvCompletedTripsCount.setText(String.valueOf(counts.completedTrips));
                    binding.tvCancelledTripsCount.setText(String.valueOf(counts.cancelledTrips));
                } else {
                    Toast.makeText(Averifyprofile.this, "Failed to load trip counts.", Toast.LENGTH_SHORT).show();
                    // Optionally set counts to "0" or "-" on failure
                    binding.tvFutureTripsCount.setText("0");
                    binding.tvOngoingTripsCount.setText("0");
                    binding.tvCompletedTripsCount.setText("0");
                    binding.tvCancelledTripsCount.setText("0");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserTripCountsResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(Averifyprofile.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Optionally set counts to "0" or "-" on failure
                binding.tvFutureTripsCount.setText("-");
                binding.tvOngoingTripsCount.setText("-");
                binding.tvCompletedTripsCount.setText("-");
                binding.tvCancelledTripsCount.setText("-");
            }
        });
    }

    private void updateUserStatus(String newStatus) {
        loadingDialog.startLoadingDialog();
        Call<StatusResponse> call = apiService.updateUserStatus(currentUser.id, newStatus);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                loadingDialog.dismissDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(Averifyprofile.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    currentUser.status = newStatus; // Update local user object
                    updateStatusUI(); // Refresh the button and status text
                    setResult(Activity.RESULT_OK); // Notify previous activity (ManageUserActivity)
                } else {
                    Toast.makeText(Averifyprofile.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(Averifyprofile.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}