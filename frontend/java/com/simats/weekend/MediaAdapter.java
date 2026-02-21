package com.simats.weekend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private final Context context;
    private final List<File> mediaFiles;
    private final OnMediaClickListener listener;
    private boolean isSelectionMode = false;
    private final Set<File> selectedItems = new HashSet<>();

    public interface OnMediaClickListener {
        void onMediaClick(File file, int position);
        void onMediaLongClick(File file, int position);
    }

    public MediaAdapter(Context context, List<File> mediaFiles, OnMediaClickListener listener) {
        this.context = context;
        this.mediaFiles = mediaFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        File mediaFile = mediaFiles.get(position);

        Glide.with(context)
                .load(mediaFile)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.image_error)
                .into(holder.thumbnail);

        // ===============================================================
        // == UPDATED: SHOW PLAY ICON FOR VIDEOS ==
        // ===============================================================
        String fileName = mediaFile.getName().toLowerCase();
        if (fileName.endsWith(".mp4") || fileName.endsWith(".webm") || fileName.endsWith(".mkv")) {
            holder.playIcon.setVisibility(View.VISIBLE);
        } else {
            holder.playIcon.setVisibility(View.GONE);
        }

        holder.selectionOverlay.setVisibility(selectedItems.contains(mediaFile) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onMediaClick(mediaFile, holder.getAdapterPosition()));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onMediaLongClick(mediaFile, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    // --- (All helper methods for selection mode remain the same) ---
    public void toggleSelection(File file) { if (selectedItems.contains(file)) { selectedItems.remove(file); } else { selectedItems.add(file); } notifyDataSetChanged(); }
    public void clearSelections() { isSelectionMode = false; selectedItems.clear(); notifyDataSetChanged(); }
    public int getSelectedItemCount() { return selectedItems.size(); }
    public List<File> getSelectedItems() { return new ArrayList<>(selectedItems); }
    public void selectAll() { if (selectedItems.size() == mediaFiles.size()) { selectedItems.clear(); } else { selectedItems.addAll(mediaFiles); } notifyDataSetChanged(); }
    public boolean isSelectionMode() { return isSelectionMode; }
    public void setSelectionMode(boolean selectionMode) { isSelectionMode = selectionMode; }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, playIcon; // Added playIcon
        View selectionOverlay;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.media_thumbnail);
            selectionOverlay = itemView.findViewById(R.id.selection_overlay);
            playIcon = itemView.findViewById(R.id.video_play_icon); // Find the new icon
        }
    }
}