package com.simats.weekend.models;

// NOTE: This was previously named MemoryFolder, ensure the file and class name is FolderItem
public class FolderItem {
    private String name;
    private int itemCount; // The missing field
    private boolean isFavorite;

    // --- UPDATED CONSTRUCTOR ---
    public FolderItem(String name, int itemCount, boolean isFavorite) {
        this.name = name;
        this.itemCount = itemCount; // Now accepts item count
        this.isFavorite = isFavorite;
    }

    // Constructor without item count for compatibility, if needed elsewhere
    public FolderItem(String name, boolean isFavorite) {
        this.name = name;
        this.itemCount = 0; // Default to 0
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getItemCount() { return itemCount; } // Getter for the new field
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}