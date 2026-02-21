package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PopularPlaceStatsResponse {
    @SerializedName("error")
    public boolean error;
    @SerializedName("stats")
    public List<PopularPlaceStat> stats;
}