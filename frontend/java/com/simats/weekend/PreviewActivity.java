package com.simats.weekend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.models.FolderItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PreviewActivity extends AppCompatActivity {

    private Uri mediaUri;
    private boolean isPhoto;
    private String tripFolderName = null;

    // --- NEW: ExoPlayer variable ---
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        ImageView imagePreview = findViewById(R.id.image_preview);
        playerView = findViewById(R.id.player_view); // Find the new PlayerView
        Button retakeButton = findViewById(R.id.retake_button);
        Button saveButton = findViewById(R.id.save_button);

        mediaUri = Uri.parse(getIntent().getStringExtra("media_uri"));
        isPhoto = getIntent().getBooleanExtra("is_photo", true);
        tripFolderName = getIntent().getStringExtra("TRIP_FOLDER_NAME");

        if (isPhoto) {
            imagePreview.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            imagePreview.setImageURI(mediaUri);
        } else {
            imagePreview.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            // The initializePlayer method will be called in onStart()
        }

        retakeButton.setOnClickListener(v -> {
            try {
                getContentResolver().delete(mediaUri, null, null);
            } catch (Exception e) { /* Ignore */ }
            setResult(RESULT_CANCELED);
            finish();
        });

        saveButton.setOnClickListener(v -> handleSave());
    }

    // --- NEW: ExoPlayer Initialization and Lifecycle Management ---

    @Override
    protected void onStart() {
        super.onStart();
        if (!isPhoto) {
            initializePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void initializePlayer() {
        // Create an ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();
        // Bind the player to the view
        playerView.setPlayer(player);
        // Create a media item from the URI
        MediaItem mediaItem = MediaItem.fromUri(mediaUri);
        // Set the media item to be played
        player.setMediaItem(mediaItem);
        // Set player to repeat video
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
        // Prepare the player
        player.prepare();
        // Start playback automatically
        player.play();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // --- The rest of the file remains the same ---

    private void handleSave() {
        if (tripFolderName != null) {
            SharedPreferences prefs = getSharedPreferences("TripFolders", Context.MODE_PRIVATE);
            String tripFolderKey = "folder_" + tripFolderName;
            if (prefs.contains(tripFolderKey)) {
                String finalFolderName = prefs.getString(tripFolderKey, tripFolderName);
                saveMediaToFolder(finalFolderName);
            } else {
                showSaveDialog();
            }
        } else {
            showSaveDialog();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_save_media, null);
        builder.setView(dialogView).setCancelable(false);
        RecyclerView foldersRecyclerView = dialogView.findViewById(R.id.foldersRecyclerView);
        EditText newFolderEditText = dialogView.findViewById(R.id.newFolderEditText);
        Button saveMediaButton = dialogView.findViewById(R.id.saveButton);
        final AlertDialog dialog = builder.create();
        foldersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<FolderItem> existingFolders = getExistingFolders();
        FolderAdapter folderAdapter = new FolderAdapter(item -> {
            saveMediaToFolder(item.getName());
            dialog.dismiss();
        }, item -> {});
        folderAdapter.updateData(existingFolders);
        foldersRecyclerView.setAdapter(folderAdapter);
        saveMediaButton.setOnClickListener(v -> {
            String folderName = newFolderEditText.getText().toString().trim();
            if (folderName.isEmpty()) {
                Toast.makeText(this, "Please enter a folder name or select one.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveMediaToFolder(folderName);
            dialog.dismiss();
        });
        dialog.show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void saveMediaToFolder(String folderName) {
        if (tripFolderName != null) {
            SharedPreferences prefs = getSharedPreferences("TripFolders", Context.MODE_PRIVATE);
            String tripFolderKey = "folder_" + tripFolderName;
            prefs.edit().putString(tripFolderKey, folderName).apply();
        }
        String fileName = getFileName(mediaUri);
        if (fileName == null) {
            Toast.makeText(this, "Could not determine file name.", Toast.LENGTH_SHORT).show();
            return;
        }
        File rootDir = new File(getExternalFilesDir(null), "MyMemories");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        File destinationFolder = new File(rootDir, folderName);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }
        File destinationFile = new File(destinationFolder, fileName);
        try (InputStream in = getContentResolver().openInputStream(mediaUri);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            getContentResolver().delete(mediaUri, null, null);
            Toast.makeText(this, "Saved to " + folderName, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent());
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save file.", Toast.LENGTH_SHORT).show();
        }
    }

    private List<FolderItem> getExistingFolders() {
        List<FolderItem> folderItems = new ArrayList<>();
        File root = new File(getExternalFilesDir(null), "MyMemories");
        if (root.exists() && root.isDirectory()) {
            File[] folders = root.listFiles(File::isDirectory);
            if (folders != null) {
                Arrays.sort(folders);
                for (File folder : folders) {
                    folderItems.add(new FolderItem(folder.getName(), false));
                }
            }
        }
        return folderItems;
    }
}