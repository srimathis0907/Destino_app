package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AdminTrip implements Serializable {
    @SerializedName("trip_id")
    public int tripId;
    @SerializedName("place_name")
    public String placeName;

    // NEW FIELD to hold the correct location
    @SerializedName("place_location")
    public String placeLocation;

    @SerializedName("user_name")
    public String userName;
    @SerializedName("start_date")
    public String startDate;
    @SerializedName("end_date")
    public String endDate;
    @SerializedName("num_people")
    public int numPeople;
    @SerializedName("num_days")
    public int numDays;
    @SerializedName("total_budget")
    public float totalBudget;
    @SerializedName("status")
    public String status;
}