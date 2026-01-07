package com.simats.weekend.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.FolderAdapter;
import com.simats.weekend.FolderContentActivity;
import com.simats.weekend.FavoritesManager;
import com.simats.weekend.R;
import com.simats.weekend.models.FolderItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FoldersFragment extends Fragment {
    private FolderAdapter adapter;
    private FavoritesManager favoritesManager;

    private File getMemoriesRootDirectory() {
        if (getContext() == null) return null;
        return new File(getContext().getExternalFilesDir(null), "MyMemories");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesManager = FavoritesManager.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_folders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FolderAdapter(
                item -> {
                    Intent intent = new Intent(getActivity(), FolderContentActivity.class);
                    intent.putExtra("folder_name", item.getName());
                    startActivity(intent);
                },
                this::showOptionsDialog // Use the updated method
        );
        recyclerView.setAdapter(adapter);
        return view;
    }

    // --- REWRITTEN METHOD TO BE DYNAMIC ---
    private void showOptionsDialog(FolderItem item) {
        // Check if the item is already a favorite to change the dialog text
        boolean isCurrentlyFavorite = favoritesManager.isFavorite(item.getName());
        final String favoriteOptionText = isCurrentlyFavorite ? "Remove from Favorites" : "Add to Favorites";

        final CharSequence[] options = {"Rename", "Delete", favoriteOptionText};

        new AlertDialog.Builder(requireContext())
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Rename
                            renameFolder(item);
                            break;
                        case 1: // Delete
                            deleteFolder(item);
                            break;
                        case 2: // Add or Remove from Favorites
                            toggleFavorite(item);
                            break;
                    }
                })
                .show();
    }

    // --- NEW METHOD to handle both adding and removing favorites ---
    private void toggleFavorite(FolderItem item) {
        boolean isCurrentlyFavorite = favoritesManager.isFavorite(item.getName());
        if (isCurrentlyFavorite) {
            favoritesManager.removeFavorite(item.getName());
            Toast.makeText(getContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoritesManager.addFavorite(item.getName());
            Toast.makeText(getContext(), "Added to Favorites", Toast.LENGTH_SHORT).show();
        }
        updateFolderList(); // Refresh the list to show the change
    }

    // This method is now obsolete and has been replaced by toggleFavorite
    // private void addToFavorites(FolderItem item) { ... }

    // --- All other methods remain the same ---

    private void renameFolder(FolderItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename Folder");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(item.getName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty() || newName.equals(item.getName())) return;

            File root = getMemoriesRootDirectory();
            File oldFile = new File(root, item.getName());
            File newFile = new File(root, newName);

            if (oldFile.renameTo(newFile)) {
                favoritesManager.renameFavorite(item.getName(), newName);
                updateFolderList();
                Toast.makeText(getContext(), "Renamed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Rename failed", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteFolder(FolderItem item) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Folder")
                .setMessage("Are you sure you want to delete '" + item.getName() + "' and all its contents?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    File fileToDelete = new File(getMemoriesRootDirectory(), item.getName());
                    if (deleteRecursive(fileToDelete)) {
                        favoritesManager.removeFavorite(item.getName());
                        updateFolderList();
                        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFolderList() {
        if (adapter == null) return;
        List<FolderItem> folderItems = new ArrayList<>();
        File root = getMemoriesRootDirectory();
        if (root != null && root.exists() && root.isDirectory()) {
            File[] folders = root.listFiles(File::isDirectory);
            if (folders != null) {
                Arrays.sort(folders);
                for (File folder : folders) {
                    boolean isFav = favoritesManager.isFavorite(folder.getName());
                    // Corrected to include item count, assuming FolderItem has this constructor
                    int itemCount = (folder.listFiles() != null) ? folder.listFiles().length : 0;
                    folderItems.add(new FolderItem(folder.getName(), itemCount, isFav));
                }
            }
        }
        adapter.updateData(folderItems);
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFolderList();
    }
}