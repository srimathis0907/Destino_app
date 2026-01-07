package com.simats.weekend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.models.FolderItem;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<FolderItem> folderItems = new ArrayList<>();
    private final OnFolderClickListener clickListener;
    private final OnFolderLongClickListener longClickListener;

    public interface OnFolderClickListener {
        void onFolderClick(FolderItem item);
    }

    public interface OnFolderLongClickListener {
        void onFolderLongClick(FolderItem item);
    }

    public FolderAdapter(OnFolderClickListener clickListener, OnFolderLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderItem item = folderItems.get(position);
        holder.bind(item, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return folderItems.size();
    }

    public void updateData(List<FolderItem> newFolderItems) {
        this.folderItems.clear();
        this.folderItems.addAll(newFolderItems);
        notifyDataSetChanged();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderNameTextView;
        ImageView favoriteIcon;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folderNameTextView);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }

        public void bind(final FolderItem item, final OnFolderClickListener clickListener, final OnFolderLongClickListener longClickListener) {
            folderNameTextView.setText(item.getName());
            favoriteIcon.setVisibility(item.isFavorite() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> clickListener.onFolderClick(item));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onFolderLongClick(item);
                return true; // Consume the long click event
            });
        }
    }
}