package com.simats.weekend
        ;

import android.content.Context;
import android.content.SharedPreferences;

public class FolderPrefManager {
    private static final String PREFS_NAME = "FolderPrefs";
    private static final String KEY_LAST_FOLDER = "last_selected_folder";

    public static void setLastSelectedFolder(Context context, String folderName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_FOLDER, folderName);
        editor.apply();
    }

    public static String getLastSelectedFolder(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LAST_FOLDER, null);
    }
}