package com.simats.weekend.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.ApiService;
import com.simats.weekend.R;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.adapters.NearbyItemAdapter;
import com.simats.weekend.models.NearbyPlace;
import com.simats.weekend.models.NearbyPlacesResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearbyPickerBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_LAT = "latitude";
    private static final String ARG_LNG = "longitude";
    private static final String ARG_TYPE = "type";

    private double latitude, longitude;
    private String type;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView pickerTitle;
    private OnNearbyItemSelectedListener mListener;

    public interface OnNearbyItemSelectedListener {
        void onNearbyItemSelected(NearbyPlace selectedPlace);
    }

    public static NearbyPickerBottomSheet newInstance(double lat, double lng, String type) {
        NearbyPickerBottomSheet fragment = new NearbyPickerBottomSheet();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the hosting activity implements the listener
        if (getParentFragment() instanceof OnNearbyItemSelectedListener) {
            mListener = (OnNearbyItemSelectedListener) getParentFragment();
        } else if (context instanceof OnNearbyItemSelectedListener) {
            mListener = (OnNearbyItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnNearbyItemSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_LAT);
            longitude = getArguments().getDouble(ARG_LNG);
            type = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_nearby_picker, container, false);

        recyclerView = view.findViewById(R.id.nearby_items_recycler_view);
        progressBar = view.findViewById(R.id.picker_progress_bar);
        pickerTitle = view.findViewById(R.id.picker_title);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (type != null) {
            fetchNearbyData(type);
        } else {
            Toast.makeText(getContext(), "Category type not provided.", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return view;
    }

    private void fetchNearbyData(String type) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        String capitalizedType = type.substring(0, 1).toUpperCase() + type.substring(1);
        pickerTitle.setText("Finding Nearby " + capitalizedType + "s...");

        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        Call<NearbyPlacesResponse> call = apiService.getNearbyPlaces(latitude, longitude, type);

        call.enqueue(new Callback<NearbyPlacesResponse>() {
            @Override
            public void onResponse(@NonNull Call<NearbyPlacesResponse> call, @NonNull Response<NearbyPlacesResponse> response) {
                // START: SAFETY CHECK
                // Ensure the fragment is still attached to the activity before updating the UI.
                if (getContext() == null || !isAdded()) {
                    return; // Stop execution if the fragment is not active
                }
                // END: SAFETY CHECK

                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    NearbyItemAdapter adapter = new NearbyItemAdapter(response.body().getData(), nearbyPlace -> {
                        mListener.onNearbyItemSelected(nearbyPlace);
                        dismiss();
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    pickerTitle.setText("No nearby " + type + "s found.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyPlacesResponse> call, @NonNull Throwable t) {
                // START: SAFETY CHECK
                // Ensure the fragment is still attached to the activity before updating the UI.
                if (getContext() == null || !isAdded()) {
                    return; // Stop execution if the fragment is not active
                }
                // END: SAFETY CHECK

                progressBar.setVisibility(View.GONE);
                pickerTitle.setText("Network Error");
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}