package com.simats.weekend;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final Context context;
    private final List<String> mediaPaths;

    public ImageAdapter(Context context, List<String> mediaPaths) {
        this.context = context;
        this.mediaPaths = mediaPaths;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String mediaPath = mediaPaths.get(position);
        File mediaFile = new File(mediaPath);

        // Check if the file is a video
        if (mediaPath.endsWith(".mp4")) {
            // Load a video thumbnail
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(mediaFile.getAbsolutePath());
                Glide.with(context)
                        .load(retriever.getFrameAtTime())
                        .centerCrop()
                        .into(holder.imageView);
            } catch (Exception e) {
                // Handle cases where thumbnail can't be retrieved
                holder.imageView.setImageResource(R.drawable.ic_default_video_thumbnail); // Use a default icon
            } finally {
                try {
                    retriever.release();
                } catch (Exception e) {
                    // Ignore
                }
            }
            holder.playIcon.setVisibility(View.VISIBLE);
        } else {
            // Load a regular image
            Glide.with(context)
                    .load(mediaFile)
                    .centerCrop()
                    .into(holder.imageView);
            holder.playIcon.setVisibility(View.GONE);
        }

        // Handle item click to open fullscreen media
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullscreenMediaActivity.class);
            intent.putExtra("mediaPath", mediaPath);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mediaPaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView playIcon;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            playIcon = itemView.findViewById(R.id.play_icon_overlay);
        }
    }
}