package com.simats.weekend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Context context;

    private static final String PREF_NAME = "WeekendAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_EMAIL = "userEmail";
    public static final String KEY_USER_FULLNAME = "userFullname";
    private static final String KEY_AUTH_TOKEN = "authToken";

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Creates a login session for the user.
     * @param userId The unique ID of the user from the database.
     * @param email The user's email.
     * @param fullname The user's full name.
     * @param token The authentication token.
     */
    public void createLoginSession(int userId, String email, String fullname, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_FULLNAME, fullname);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public boolean checkLogin(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return false;
        }
        return true;
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USER_ID, String.valueOf(prefs.getInt(KEY_USER_ID, -1)));
        user.put(KEY_USER_EMAIL, prefs.getString(KEY_USER_EMAIL, null));
        user.put(KEY_USER_FULLNAME, prefs.getString(KEY_USER_FULLNAME, null));
        return user;
    }

    /**
     * Gets the logged-in user's ID.
     * @return User ID if logged in, otherwise -1.
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public void logoutUser(){
        editor.clear();
        editor.apply();

        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public boolean isLoggedIn(){
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}