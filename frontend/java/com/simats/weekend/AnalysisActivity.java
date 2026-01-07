package com.simats.weekend;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.simats.weekend.databinding.ActivityAnalysisBinding;
import com.simats.weekend.models.AnalysisGraphResponse; // Corrected
import com.simats.weekend.models.AnalysisStatsResponse;
// REMOVED: import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
// REMOVED: import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisActivity extends AppCompatActivity {

    private ActivityAnalysisBinding binding;
    private String startDate;
    private String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFilters();
        fetchStats();
        // Initially load data for the last 30 days
        binding.btnFilterMonth.setChecked(true); // UPDATED ID
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupFilters() {
        // Set default date range to last 30 days
        Calendar cal = Calendar.getInstance();
        endDate = formatDate(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -30);
        startDate = formatDate(cal.getTime());

        // UPDATED to use MaterialButtonToggleGroup listener
        binding.filterToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return; // Only act on the button that was just checked

            Calendar calendar = Calendar.getInstance();
            endDate = formatDate(calendar.getTime());

            if (checkedId == R.id.btn_filter_week) { // UPDATED ID
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startDate = formatDate(calendar.getTime());
                fetchAllChartData();
            } else if (checkedId == R.id.btn_filter_month) { // UPDATED ID
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                startDate = formatDate(calendar.getTime());
                fetchAllChartData();
            } else if (checkedId == R.id.btn_filter_range) { // UPDATED ID
                showDateRangePicker();
            }
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = formatDate(new Date(selection.first));
            endDate = formatDate(new Date(selection.second));
            fetchAllChartData();
            // Manually keep the range button checked after selection
            binding.filterToggleGroup.check(R.id.btn_filter_range);
        });

        datePicker.addOnCancelListener(dialog -> {
            // If user cancels, re-check the button that was active before
            // This is a bit complex, simplest way is to default back to 30 days
            // Or just refetch based on current startDate/endDate
            // For now, we'll just let it be, but if you want to restore previous state,
            // you'd need to store it before opening the picker.
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void fetchStats() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getAnalysisStats().enqueue(new Callback<AnalysisStatsResponse>() {
            @Override
            public void onResponse(Call<AnalysisStatsResponse> call, Response<AnalysisStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    setupStatCards(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<AnalysisStatsResponse> call, Throwable t) {
                Toast.makeText(AnalysisActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllChartData() {
        fetchChartData("trip_status", binding.pieChartStatus);
        // REMOVED: fetchChartData("trips_by_month", binding.barChartMonths);
        fetchChartData("top_cancellers", binding.hbarChartCancellers);
    }

    private void fetchChartData(String chartType, View chartView) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        // **THIS IS THE LINE I FIXED**
        apiService.getAnalysisGraphData(chartType, startDate, endDate).enqueue(new Callback<AnalysisGraphResponse>() {
            @Override
            public void onResponse(Call<AnalysisGraphResponse> call, Response<AnalysisGraphResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<JsonElement> data = response.body().getData();
                    switch (chartType) {
                        case "trip_status":
                            setupPieChart((PieChart) chartView, data);
                            break;
                        // REMOVED: trips_by_month case
                        case "top_cancellers":
                            setupHorizontalBarChart((HorizontalBarChart) chartView, data);
                            break;
                    }
                }
            }
            @Override
            public void onFailure(Call<AnalysisGraphResponse> call, Throwable t) {
                Toast.makeText(AnalysisActivity.this, "Failed to load " + chartType, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStatCards(AnalysisStatsResponse.StatsData data) {
        binding.tvTotalUsers.setText(String.valueOf(data.getTotalUsers()));
        binding.tvOngoingTrips.setText(String.valueOf(data.getOngoingTrips()));
        binding.tvCancellations.setText(String.valueOf(data.getCancellations()));
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        binding.tvAvgCost.setText(currencyFormat.format(data.getAvgCompletedCost()));
    }

    private void setupPieChart(PieChart chart, List<JsonElement> data) {
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(true);
        chart.setEntryLabelTextSize(12f);
        chart.setEntryLabelColor(Color.BLACK);
        chart.animateY(1000);
        chart.getLegend().setWordWrapEnabled(true); // Added for better legend display

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (JsonElement element : data) {
            JsonObject obj = element.getAsJsonObject();
            entries.add(new PieEntry(obj.get("count").getAsFloat(), capitalize(obj.get("status").getAsString())));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Trip Status");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(chart));

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate();
    }

    // REMOVED: setupBarChart() method deleted entirely

    // UPDATED: This method is now cleaner
    private void setupHorizontalBarChart(HorizontalBarChart chart, List<JsonElement> data) {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false); // No right-side Y-axis
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false); // No legend, title is enough
        chart.animateY(1000);

        // Configure Y-axis (Left)
        chart.getAxisLeft().setGranularity(1.0f); // Show only whole numbers
        chart.getAxisLeft().setAxisMinimum(0f); // Start at 0

        // Configure X-axis (Bottom)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JsonObject obj = data.get(i).getAsJsonObject();
            entries.add(new BarEntry(i, obj.get("count").getAsFloat()));
            labels.add(obj.get("fullname").getAsString());
        }

        // Reverse order for horizontal chart to show top user at the top
        java.util.Collections.reverse(entries);
        java.util.Collections.reverse(labels);

        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        BarDataSet dataSet = new BarDataSet(entries, "Cancellation Count");
        dataSet.setColor(Color.parseColor("#FF6B6B")); // Use user's requested red color
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new IntValueFormatter()); // Use custom formatter for whole numbers

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);
        chart.invalidate();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // ADDED: Inner class to format bar chart values as integers
    public class IntValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }
}