package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class SignUpResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("user_id")
    private int userId;

    // --- Getter Methods ---

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }
}