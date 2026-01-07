package com.simats.weekend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;

public class FullscreenMediaActivity extends AppCompatActivity {

    private File mediaFile; // Store the file reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_media);

        ImageView fullscreenImage = findViewById(R.id.fullscreen_image);
        VideoView fullscreenVideo = findViewById(R.id.fullscreen_video);
        ImageButton shareButton = findViewById(R.id.btn_share);

        String mediaPath = getIntent().getStringExtra("mediaPath");

        if (mediaPath == null || mediaPath.isEmpty()) {
            Toast.makeText(this, "Error: Media path not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mediaFile = new File(mediaPath);
        if (!mediaFile.exists()) {
            Toast.makeText(this, "Error: Media file does not exist.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (mediaPath.toLowerCase().endsWith(".mp4")) {
            fullscreenVideo.setVisibility(View.VISIBLE);
            fullscreenImage.setVisibility(View.GONE);
            fullscreenVideo.setVideoURI(Uri.fromFile(mediaFile));
            fullscreenVideo.setOnPreparedListener(mp -> mp.setLooping(true));
            fullscreenVideo.start();
        } else {
            fullscreenImage.setVisibility(View.VISIBLE);
            fullscreenVideo.setVisibility(View.GONE);
            Glide.with(this).load(mediaFile).into(fullscreenImage);
        }

        // --- NEW: Share Button Logic ---
        shareButton.setOnClickListener(v -> shareMediaFile());
    }

    private void shareMediaFile() {
        if (mediaFile == null || !mediaFile.exists()) {
            Toast.makeText(this, "Cannot share file.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use FileProvider to create a secure content URI
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.example.weekend.fileprovider", // MUST match authorities in AndroidManifest
                mediaFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, mediaUri);

        // Determine the MIME type (e.g., "image/jpeg" or "video/mp4")
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(mediaUri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        shareIntent.setType(mimeType);

        // Grant read permission to the receiving app
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share Media Using..."));
    }
}