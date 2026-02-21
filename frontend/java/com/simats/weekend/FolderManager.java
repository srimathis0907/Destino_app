package com.simats.weekend;

import com.simats.weekend.models.FolderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FolderManager {
    private static FolderManager instance;
    private List<FolderItem> allFolders;

    private FolderManager() {
        allFolders = new ArrayList<>();
        // The list starts empty. Folders are only added by the user.
    }

    public static synchronized FolderManager getInstance() {
        if (instance == null) {
            instance = new FolderManager();
        }
        return instance;
    }

    public List<FolderItem> getAllFolders() {
        return allFolders;
    }

    public List<FolderItem> getFavoriteFolders() {
        return allFolders.stream().filter(FolderItem::isFavorite).collect(Collectors.toList());
    }

    public void addFolder(String folderName) {
        boolean exists = allFolders.stream().anyMatch(folder -> folder.getName().equalsIgnoreCase(folderName));
        if (!exists) {
            allFolders.add(0, new FolderItem(folderName, 1, false));
        }
    }

    public void renameFolder(FolderItem folder, String newName) {
        folder.setName(newName);
    }

    public void deleteFolder(FolderItem folder) {
        allFolders.remove(folder);
    }

    public void toggleFavorite(FolderItem folder) {
        folder.setFavorite(!folder.isFavorite());
    }
}