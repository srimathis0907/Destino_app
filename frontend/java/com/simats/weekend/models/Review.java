package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("category")
    public String category;

    @SerializedName("rating")
    public float rating;

    @SerializedName("review_text")
    public String reviewText;

    // ADD THIS NEW FIELD
    @SerializedName("user_name")
    public String userName;

    public Review(String category, float rating, String reviewText) {
        this.category = category;
        this.rating = rating;
        this.reviewText = reviewText;
    }
}