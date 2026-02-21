package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NearbyPlacesResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<NearbyPlace> data;

    // Getters
    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<NearbyPlace> getData() {
        return data;
    }
}