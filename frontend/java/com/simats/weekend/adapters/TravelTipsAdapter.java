package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.TravelTip;
import java.util.List;

public class TravelTipsAdapter extends RecyclerView.Adapter<TravelTipsAdapter.ViewHolder> {

    public interface OnTipInteractionListener {
        void onEditClick(TravelTip tip);
        void onDeleteClick(TravelTip tip);
    }

    private final List<TravelTip> tipList;
    private final OnTipInteractionListener listener;

    public TravelTipsAdapter(List<TravelTip> tipList, OnTipInteractionListener listener) {
        this.tipList = tipList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelTip tip = tipList.get(position);
        holder.tvTitle.setText(tip.getTitle());
        holder.tvDescription.setText(tip.getContent());

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(tip));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(tip));
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_tip_title);
            tvDescription = itemView.findViewById(R.id.tv_tip_content);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}