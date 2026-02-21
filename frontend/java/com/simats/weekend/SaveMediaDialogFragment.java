package com.simats.weekend;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.models.FolderItem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SaveMediaDialogFragment extends DialogFragment {

    private static final String ARG_MEDIA_URI = "media_uri";
    private Uri mediaUri;
    private FolderAdapter folderAdapter;
    private List<FolderItem> allFolderItems = new ArrayList<>();

    private File getMemoriesRootDirectory() {
        if (getContext() == null) return null;
        return new File(getContext().getExternalFilesDir(null), "MyMemories");
    }

    public static SaveMediaDialogFragment newInstance(Uri mediaUri) {
        SaveMediaDialogFragment fragment = new SaveMediaDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEDIA_URI, mediaUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mediaUri = Uri.parse(getArguments().getString(ARG_MEDIA_URI));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_save_media, null);
        builder.setView(view);

        EditText searchEditText = view.findViewById(R.id.searchEditText);
        RecyclerView foldersRecyclerView = view.findViewById(R.id.foldersRecyclerView);
        EditText newFolderEditText = view.findViewById(R.id.newFolderEditText);
        Button saveButton = view.findViewById(R.id.saveButton);

        foldersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- THIS IS THE CORRECTED PART ---
        // 1. The first argument handles the regular click (to save the file).
        // 2. The second argument handles the long click (we do nothing here).
        folderAdapter = new FolderAdapter(
                item -> saveMediaToFolder(item.getName()), // On regular click
                item -> { /* Do nothing on long click in this dialog */ } // On long click
        );

        foldersRecyclerView.setAdapter(folderAdapter);
        loadExistingFolders();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFolders(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        saveButton.setOnClickListener(v -> {
            String newFolderName = newFolderEditText.getText().toString().trim();
            if (newFolderName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a folder name.", Toast.LENGTH_SHORT).show();
            } else {
                saveMediaToFolder(newFolderName);
            }
        });

        return builder.create();
    }

    private void loadExistingFolders() {
        allFolderItems.clear();
        File root = getMemoriesRootDirectory();
        if (root != null && root.exists() && root.isDirectory()) {
            File[] folders = root.listFiles(File::isDirectory);
            if (folders != null) {
                Arrays.sort(folders);
                for (File folder : folders) {
                    // The new adapter needs FolderItem objects. Favorite status is not relevant here.
                    allFolderItems.add(new FolderItem(folder.getName(), false));
                }
            }
        }
        folderAdapter.updateData(allFolderItems);
    }

    private void filterFolders(String query) {
        if (query.isEmpty()) {
            folderAdapter.updateData(allFolderItems);
        } else {
            List<FolderItem> filteredList = allFolderItems.stream()
                    .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            folderAdapter.updateData(filteredList);
        }
    }

    private void saveMediaToFolder(String folderName) {
        if (mediaUri == null) {
            Toast.makeText(getContext(), "Error: No media file to save.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        File destinationFolder = new File(getMemoriesRootDirectory(), folderName);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        String fileExtension = ".jpg"; // Default
        if (getContext() != null && getContext().getContentResolver().getType(mediaUri) != null) {
            fileExtension = getContext().getContentResolver().getType(mediaUri).contains("video") ? ".mp4" : ".jpg";
        }

        File destinationFile = new File(destinationFolder, "MEM_" + System.currentTimeMillis() + fileExtension);

        try (InputStream in = getContext().getContentResolver().openInputStream(mediaUri);
             OutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            Toast.makeText(getContext(), "Saved to " + folderName + " in My Memories!", Toast.LENGTH_LONG).show();
            dismiss();

        } catch (Exception e) {
            Log.e("SaveMedia", "Failed to copy file to app storage", e);
            Toast.makeText(getContext(), "Failed to save file.", Toast.LENGTH_SHORT).show();
        }
    }
}