package com.simats.weekend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.weekend.adapters.TravelTipsAdapter;
import com.simats.weekend.databinding.ActivityManageTipsBinding;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.TipsResponse;
import com.simats.weekend.models.TravelTip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// The class correctly implements the interface
public class ManageTipsActivity extends AppCompatActivity implements TravelTipsAdapter.OnTipInteractionListener {

    private ActivityManageTipsBinding binding;
    private TravelTipsAdapter adapter;
    private List<TravelTip> tipList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        fetchTips();
    }

    private void setupToolbar() {
        binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        binding.recyclerViewTips.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TravelTipsAdapter(tipList, this);
        binding.recyclerViewTips.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnAddNewTip.setOnClickListener(v -> showAddEditTipDialog(null));
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.recyclerViewTips.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateUIState() {
        if (tipList.isEmpty()) {
            binding.recyclerViewTips.setVisibility(View.GONE);
            binding.tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewTips.setVisibility(View.VISIBLE);
            binding.tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    private void fetchTips() {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<TipsResponse> call = apiService.getTravelTips();

        call.enqueue(new Callback<TipsResponse>() {
            @Override
            public void onResponse(Call<TipsResponse> call, Response<TipsResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    tipList.clear();
                    tipList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageTipsActivity.this, "Failed to load tips.", Toast.LENGTH_SHORT).show();
                }
                updateUIState();
            }

            @Override
            public void onFailure(Call<TipsResponse> call, Throwable t) {
                showLoading(false);
                updateUIState();
                Toast.makeText(ManageTipsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(TravelTip tip) {
        showAddEditTipDialog(tip);
    }

    // --- THIS IS THE MISSING METHOD THAT FIXES THE ERROR ---
    @Override
    public void onDeleteClick(TravelTip tip) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tip")
                .setMessage("Are you sure you want to delete this tip?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Call the backend to delete the tip
                    manageTip("delete", tip.getId(), null, null);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    // --------------------------------------------------------

    private void showAddEditTipDialog(final TravelTip tip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_tip, null);
        builder.setView(dialogView);

        final EditText etTitle = dialogView.findViewById(R.id.et_tip_title);
        final EditText etContent = dialogView.findViewById(R.id.et_tip_content);

        builder.setTitle(tip == null ? "Add New Tip" : "Edit Tip");
        if (tip != null) {
            etTitle.setText(tip.getTitle());
            etContent.setText(tip.getContent());
        }

        builder.setPositiveButton(tip == null ? "Add" : "Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tip == null) { // Add new tip
                manageTip("add", 0, title, content);
            } else { // Edit existing tip
                manageTip("update", tip.getId(), title, content);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void manageTip(String action, int id, String title, String content) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("action", action);
        if (id > 0) body.put("id", id);
        if (title != null) body.put("title", title);
        if (content != null) body.put("content", content);

        Call<StatusResponse> call = apiService.manageTravelTip(body);
        call.enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(ManageTipsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    fetchTips(); // Refresh the list from the server
                } else {
                    showLoading(false);
                    Toast.makeText(ManageTipsActivity.this, "Operation failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ManageTipsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}