package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class PopularPlaceStat {
    @SerializedName("place_name")
    public String placeName;
    @SerializedName("trip_count")
    public int tripCount;
}