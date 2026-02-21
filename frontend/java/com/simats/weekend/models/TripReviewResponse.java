package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TripReviewResponse {
    @SerializedName("error")
    public boolean error;
    @SerializedName("reviews")
    public List<Review> reviews;
}