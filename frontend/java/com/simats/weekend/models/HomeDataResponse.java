package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HomeDataResponse {

    @SerializedName("recommended_place")
    private Place recommendedPlace;

    @SerializedName("popular_places")
    private List<Place> popularPlaces;

    public Place getRecommendedPlace() {
        return recommendedPlace;
    }

    public List<Place> getPopularPlaces() {
        return popularPlaces;
    }
}