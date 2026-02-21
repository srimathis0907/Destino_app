package com.simats.weekend;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterDistanceBottomSheet extends BottomSheetDialogFragment {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_1KM = 1;
    public static final int FILTER_3KM = 3;
    public static final int FILTER_OVER_3KM = 4;

    private FilterListener mListener;

    public interface FilterListener {
        void onFilterSelected(int filterOption);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FilterListener) {
            mListener = (FilterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FilterListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_filter_distance, container, false);

        RadioGroup radioGroup = view.findViewById(R.id.filter_radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_all) {
                mListener.onFilterSelected(FILTER_ALL);
            } else if (checkedId == R.id.radio_1km) {
                mListener.onFilterSelected(FILTER_1KM);
            } else if (checkedId == R.id.radio_3km) {
                mListener.onFilterSelected(FILTER_3KM);
            } else if (checkedId == R.id.radio_over_3km) {
                mListener.onFilterSelected(FILTER_OVER_3KM);
            }
            dismiss();
        });

        return view;
    }
}