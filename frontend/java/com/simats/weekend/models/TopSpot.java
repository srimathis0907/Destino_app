package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TopSpot implements Serializable {

// In your TopSpot.java file

    // In your TopSpot.java file
    @SerializedName("name") // Change "spot_name" to "name"
    private String name;

    @SerializedName("description") // Change "spot_description" to "description"
    private String description;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    // --- Getters ---
    // Renamed to getName() and getDescription() to fix the error and for consistency
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}