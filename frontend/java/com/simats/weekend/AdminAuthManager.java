package com.simats.weekend;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminAuthManager {

    private static final String PREFS_NAME = "AdminAuthPrefs";
    private static final String KEY_PASSWORD = "admin_password";
    private static final String DEFAULT_PASSWORD = "admin123";

    // Method to save the new password
    public static void savePassword(Context context, String newPassword) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_PASSWORD, newPassword);
        editor.apply();
    }

    // Method to get the saved password
    public static String getPassword(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Return the saved password, or the default "admin123" if none is found
        return prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD);
    }
}