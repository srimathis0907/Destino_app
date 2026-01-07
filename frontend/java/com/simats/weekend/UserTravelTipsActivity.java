package com.simats.weekend;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.weekend.adapters.UserTipsAdapter;
import com.simats.weekend.databinding.ActivityTravelTipsBinding;
import com.simats.weekend.models.TipsResponse;
import com.simats.weekend.models.TravelTip;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserTravelTipsActivity extends AppCompatActivity {

    private ActivityTravelTipsBinding binding;
    private UserTipsAdapter adapter;
    private List<TravelTip> tipList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTravelTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        fetchTravelTips();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        binding.recyclerViewUserTips.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserTipsAdapter(tipList);
        binding.recyclerViewUserTips.setAdapter(adapter);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.recyclerViewUserTips.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateUIState() {
        if (tipList.isEmpty()) {
            binding.recyclerViewUserTips.setVisibility(View.GONE);
            binding.tvMessage.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewUserTips.setVisibility(View.VISIBLE);
            binding.tvMessage.setVisibility(View.GONE);
        }
    }

    private void fetchTravelTips() {
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
                    binding.tvMessage.setText("Failed to load travel tips.");
                }
                updateUIState();
            }

            @Override
            public void onFailure(Call<TipsResponse> call, Throwable t) {
                showLoading(false);
                binding.tvMessage.setText("Network Error. Please try again.");
                updateUIState();
                Toast.makeText(UserTravelTipsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}