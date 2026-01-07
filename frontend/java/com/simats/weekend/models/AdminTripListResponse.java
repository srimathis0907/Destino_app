package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdminTripListResponse {
    @SerializedName("error")
    public boolean error;
    @SerializedName("trips")
    public List<AdminTrip> trips;
}