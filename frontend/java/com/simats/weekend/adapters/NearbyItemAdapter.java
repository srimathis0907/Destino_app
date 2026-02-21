package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.NearbyPlace;
import java.util.List;

public class NearbyItemAdapter extends RecyclerView.Adapter<NearbyItemAdapter.ItemViewHolder> {
    private final List<NearbyPlace> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NearbyPlace nearbyPlace);
    }

    public NearbyItemAdapter(List<NearbyPlace> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_nearby_picker, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        NearbyPlace place = items.get(position);
        holder.name.setText(place.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(place));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
        }
    }
}