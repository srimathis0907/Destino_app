package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    // --- Getter Methods ---

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}