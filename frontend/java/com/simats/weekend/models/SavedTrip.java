package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SavedTrip implements Serializable {
    private int id;
    private String name;
    private String location;
    @SerializedName("image_url")
    private String imageUrl;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
}