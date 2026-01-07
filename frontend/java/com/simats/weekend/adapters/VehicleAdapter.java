package com.simats.weekend.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.Vehicle;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    public interface OnVehicleDataChangedListener {
        void onDataChanged();
    }

    private List<Vehicle> vehicleList;
    private Context context;
    private OnVehicleDataChangedListener listener;

    public VehicleAdapter(Context context, List<Vehicle> vehicleList, OnVehicleDataChangedListener listener) {
        this.context = context;
        this.vehicleList = vehicleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle_selection, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.bind(vehicle, listener);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvCount;
        ImageButton btnMinus, btnPlus;
        LinearLayout detailsLayout;
        EditText etMileage, etFuelPrice; // Added etFuelPrice
        Spinner spinnerFuel;
        TextWatcher mileageTextWatcher, fuelPriceTextWatcher; // Added fuelPriceTextWatcher

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_vehicle_icon);
            tvName = itemView.findViewById(R.id.tv_vehicle_name);
            tvCount = itemView.findViewById(R.id.tv_vehicle_count);
            btnMinus = itemView.findViewById(R.id.btn_vehicle_minus);
            btnPlus = itemView.findViewById(R.id.btn_vehicle_plus);
            detailsLayout = itemView.findViewById(R.id.layout_vehicle_details);
            etMileage = itemView.findViewById(R.id.et_mileage);
            spinnerFuel = itemView.findViewById(R.id.spinner_fuel_type);
            // ===============================================================
            // == NEW: FIND THE NEW FUEL PRICE EDITTEXT ==
            // ===============================================================
            etFuelPrice = itemView.findViewById(R.id.et_fuel_price_item);
        }

        void bind(Vehicle vehicle, OnVehicleDataChangedListener listener) {
            ivIcon.setImageResource(vehicle.getIconResId());
            tvName.setText(vehicle.getName());
            tvCount.setText(String.valueOf(vehicle.getQuantity()));

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(itemView.getContext(),
                    R.array.fuel_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFuel.setAdapter(adapter);
            spinnerFuel.setSelection(vehicle.getFuelTypeIndex());

            if (vehicle.getQuantity() > 0) {
                detailsLayout.setVisibility(View.VISIBLE);
                if (vehicle.getMileage() > 0) {
                    etMileage.setText(String.valueOf(vehicle.getMileage()));
                } else {
                    etMileage.setText("");
                }
                // ===============================================================
                // == NEW: DISPLAY THE SAVED FUEL PRICE ==
                // ===============================================================
                if (vehicle.getFuelPrice() > 0) {
                    etFuelPrice.setText(String.valueOf(vehicle.getFuelPrice()));
                } else {
                    etFuelPrice.setText("");
                }
            } else {
                detailsLayout.setVisibility(View.GONE);
            }

            btnMinus.setOnClickListener(v -> {
                if (vehicle.getQuantity() > 0) {
                    vehicle.setQuantity(vehicle.getQuantity() - 1);
                    tvCount.setText(String.valueOf(vehicle.getQuantity()));
                    if (vehicle.getQuantity() == 0) detailsLayout.setVisibility(View.GONE);
                    if (listener != null) listener.onDataChanged();
                }
            });

            btnPlus.setOnClickListener(v -> {
                vehicle.setQuantity(vehicle.getQuantity() + 1);
                tvCount.setText(String.valueOf(vehicle.getQuantity()));
                if (vehicle.getQuantity() > 0) detailsLayout.setVisibility(View.VISIBLE);
                if (listener != null) listener.onDataChanged();
            });

            if (mileageTextWatcher != null) {
                etMileage.removeTextChangedListener(mileageTextWatcher);
            }
            mileageTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try { vehicle.setMileage(Double.parseDouble(s.toString())); } catch (NumberFormatException e) { vehicle.setMileage(0); }
                    if (listener != null) listener.onDataChanged();
                }
                @Override public void afterTextChanged(Editable s) {}
            };
            etMileage.addTextChangedListener(mileageTextWatcher);

            // ===============================================================
            // == NEW: ADD TEXT WATCHER FOR THE FUEL PRICE FIELD ==
            // ===============================================================
            if (fuelPriceTextWatcher != null) {
                etFuelPrice.removeTextChangedListener(fuelPriceTextWatcher);
            }
            fuelPriceTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try { vehicle.setFuelPrice(Double.parseDouble(s.toString())); } catch (NumberFormatException e) { vehicle.setFuelPrice(0); }
                    if (listener != null) listener.onDataChanged();
                }
                @Override public void afterTextChanged(Editable s) {}
            };
            etFuelPrice.addTextChangedListener(fuelPriceTextWatcher);


            spinnerFuel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    vehicle.setFuelTypeIndex(position);
                    if (listener != null) listener.onDataChanged();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }
}