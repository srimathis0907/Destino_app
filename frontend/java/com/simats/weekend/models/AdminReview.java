package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class AdminReview {
    @SerializedName("rating")
    public float rating;

    @SerializedName("review_text")
    public String reviewText;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("place_name")
    public String placeName;

    @SerializedName("place_location")
    public String placeLocation;

    @SerializedName("created_at")
    public Date createdAt; // ✅ Changed from String → Date
    // Inside the AdminReview.java class

    @SerializedName("full_location") // This MUST match the key from the PHP JSON
    public String fullLocation;
}
