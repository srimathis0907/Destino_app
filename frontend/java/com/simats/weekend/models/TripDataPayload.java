// Create new file: TripDataPayload.java
package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TripDataPayload {
    // These names match the PHP script's expectations
    @SerializedName("user_id")
    private int userId;
    @SerializedName("place_id")
    private int placeId;
    @SerializedName("place_name")
    private String placeName;
    @SerializedName("place_location")
    private String placeLocation;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("num_people")
    private int numPeople;
    @SerializedName("num_days")
    private int numDays;
    @SerializedName("transport_cost")
    private double transportCost;
    @SerializedName("food_cost")
    private double foodCost;
    @SerializedName("hotel_cost")
    private double hotelCost;
    @SerializedName("other_cost")
    private double otherCost;
    @SerializedName("total_budget")
    private double totalBudget;
    @SerializedName("itinerary_data")
    private List<ItineraryDay> itineraryData;

    // Constructor to build the payload
    public TripDataPayload(int userId, int placeId, String placeName, String placeLocation,
                           String startDate, String endDate, int numPeople, int numDays,
                           double transportCost, double foodCost, double hotelCost,
                           double otherCost, double totalBudget, List<ItineraryDay> itineraryData) {
        this.userId = userId;
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeLocation = placeLocation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numPeople = numPeople;
        this.numDays = numDays;
        this.transportCost = transportCost;
        this.foodCost = foodCost;
        this.hotelCost = hotelCost;
        this.otherCost = otherCost;
        this.totalBudget = totalBudget;
        this.itineraryData = itineraryData;
    }
}