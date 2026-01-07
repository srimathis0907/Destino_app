package com.simats.weekend.adapters;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.Transport;
import java.util.List;

public class TransportOptionsAdapter extends RecyclerView.Adapter<TransportOptionsAdapter.TransportViewHolder> {

    private final List<Transport> transportList;

    public TransportOptionsAdapter(List<Transport> transportList) {
        this.transportList = transportList;
    }

    @NonNull
    @Override
    public TransportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transport_option_draggable, parent, false);
        return new TransportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransportViewHolder holder, int position) {
        Transport transport = transportList.get(position);
        holder.transportName.setText(transport.getType());
        holder.transportIcon.setImageResource(transport.getIconResourceId(holder.itemView.getContext()));

        holder.itemView.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item(String.valueOf(position));
            ClipData dragData = new ClipData("transport", new String[]{ "text/plain" }, item);
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(holder.itemView);
            v.startDragAndDrop(dragData, myShadow, null, 0);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transportList.size();
    }

    static class TransportViewHolder extends RecyclerView.ViewHolder {
        TextView transportName;
        ImageView transportIcon;
        TransportViewHolder(View itemView) {
            super(itemView);
            transportName = itemView.findViewById(R.id.transport_option_name);
            transportIcon = itemView.findViewById(R.id.transport_option_icon);
        }
    }
}