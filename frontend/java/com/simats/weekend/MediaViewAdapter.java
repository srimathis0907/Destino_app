package com.simats.weekend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;
import java.util.List;

public class MediaViewAdapter extends RecyclerView.Adapter<MediaViewAdapter.MediaViewHolder> {

    private final Context context;
    private final List<File> mediaFiles;

    public MediaViewAdapter(Context context, List<File> mediaFiles) {
        this.context = context;
        this.mediaFiles = mediaFiles;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fullscreen_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        File mediaFile = mediaFiles.get(position);
        String fileName = mediaFile.getName().toLowerCase();
        boolean isVideo = fileName.endsWith(".mp4") || fileName.endsWith(".webm") || fileName.endsWith(".mkv");

        if (isVideo) {
            // This is a video file
            holder.photoView.setVisibility(View.GONE);
            holder.playerView.setVisibility(View.VISIBLE);
        } else {
            // This is an image file
            holder.playerView.setVisibility(View.GONE);
            holder.photoView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(mediaFile)
                    .fitCenter()
                    .into(holder.photoView);
        }
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        PlayerView playerView; // The new video player view

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.fullscreen_image_view);
            playerView = itemView.findViewById(R.id.player_view); // Find the new view
        }
    }
}