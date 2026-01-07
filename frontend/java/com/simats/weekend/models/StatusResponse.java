package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class StatusResponse {

    // Using the Boolean object wrapper allows the value to be null if not present in the JSON
    @SerializedName("status")
    private Boolean status;

    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    /**
     * This is the updated, backward-compatible method.
     * It intelligently checks for success from both old and new API responses.
     * OLD SUCCESS: "status": true
     * NEW SUCCESS: "error": false
     * @return true if the API call was successful, false otherwise.
     */
    public boolean isStatus() {
        // Handle the old response format ("status": true)
        if (status != null) {
            return status;
        }
        // Handle the new response format ("error": false)
        if (error != null) {
            return !error; // Success is when 'error' is false
        }
        // If neither key is present, default to failure.
        return false;
    }

    public String getMessage() {
        return message;
    }
}