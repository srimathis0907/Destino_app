package com.simats.weekend.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.FavoriteFilesManager;
import com.simats.weekend.MediaAdapter;
import com.simats.weekend.MediaViewActivity;
import com.simats.weekend.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMediaFragment extends Fragment implements MediaAdapter.OnMediaClickListener {

    protected String folderName;
    protected List<File> mediaFiles;
    protected MediaAdapter adapter;
    protected ActionMode actionMode;
    protected FavoriteFilesManager favoriteFilesManager;

    // This launcher will refresh the list when we return from the full-screen viewer
    private final ActivityResultLauncher<Intent> mediaViewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshMediaList()
    );

    // Abstract method that subclasses must implement to provide their specific media files
    protected abstract List<File> getMediaFiles();
    protected abstract String getShareType(); // e.g., "image/*" or "video/*"


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            folderName = getArguments().getString("folder_name");
        }
        favoriteFilesManager = FavoriteFilesManager.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_grid, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.media_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        mediaFiles = new ArrayList<>();
        adapter = new MediaAdapter(getContext(), mediaFiles, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMediaList();
    }

    protected void refreshMediaList() {
        mediaFiles.clear();
        mediaFiles.addAll(getMediaFiles());
        // Sort by date, newest first
        mediaFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMediaClick(File file, int position) {
        if (adapter.isSelectionMode()) {
            toggleSelection(file);
        } else {
            // Open the full-screen viewer activity
            Intent intent = new Intent(getActivity(), MediaViewActivity.class);
            ArrayList<File> fileList = new ArrayList<>(mediaFiles);
            intent.putExtra(MediaViewActivity.EXTRA_FILES_LIST, fileList);
            intent.putExtra(MediaViewActivity.EXTRA_CURRENT_POSITION, position);
            mediaViewLauncher.launch(intent);
        }
    }

    @Override
    public void onMediaLongClick(File file, int position) {
        if (!adapter.isSelectionMode()) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
            adapter.setSelectionMode(true);
        }
        toggleSelection(file);
    }

    private void toggleSelection(File file) {
        adapter.toggleSelection(file);
        int count = adapter.getSelectedItemCount();
        if (count == 0) {
            if (actionMode != null) {
                actionMode.finish();
            }
        } else {
            if (actionMode != null) {
                actionMode.setTitle(count + " selected");
                actionMode.invalidate();
            }
        }
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_actions, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                deleteSelectedItems();
                mode.finish();
                return true;
            } else if (id == R.id.action_share) {
                shareSelectedItems();
                mode.finish();
                return true;
            } else if (id == R.id.action_favorite) {
                favoriteSelectedItems();
                mode.finish();
                return true;
            } else if (id == R.id.action_select_all) {
                adapter.selectAll();
                if (actionMode != null) {
                    actionMode.setTitle(adapter.getSelectedItemCount() + " selected");
                }
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
            actionMode = null;
        }
    };

    private void deleteSelectedItems() {
        List<File> selectedFiles = adapter.getSelectedItems();
        new AlertDialog.Builder(getContext())
                .setTitle("Delete " + selectedFiles.size() + " items?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (File file : selectedFiles) {
                        if (file.delete()) {
                            mediaFiles.remove(file);
                            favoriteFilesManager.removeFavorite(file.getAbsolutePath());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Items deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareSelectedItems() {
        List<File> selectedFiles = adapter.getSelectedItems();
        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : selectedFiles) {
            String authority = requireContext().getPackageName() + ".provider";
            Uri uri = FileProvider.getUriForFile(getContext(), authority, file);
            uris.add(uri);
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType(getShareType());
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share " + uris.size() + " items"));
    }

    private void favoriteSelectedItems() {
        List<File> selectedFiles = adapter.getSelectedItems();
        for (File file : selectedFiles) {
            favoriteFilesManager.addFavorite(file.getAbsolutePath());
        }
        Toast.makeText(getContext(), selectedFiles.size() + " items added to favorites", Toast.LENGTH_SHORT).show();
    }
}