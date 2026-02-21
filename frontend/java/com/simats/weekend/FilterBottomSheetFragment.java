package com.simats.weekend;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public static class FilterOptions {
        String filterType = "current_month";
        String month = null;
    }

    public interface OnFiltersAppliedListener {
        void onFiltersApplied(FilterOptions options);
    }

    private OnFiltersAppliedListener listener;
    private FilterOptions currentFilters = new FilterOptions();
    private List<String> categories = Arrays.asList("Sort By", "Month");

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFiltersAppliedListener) {
            listener = (OnFiltersAppliedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFiltersAppliedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView categoryRecyclerView = view.findViewById(R.id.filter_categories_recycler_view);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FilterCategoryAdapter categoryAdapter = new FilterCategoryAdapter(categories, this::showFragmentForCategory);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Show initial fragment
        showFragmentForCategory(0);

        view.findViewById(R.id.apply_filters_button).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFiltersApplied(currentFilters);
            }
            dismiss();
        });

        view.findViewById(R.id.clear_all_button).setOnClickListener(v -> {
            currentFilters = new FilterOptions();
            showFragmentForCategory(0); // Reset to default view
        });
    }

    private void showFragmentForCategory(int position) {
        if (position == 0) { // Sort By
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.filter_options_container, createSortByFragment())
                    .commit();
        } else if (position == 1) { // Month
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.filter_options_container, createMonthFragment())
                    .commit();
        }
    }

    private SortByFilterFragment createSortByFragment() {
        SortByFilterFragment fragment = new SortByFilterFragment();
        fragment.setListener(filterType -> {
            currentFilters.filterType = filterType;
            currentFilters.month = null; // Clear month if a sort by is selected
        });
        return fragment;
    }

    private MonthFilterFragment createMonthFragment() {
        MonthFilterFragment fragment = new MonthFilterFragment();
        fragment.setListener(month -> {
            currentFilters.filterType = "by_month";
            currentFilters.month = month;
        });
        return fragment;
    }
}