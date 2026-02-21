package com.simats.weekend;

import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    public interface OnAddressClickListener {
        void onAddressClick(Address address);
    }

    private final List<Address> addresses;
    private final OnAddressClickListener listener;

    public AddressAdapter(List<Address> addresses, OnAddressClickListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.bind(address, listener);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressLine1, addressLine2;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            addressLine1 = itemView.findViewById(R.id.address_line_1);
            addressLine2 = itemView.findViewById(R.id.address_line_2);
        }

        void bind(final Address address, final OnAddressClickListener listener) {
            addressLine1.setText(address.getFeatureName()); // E.g., "Chennai Institute of Technology"

            // Build the second line of the address
            String address2 = (address.getSubLocality() != null ? address.getSubLocality() + ", " : "") +
                    (address.getLocality() != null ? address.getLocality() : "");
            addressLine2.setText(address2);

            itemView.setOnClickListener(v -> listener.onAddressClick(address));
        }
    }
}