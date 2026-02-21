package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class PlaceDetails implements Serializable {

    private int id;
    private String name;
    private String location;
    private double latitude;
    private double longitude;

    @SerializedName("airport_code")
    private String airportCode;

    @SerializedName("suitable_months")
    private String suitableMonths;

    @SerializedName("avg_budget")
    private String avgBudget;

    @SerializedName("local_language")
    private String localLanguage;

    @SerializedName("toll_cost")
    private double tollCost;
    @SerializedName("parking_cost")
    private double parkingCost;
    @SerializedName("hotel_std_cost")
    private double hotelStdCost;
    @SerializedName("hotel_high_cost")
    private double hotelHighCost;
    @SerializedName("hotel_low_cost")
    private double hotelLowCost;
    @SerializedName("food_std_veg")
    private double foodStdVeg;
    @SerializedName("food_std_nonveg")
    private double foodStdNonVeg;
    @SerializedName("food_std_combo")
    private double foodStdCombo;
    @SerializedName("food_high_veg")
    private double foodHighVeg;
    @SerializedName("food_high_nonveg")
    private double foodHighNonVeg;
    @SerializedName("food_high_combo")
    private double foodHighCombo;
    @SerializedName("food_low_veg")
    private double foodLowVeg;
    @SerializedName("food_low_nonveg")
    private double foodLowNonVeg;
    @SerializedName("food_low_combo")
    private double foodLowCombo;

    // --- START: UPDATED PART ---
    // Changed from List<String> to List<PlaceImage> to match the JSON structure
    private List<PlaceImage> images;
    // --- END: UPDATED PART ---

    @SerializedName("top_spots")
    private List<TopSpot> topSpots;
    @SerializedName("transport_options")
    private List<Transport> transportOptions;
    @SerializedName("averageRating")
    private float averageRating;
    @SerializedName("reviewCount")
    private int reviewCount;
    @SerializedName("flight_example")
    private String flightExample;
    @SerializedName("train_example")
    private String trainExample;
    @SerializedName("bus_example")
    private String busExample;
    private double distance;

    // --- GETTER METHODS ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAirportCode() { return airportCode; }
    public String getSuitableMonths() { return suitableMonths; }
    public String getAvgBudget() { return avgBudget; }
    public String getLocalLanguage() { return localLanguage; }
    public double getHotelStdCost() { return hotelStdCost; }
    public double getHotelHighCost() { return hotelHighCost; }
    public double getHotelLowCost() { return hotelLowCost; }
    public double getTollCost() { return tollCost; }
    public double getParkingCost() { return parkingCost; }

    // --- START: UPDATED PART ---
    // The getter method is also updated to return the new list type
    public List<PlaceImage> getImages() { return images; }
    // --- END: UPDATED PART ---

    public List<TopSpot> getTopSpots() { return topSpots; }
    public List<Transport> getTransportOptions() { return transportOptions; }
    public String getFlightExample() { return flightExample; }
    public String getTrainExample() { return trainExample; }
    public String getBusExample() { return busExample; }
    public float getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public double getDistance() { return distance; }

    // --- HELPER METHODS ---
    public double getHotelCost(int tierIndex) {
        switch (tierIndex) {
            case 0: return hotelHighCost;
            case 2: return hotelLowCost;
            case 1: default: return hotelStdCost;
        }
    }

    public double getFoodCost(int tierIndex, int foodPrefIndex) {
        switch (tierIndex) {
            case 0:
                if (foodPrefIndex == 0) return foodHighVeg;
                if (foodPrefIndex == 1) return foodHighNonVeg;
                return foodHighCombo;
            case 2:
                if (foodPrefIndex == 0) return foodLowVeg;
                if (foodPrefIndex == 1) return foodLowNonVeg;
                return foodLowCombo;
            case 1: default:
                if (foodPrefIndex == 0) return foodStdVeg;
                if (foodPrefIndex == 1) return foodStdNonVeg;
                return foodStdCombo;
        }
    }
}