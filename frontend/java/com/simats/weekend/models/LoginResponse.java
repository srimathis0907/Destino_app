package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    // UPDATED to use AppUser
    @SerializedName("user")
    private AppUser user;

    // --- Getter Methods ---

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    // UPDATED to use AppUser
    public AppUser getUser() {
        return user;
    }
}