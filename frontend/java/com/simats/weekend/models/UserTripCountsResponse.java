package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class UserTripCountsResponse {
    @SerializedName("error")
    public boolean error;

    @SerializedName("completed_trips")
    public int completedTrips;

    @SerializedName("ongoing_trips")
    public int ongoingTrips;

    @SerializedName("cancelled_trips")
    public int cancelledTrips;
    // Inside the UserTripCountsResponse.java class

    @SerializedName("future_trips") // This MUST match the key from the PHP JSON
    public int futureTrips;
}