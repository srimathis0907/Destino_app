package com.simats.weekend.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Fetches media files that are marked as favorites
public abstract class BaseFavMediaFragment extends BaseMediaFragment {

    // Subclasses decide which file names are relevant (photos vs videos)
    protected abstract boolean isCorrectFileType(String fileName);

    @Override
    protected List<File> getMediaFiles() {
        List<File> files = new ArrayList<>();
        if (getContext() == null) return files;

        Set<String> favoritePaths = favoriteFilesManager.getAllFavorites();
        for (String path : favoritePaths) {
            File file = new File(path);
            if (file.exists() && isCorrectFileType(file.getName())) {
                files.add(file);
            }
        }
        return files;
    }
}



