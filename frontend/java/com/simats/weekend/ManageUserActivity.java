package com.simats.weekend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.weekend.adapters.UserAdapter;
import com.simats.weekend.databinding.ActivityManageUsersBinding;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.User;
import com.simats.weekend.models.UserListResponse;
import com.simats.weekend.utils.LoadingDialog; // NEW IMPORT
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUserActivity extends AppCompatActivity implements UserAdapter.UserActionsListener {

    private ActivityManageUsersBinding binding;
    private UserAdapter adapter;
    private List<User> fullUserList = new ArrayList<>();
    private ActivityResultLauncher<Intent> userDetailsLauncher;
    private LoadingDialog loadingDialog; // NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this); // NEW: Initialize the dialog

        userDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        fetchUsers();
                    }
                });

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        fetchUsers();
    }

    private void setupToolbar() {
        binding.toolbarManageUsers.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(new ArrayList<>(), this);
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUsers.setAdapter(adapter);
    }

    private void fetchUsers() {
        // UPDATED: Show page loader, hide content
        binding.progressBarUsers.setVisibility(View.VISIBLE);
        binding.recyclerViewUsers.setVisibility(View.GONE);
        binding.tvNotFound.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<UserListResponse> call = apiService.getAllUsers();

        call.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                binding.progressBarUsers.setVisibility(View.GONE); // Hide loader
                if (response.isSuccessful() && response.body() != null && !response.body().error) {
                    fullUserList.clear();
                    fullUserList.addAll(response.body().users);
                    adapter.updateUsers(fullUserList);
                    updateSummaryCounts();
                    filter(binding.searchViewUsers.getQuery().toString()); // This will handle showing the list or "not found"
                } else {
                    Toast.makeText(ManageUserActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    binding.tvNotFound.setText("Failed to load data");
                    binding.tvNotFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                binding.progressBarUsers.setVisibility(View.GONE); // Hide loader
                Toast.makeText(ManageUserActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.tvNotFound.setText("Network Error");
                binding.tvNotFound.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSearch() {
        binding.searchViewUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<User> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(fullUserList);
        } else {
            for (User item : fullUserList) {
                if (item.fullname.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) ||
                        item.email.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                    filteredList.add(item);
                }
            }
        }

        if (filteredList.isEmpty() && !fullUserList.isEmpty()) {
            binding.recyclerViewUsers.setVisibility(View.GONE);
            binding.tvNotFound.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewUsers.setVisibility(View.VISIBLE);
            binding.tvNotFound.setVisibility(View.GONE);
        }
        adapter.updateUsers(filteredList);
    }

    private void updateSummaryCounts() {
        int total = fullUserList.size();
        int active = 0;
        for (User user : fullUserList) {
            if (user.isActive()) {
                active++;
            }
        }
        int blocked = total - active;

        binding.tvTotalUsersCount.setText(String.valueOf(total));
        binding.tvActiveUsersCount.setText(String.valueOf(active));
        binding.tvBlockedUsersCount.setText(String.valueOf(blocked));
    }

    @Override
    public void onViewDetails(User user) {
        Intent intent = new Intent(this, Averifyprofile.class);
        intent.putExtra("USER_OBJECT", user);
        userDetailsLauncher.launch(intent);
    }

    @Override
    public void onUpdateStatus(User user, String newStatus) {
        loadingDialog.startLoadingDialog(); // UPDATED: Show action dialog
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<StatusResponse> call = apiService.updateUserStatus(user.id, newStatus);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                loadingDialog.dismissDialog(); // UPDATED: Hide action dialog
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(ManageUserActivity.this, "User status updated", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                } else {
                    Toast.makeText(ManageUserActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                loadingDialog.dismissDialog(); // UPDATED: Hide action dialog
                Toast.makeText(ManageUserActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}