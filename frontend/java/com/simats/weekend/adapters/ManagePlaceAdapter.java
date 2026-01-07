package com.simats.weekend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.simats.weekend.R;
import com.simats.weekend.RetrofitClient;
import com.simats.weekend.models.AdminPlace;
import java.util.List;

public class ManagePlaceAdapter extends RecyclerView.Adapter<ManagePlaceAdapter.PlaceViewHolder> {

    private final Context context;
    private List<AdminPlace> placeList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int placeId);
        void onDeleteClick(int placeId, String placeName);
    }

    public ManagePlaceAdapter(Context context, List<AdminPlace> placeList, OnItemClickListener listener) {
        this.context = context;
        this.placeList = placeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_admin, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        AdminPlace place = placeList.get(position);
        holder.tvPlaceName.setText(place.getName());

        String fullImageUrl = RetrofitClient.BASE_URL + place.getImageUrl();
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.default_placeholder) // Add a default placeholder drawable
                .error(R.drawable.default_placeholder)       // Add an error placeholder drawable
                .centerCrop()
                .into(holder.ivPlaceImage);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(place.getId()));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(place.getId(), place.getName()));
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public void filterList(List<AdminPlace> filteredList) {
        this.placeList = filteredList;
        notifyDataSetChanged();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        TextView tvPlaceName;
        ImageButton btnEdit, btnDelete;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.iv_place_image);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}