package com.simats.weekend.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotosFragment extends BaseMediaFragment {

    @Override
    protected List<File> getMediaFiles() {
        List<File> files = new ArrayList<>();
        if (getContext() == null || folderName == null) return files;

        File folder = new File(getContext().getExternalFilesDir(null), "MyMemories/" + folderName);
        if (folder.exists() && folder.isDirectory()) {
            File[] allFiles = folder.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    @Override
    protected String getShareType() {
        return "image/*";
    }
}