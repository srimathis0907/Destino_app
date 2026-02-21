package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Place implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("location")
    private String location;

    @SerializedName("suitable_months")
    private String suitableMonths;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    // --- START: UPDATED PART ---
    // Changed from List<String> to List<PlaceImage>
    @SerializedName("images")
    private List<PlaceImage> images;
    // --- END: UPDATED PART ---

    private transient int imageResourceId;

    // --- CONSTRUCTORS ---
    public Place() {}

    public Place(String name, String bestTimeToVisit, int imageResourceId, double latitude, double longitude) {
        this.name = name;
        this.suitableMonths = bestTimeToVisit;
        this.imageResourceId = imageResourceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = 0;
        this.location = "Local";
        this.images = null;
    }

    // --- GETTER METHODS for ALL fields ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getSuitableMonths() { return suitableMonths; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getImageResourceId() { return imageResourceId; }

    // --- START: UPDATED PART ---
    // Updated the getter to return the new list type
    public List<PlaceImage> getImages() { return images; }

    // Updated this helper method to get the URL from the PlaceImage object
    public String getFirstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return null;
    }
    // --- END: UPDATED PART ---
}