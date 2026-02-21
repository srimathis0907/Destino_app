package com.simats.weekend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class SortByFilterFragment extends Fragment {

    interface SortByListener { void onSortBySelected(String filterType); }
    private SortByListener listener;

    public void setListener(SortByListener listener) { this.listener = listener; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_sort_by_filter, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChipGroup chipGroup = view.findViewById(R.id.sort_by_chip_group);
        String[] sortOptions = {"This Month", "Nearby", "Monsoon", "All"};
        String[] filterTags = {"current_month", "nearby", "monsoon", "all"};

        for (int i = 0; i < sortOptions.length; i++) {
            Chip chip = new Chip(getContext());
            chip.setText(sortOptions[i]);
            chip.setTag(filterTags[i]);
            chip.setCheckable(true);
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip checkedChip = group.findViewById(checkedId);
                if (listener != null) {
                    listener.onSortBySelected((String) checkedChip.getTag());
                }
            }
        });
    }
}