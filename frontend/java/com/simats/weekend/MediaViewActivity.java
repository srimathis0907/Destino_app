package com.simats.weekend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MediaViewActivity extends AppCompatActivity {

    public static final String EXTRA_FILES_LIST = "files_list";
    public static final String EXTRA_CURRENT_POSITION = "current_position";

    private ViewPager2 viewPager;
    private ArrayList<File> mediaFiles;
    private MediaViewAdapter adapter;
    private FavoriteFilesManager favoriteFilesManager;
    private boolean hasChanges = false;
    private ImageButton favoriteButton;

    // --- NEW: ExoPlayer INSTANCE ---
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);

        Toolbar toolbar = findViewById(R.id.toolbar_media_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favoriteFilesManager = FavoriteFilesManager.getInstance(this);

        mediaFiles = (ArrayList<File>) getIntent().getSerializableExtra(EXTRA_FILES_LIST);
        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);

        viewPager = findViewById(R.id.media_view_pager);
        adapter = new MediaViewAdapter(this, mediaFiles);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        setupBottomBar();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUiForCurrentItem();
                // When page changes, play the video if it's a video, or stop the player if it's an image.
                playOrStopMedia(position);
            }
        });
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void playOrStopMedia(int position) {
        if (player == null) {
            return; // Player not initialized yet
        }

        File currentFile = mediaFiles.get(position);
        String fileName = currentFile.getName().toLowerCase();
        boolean isVideo = fileName.endsWith(".mp4") || fileName.endsWith(".webm") || fileName.endsWith(".mkv");

        // Find the ViewHolder for the current page
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager.getChildAt(0)).findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof MediaViewAdapter.MediaViewHolder) {
            MediaViewAdapter.MediaViewHolder mediaViewHolder = (MediaViewAdapter.MediaViewHolder) viewHolder;

            if (isVideo) {
                // Attach the player to the PlayerView in the ViewHolder
                mediaViewHolder.playerView.setPlayer(player);
                // Create and play the new video
                MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(currentFile));
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            } else {
                // It's an image, so stop the player and detach it from any PlayerView
                player.stop();
                player.clearMediaItems();
                mediaViewHolder.playerView.setPlayer(null);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
        // We need to wait for the layout to be ready before playing the first item
        viewPager.post(() -> playOrStopMedia(viewPager.getCurrentItem()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }


    private void setupBottomBar() {
        ImageButton shareButton = findViewById(R.id.action_share);
        favoriteButton = findViewById(R.id.action_favorite);
        ImageButton deleteButton = findViewById(R.id.action_delete);
        ImageButton detailsButton = findViewById(R.id.action_details);

        updateUiForCurrentItem();

        shareButton.setOnClickListener(v -> shareCurrentMedia());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        deleteButton.setOnClickListener(v -> deleteCurrentMedia());
        detailsButton.setOnClickListener(v -> showDetails());
    }

    private File getCurrentFile() {
        if (mediaFiles.isEmpty() || viewPager.getCurrentItem() >= mediaFiles.size()) return null;
        return mediaFiles.get(viewPager.getCurrentItem());
    }

    private void shareCurrentMedia() {
        File currentFile = getCurrentFile();
        if (currentFile == null) return;
        String authority = "com.example.weekend.fileprovider";
        Uri uri = FileProvider.getUriForFile(this, authority, currentFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        String mimeType = getContentResolver().getType(uri);
        intent.setType(mimeType != null ? mimeType : "*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void toggleFavorite() {
        File currentFile = getCurrentFile();
        if (currentFile == null) return;
        String path = currentFile.getAbsolutePath();
        if (favoriteFilesManager.isFavorite(path)) {
            favoriteFilesManager.removeFavorite(path);
        } else {
            favoriteFilesManager.addFavorite(path);
        }
        updateFavoriteButtonState();
        hasChanges = true;
    }

    private void deleteCurrentMedia() {
        File currentFile = getCurrentFile();
        if (currentFile == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete Item?")
                .setMessage("Are you sure you want to permanently delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int currentPosition = viewPager.getCurrentItem();
                    if (currentFile.delete()) {
                        favoriteFilesManager.removeFavorite(currentFile.getAbsolutePath());
                        mediaFiles.remove(currentPosition);
                        adapter.notifyItemRemoved(currentPosition);
                        adapter.notifyItemRangeChanged(currentPosition, mediaFiles.size());
                        hasChanges = true;
                        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                        if (mediaFiles.isEmpty()) {
                            finish();
                        } else {
                            updateToolbarTitle();
                            updateUiForCurrentItem();
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDetails() {
        File currentFile = getCurrentFile();
        if (currentFile == null) return;
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(currentFile.getName()).append("\n\n");
        String date = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(currentFile.lastModified()));
        details.append("Date: ").append(date).append("\n\n");
        if (currentFile.getName().toLowerCase().endsWith(".jpg") || currentFile.getName().toLowerCase().endsWith(".jpeg")) {
            try {
                ExifInterface exifInterface = new ExifInterface(currentFile.getAbsolutePath());
                float[] latLong = new float[2];
                if (exifInterface.getLatLong(latLong)) {
                    details.append(String.format(Locale.US, "Location: Lat: %.4f, Lng: %.4f", latLong[0], latLong[1]));
                } else {
                    details.append("Location: Not available");
                }
            } catch (IOException e) {
                details.append("Location: Not available");
                Log.e("MediaViewActivity", "Could not read EXIF data", e);
            }
        } else {
            details.append("Location: Not available for videos");
        }
        new AlertDialog.Builder(this)
                .setTitle("Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateUiForCurrentItem() {
        updateFavoriteButtonState();
        updateToolbarTitle();
    }

    private void updateFavoriteButtonState() {
        File currentFile = getCurrentFile();
        if (currentFile == null) return;
        if (favoriteFilesManager.isFavorite(currentFile.getAbsolutePath())) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void updateToolbarTitle() {
        if (mediaFiles.isEmpty()) {
            getSupportActionBar().setTitle("");
        } else {
            getSupportActionBar().setTitle((viewPager.getCurrentItem() + 1) + " of " + mediaFiles.size());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_media_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        // Rotation is handled by the PhotoView library itself, so a button isn't strictly necessary for that
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        super.onBackPressed();
    }
}