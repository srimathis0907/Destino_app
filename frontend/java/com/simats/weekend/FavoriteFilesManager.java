package com.simats.weekend;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton to manage favorite file paths using SharedPreferences.
 */
public class FavoriteFilesManager {
    private static final String PREFS_NAME = "FavoriteFilesPrefs";
    private static final String KEY_FAVORITES = "favorite_file_paths";
    private static FavoriteFilesManager instance;
    private final SharedPreferences sharedPreferences;

    private FavoriteFilesManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FavoriteFilesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteFilesManager(context);
        }
        return instance;
    }

    private Set<String> getFavoritesSet() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    public void addFavorite(String filePath) {
        Set<String> favorites = getFavoritesSet();
        favorites.add(filePath);
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public void removeFavorite(String filePath) {
        Set<String> favorites = getFavoritesSet();
        favorites.remove(filePath);
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public boolean isFavorite(String filePath) {
        return getFavoritesSet().contains(filePath);
    }

    public Set<String> getAllFavorites() {
        return getFavoritesSet();
    }
}