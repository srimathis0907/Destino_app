package com.simats.weekend.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.R;
import com.simats.weekend.models.NotificationListItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final Context context;
    private final List<NotificationListItem> itemList;

    public NotificationAdapter(Context context, List<NotificationListItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof NotificationListItem.NotificationHeader) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            NotificationListItem.NotificationHeader header = (NotificationListItem.NotificationHeader) itemList.get(position);
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerTitle.setText(header.title());
        } else {
            NotificationListItem.NotificationItem item = (NotificationListItem.NotificationItem) itemList.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.title.setText(item.title());
            itemHolder.subtitle.setText(item.subtitle());
            itemHolder.time.setText(item.time());
            itemHolder.icon.setImageResource(item.iconResId());

            // Set background color and icon tint
            int color = ContextCompat.getColor(context, item.iconBgColor());
            itemHolder.icon.setBackgroundTintList(ColorStateList.valueOf(color));
            ImageViewCompat.setImageTintList(itemHolder.icon, ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white)));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder for Header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.header_title);
        }
    }

    // ViewHolder for Notification Item
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, subtitle, time;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.notification_icon);
            title = itemView.findViewById(R.id.notification_title);
            subtitle = itemView.findViewById(R.id.notification_subtitle);
            time = itemView.findViewById(R.id.notification_time);
        }
    }
}