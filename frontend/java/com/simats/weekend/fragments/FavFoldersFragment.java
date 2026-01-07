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

public class FavFoldersFragment extends Fragment {
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

        // --- THIS IS THE UPDATED PART ---
        // The click listener now correctly starts the FolderContentActivity
        adapter = new FolderAdapter(
                item -> {
                    Intent intent = new Intent(getActivity(), FolderContentActivity.class);
                    intent.putExtra("folder_name", item.getName());
                    startActivity(intent);
                },
                this::showOptionsDialog
        );
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void showOptionsDialog(FolderItem item) {
        final CharSequence[] options = {"Rename", "Delete", "Remove from Favorites"};

        new AlertDialog.Builder(getContext())
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: renameFolder(item); break;
                        case 1: deleteFolder(item); break;
                        case 2: removeFromFavorites(item); break;
                    }
                })
                .show();
    }

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
                updateFavoriteFolderList();
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
                        updateFavoriteFolderList();
                        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeFromFavorites(FolderItem item) {
        favoritesManager.removeFavorite(item.getName());
        updateFavoriteFolderList(); // Refresh to remove the item from this list
        Toast.makeText(getContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteFolderList() {
        if (adapter == null) return;
        List<FolderItem> favoriteFolderItems = new ArrayList<>();
        File root = getMemoriesRootDirectory();
        if (root != null && root.exists() && root.isDirectory()) {
            File[] folders = root.listFiles(File::isDirectory);
            if (folders != null) {
                Arrays.sort(folders);
                for (File folder : folders) {
                    // Only add the folder if it's marked as a favorite
                    if (favoritesManager.isFavorite(folder.getName())) {
                        favoriteFolderItems.add(new FolderItem(folder.getName(), true));
                    }
                }
            }
        }
        adapter.updateData(favoriteFolderItems);
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
        updateFavoriteFolderList();
    }
}