package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class AdminPlace {
    // These fields match the JSON keys from your get_all_places.php script
    private int id;
    private String name;

    // The @SerializedName annotation tells Gson to map the JSON key "image_url"
    // to this Java field named "imageUrl".
    @SerializedName("image_url")
    private String imageUrl;

    // --- Getters to access the private fields ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}