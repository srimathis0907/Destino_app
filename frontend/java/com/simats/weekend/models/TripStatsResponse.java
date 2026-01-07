package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class TripStatsResponse {
    public boolean error;
    public String message;

    @SerializedName("future_trips") // Matches PHP JSON key
    public int futureTrips; // Java variable name

    @SerializedName("ongoing_trips")
    public int ongoingTrips;

    @SerializedName("completed_trips")
    public int completedTrips;

    @SerializedName("cancelled_trips")
    public int cancelledTrips;
}