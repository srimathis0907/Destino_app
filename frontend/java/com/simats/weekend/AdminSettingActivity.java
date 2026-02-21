package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.simats.weekend.models.StatusResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSettingActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_setting);

        sessionManager = new SessionManager(this);

        ImageView backArrow = findViewById(R.id.backArrow);
        LinearLayout travelTipsLayout = findViewById(R.id.travelTipsLayout);
        LinearLayout analysisLayout = findViewById(R.id.analysisLayout);
        LinearLayout aboutAppLayout = findViewById(R.id.aboutAppLayout);
        LinearLayout contactSupportLayout = findViewById(R.id.contactSupportLayout);
        LinearLayout termsLayout = findViewById(R.id.termsLayout);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);

        backArrow.setOnClickListener(v -> onBackPressed());
        travelTipsLayout.setOnClickListener(v -> startActivity(new Intent(this, ManageTipsActivity.class)));
        analysisLayout.setOnClickListener(v -> startActivity(new Intent(this, AnalysisActivity.class)));

        // UPDATED Click Listeners
        aboutAppLayout.setOnClickListener(v -> startActivity(new Intent(this, AboutAppActivity.class)));
        contactSupportLayout.setOnClickListener(v -> startActivity(new Intent(this, AdminContactSupport.class)));
        termsLayout.setOnClickListener(v -> startActivity(new Intent(this, TermsPolicyActivity.class)));

        logoutButton.setOnClickListener(v -> showLogoutDialog());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Deleting account...", Toast.LENGTH_SHORT).show();
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Integer> body = new HashMap<>();
        body.put("user_id", userId);

        Call<StatusResponse> call = apiService.deleteAccount(body);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(AdminSettingActivity.this, "Account deleted successfully.", Toast.LENGTH_LONG).show();
                    sessionManager.logoutUser();
                    Intent intent = new Intent(AdminSettingActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminSettingActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                Toast.makeText(AdminSettingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}