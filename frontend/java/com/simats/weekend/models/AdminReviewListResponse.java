package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdminReviewListResponse {
    @SerializedName("error")
    public boolean error;
    @SerializedName("reviews")
    public List<AdminReview> reviews;
}