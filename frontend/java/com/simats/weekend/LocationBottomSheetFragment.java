package com.simats.weekend;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationBottomSheetFragment extends BottomSheetDialogFragment {

    // UPDATED INTERFACE: Now passes the full Location object
    public interface LocationListener {
        void onLocationSelected(Location location);
    }

    private LocationListener mListener;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView gpsButtonText;
    private View useCurrentLocationView;
    private RecyclerView suggestionsRecyclerView;
    private AddressAdapter addressAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission is required.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LocationListener) {
            mListener = (LocationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LocationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_bottom_sheet, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LinearLayout useCurrentLocationButton = view.findViewById(R.id.use_current_location_button);
        gpsButtonText = view.findViewById(R.id.gps_button_text);
        EditText searchEditText = view.findViewById(R.id.search_location_edit_text);
        useCurrentLocationView = view.findViewById(R.id.use_current_location_view);
        suggestionsRecyclerView = view.findViewById(R.id.suggestions_recycler_view);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        useCurrentLocationButton.setOnClickListener(v -> checkPermissionAndFetchLocation());
        setupSearch(searchEditText);
        return view;
    }

    private void setupSearch(EditText searchEditText) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                String query = s.toString();
                if (query.length() > 2) {
                    searchHandler.postDelayed(() -> performSearch(query), 500);
                } else {
                    suggestionsRecyclerView.setVisibility(View.GONE);
                    useCurrentLocationView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        executorService.execute(() -> {
            GeocoderNominatim geocoder = new GeocoderNominatim("WeekendApp/1.0");
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", India", 5);
                new Handler(Looper.getMainLooper()).post(() -> displaySuggestions(addresses));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void displaySuggestions(List<Address> addresses) {
        if (addresses != null && !addresses.isEmpty()) {
            useCurrentLocationView.setVisibility(View.GONE);
            suggestionsRecyclerView.setVisibility(View.VISIBLE);
            addressAdapter = new AddressAdapter(addresses, address -> {
                if (address.hasLatitude() && address.hasLongitude()) {
                    Location selectedLocation = new Location("selected");
                    selectedLocation.setLatitude(address.getLatitude());
                    selectedLocation.setLongitude(address.getLongitude());
                    mListener.onLocationSelected(selectedLocation);
                    dismiss();
                }
            });
            suggestionsRecyclerView.setAdapter(addressAdapter);
        }
    }

    private void checkPermissionAndFetchLocation() {
        gpsButtonText.setText("Fetching your location...");
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String address = getAddressFromLocation(location);
                String coords = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", location.getLatitude(), location.getLongitude());
                showLocationDetailsDialog(location, coords, address);
            } else {
                Toast.makeText(getContext(), "Could not get location. Ensure GPS is on.", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    private void showLocationDetailsDialog(Location location, String coords, String address) {
        new AlertDialog.Builder(getContext())
                .setTitle("Location Fetched")
                .setMessage(coords + "\n" + address)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    mListener.onLocationSelected(location); // Pass the whole Location object back
                    dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    gpsButtonText.setText("Use Current Location");
                })
                .show();
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return (address.getSubLocality() != null ? address.getSubLocality() + ", " : "") +
                        (address.getLocality() != null ? address.getLocality() : "Unknown Area");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Current Location";
    }
}