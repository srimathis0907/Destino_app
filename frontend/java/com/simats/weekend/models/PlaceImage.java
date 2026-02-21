package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PlaceImage implements Serializable {

    private int id;

    @SerializedName("image_url")
    private String imageUrl;

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}