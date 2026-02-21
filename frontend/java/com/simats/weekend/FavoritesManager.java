package com.simats.weekend;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Singleton class to manage favorite folders using SharedPreferences.
 */
public class FavoritesManager {
    private static final String PREFS_NAME = "FavoriteFoldersPrefs";
    private static final String KEY_FAVORITES = "favorite_folder_names";
    private static FavoritesManager instance;
    private final SharedPreferences sharedPreferences;

    private FavoritesManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }

    private Set<String> getFavoritesSet() {
        // Return a new mutable set to avoid modification issues
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    public void addFavorite(String folderName) {
        Set<String> favorites = getFavoritesSet();
        favorites.add(folderName);
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public void removeFavorite(String folderName) {
        Set<String> favorites = getFavoritesSet();
        favorites.remove(folderName);
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public boolean isFavorite(String folderName) {
        return getFavoritesSet().contains(folderName);
    }

    public void renameFavorite(String oldName, String newName) {
        if (isFavorite(oldName)) {
            removeFavorite(oldName);
            addFavorite(newName);
        }
    }
}