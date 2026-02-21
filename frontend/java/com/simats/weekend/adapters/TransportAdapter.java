package com.simats.weekend.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.Transport;
import java.util.List;

public class TransportAdapter extends RecyclerView.Adapter<TransportAdapter.TransportViewHolder> {

    private final Context context;
    private final List<Transport> transportList;
    private final int[] iconColors;

    public TransportAdapter(Context context, List<Transport> transportList) {
        this.context = context;
        this.transportList = transportList;
        // Define an array of colors to cycle through for the icons
        this.iconColors = new int[]{
                Color.parseColor("#00B5B0"), // Teal
                Color.parseColor("#FF6B6B"), // Coral
                Color.parseColor("#4ECDC4"), // Lighter Teal
                Color.parseColor("#FFC107"), // Amber
                Color.parseColor("#5A67D8")  // Indigo
        };
    }

    @NonNull
    @Override
    public TransportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transport, parent, false);
        return new TransportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransportViewHolder holder, int position) {
        if (transportList != null && !transportList.isEmpty()) {
            Transport transport = transportList.get(position);

            holder.transportType.setText(transport.getType());
            holder.transportInfo.setText(transport.getInfo());

            int iconId = context.getResources().getIdentifier(transport.getIcon(), "drawable", context.getPackageName());
            if (iconId != 0) {
                holder.transportIcon.setImageResource(iconId);
            } else {
                holder.transportIcon.setImageResource(R.drawable.ic_default_transport);
            }

            // Set a unique color for each icon
            int color = iconColors[position % iconColors.length];
            holder.transportIcon.setImageTintList(ColorStateList.valueOf(color));

            // Set animation
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            holder.itemView.startAnimation(animation);
        }
    }

    @Override
    public int getItemCount() {
        return transportList != null ? transportList.size() : 0;
    }

    static class TransportViewHolder extends RecyclerView.ViewHolder {
        ImageView transportIcon;
        TextView transportType, transportInfo;

        TransportViewHolder(@NonNull View itemView) {
            super(itemView);
            transportIcon = itemView.findViewById(R.id.transport_icon);
            transportType = itemView.findViewById(R.id.transport_type);
            transportInfo = itemView.findViewById(R.id.transport_info);
        }
    }
}