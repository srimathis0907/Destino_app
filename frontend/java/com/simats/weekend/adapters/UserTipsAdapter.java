package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.TravelTip;
import java.util.List;

public class UserTipsAdapter extends RecyclerView.Adapter<UserTipsAdapter.ViewHolder> {

    private final List<TravelTip> tipList;

    public UserTipsAdapter(List<TravelTip> tipList) {
        this.tipList = tipList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_travel_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelTip tip = tipList.get(position);
        holder.tvTitle.setText(tip.getTitle());
        holder.tvContent.setText(tip.getContent());
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_tip_title);
            tvContent = itemView.findViewById(R.id.tv_tip_content);
        }
    }
}