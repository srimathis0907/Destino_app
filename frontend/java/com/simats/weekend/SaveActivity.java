package com.simats.weekend;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
// Other imports for the dialog will be needed here

public class SaveActivity extends AppCompatActivity {

    private TextView tvCurrentFolder;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        ImageView ivPreview = findViewById(R.id.iv_save_preview);
        tvCurrentFolder = findViewById(R.id.tv_current_folder);
        Button btnChangeFolder = findViewById(R.id.btn_change_folder);

        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            ivPreview.setImageURI(imageUri);
        }

        updateFolderDisplay();

        btnChangeFolder.setOnClickListener(v -> showSaveFolderDialog());
    }

    private void updateFolderDisplay() {
        String lastFolder = FolderPrefManager.getLastSelectedFolder(this);
        if (lastFolder != null) {
            tvCurrentFolder.setText("Saving to: " + lastFolder);
            // TODO: In a full app, you would save the image to this folder right away
        } else {
            tvCurrentFolder.setText("Select a folder to save");
            // Automatically show the dialog if no folder is pre-selected
            showSaveFolderDialog();
        }
    }

    private void showSaveFolderDialog() {
        // This method will be almost identical to the one we built previously.
        // It will show a dialog with a RecyclerView of folders from FolderManager,
        // a SearchView, and a "Create New Folder" button.
        // When a folder is selected or created, it will:
        // 1. Save the image file to that folder's directory on the device.
        // 2. Use FolderPrefManager.setLastSelectedFolder(this, folderName);
        // 3. finish(); // Close the SaveActivity
    }
}