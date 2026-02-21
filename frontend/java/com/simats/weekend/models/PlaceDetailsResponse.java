package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceDetailsResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PlaceDetails data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public PlaceDetails getData() { return data; }

    public static class PlaceDetails {
        private int id;
        private String name;
        private String location;
        @SerializedName("suitable_months")
        private String suitableMonths;
        @SerializedName("is_monsoon_destination")
        private int isMonsoonDestination;
        private double latitude;
        private double longitude;
        @SerializedName("toll_cost")
        private double tollCost;
        @SerializedName("parking_cost")
        private double parkingCost;
        @SerializedName("hotel_high_cost")
        private double hotelHighCost;
        @SerializedName("hotel_std_cost")
        private double hotelStdCost;
        @SerializedName("hotel_low_cost")
        private double hotelLowCost;
        @SerializedName("food_std_veg")
        private double foodStdVeg;
        @SerializedName("food_std_nonveg")
        private double foodStdNonveg;
        @SerializedName("food_std_combo")
        private double foodStdCombo;
        @SerializedName("food_high_veg")
        private double foodHighVeg;
        @SerializedName("food_high_nonveg")
        private double foodHighNonveg;
        @SerializedName("food_high_combo")
        private double foodHighCombo;
        @SerializedName("food_low_veg")
        private double foodLowVeg;
        @SerializedName("food_low_nonveg")
        private double foodLowNonveg;
        @SerializedName("food_low_combo")
        private double foodLowCombo;
        @SerializedName("avg_budget")
        private String avgBudget;
        @SerializedName("local_language")
        private String localLanguage;

        private List<PlaceImage> images;
        private List<TopSpot> spots;
        @SerializedName("transport_options")
        private List<TransportOption> transportOptions;

        // Getters for all fields
        public int getId() { return id; }
        public String getName() { return name; }
        public String getLocation() { return location; }
        public String getSuitableMonths() { return suitableMonths; }
        public int isMonsoonDestination() { return isMonsoonDestination; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public double getTollCost() { return tollCost; }
        public double getParkingCost() { return parkingCost; }
        public double getHotelHighCost() { return hotelHighCost; }
        public double getHotelStdCost() { return hotelStdCost; }
        public double getHotelLowCost() { return hotelLowCost; }
        public double getFoodStdVeg() { return foodStdVeg; }
        public double getFoodStdNonveg() { return foodStdNonveg; }
        public double getFoodStdCombo() { return foodStdCombo; }
        public double getFoodHighVeg() { return foodHighVeg; }
        public double getFoodHighNonveg() { return foodHighNonveg; }
        public double getFoodHighCombo() { return foodHighCombo; }
        public double getFoodLowVeg() { return foodLowVeg; }
        public double getFoodLowNonveg() { return foodLowNonveg; }
        public double getFoodLowCombo() { return foodLowCombo; }
        public String getAvgBudget() { return avgBudget; }
        public String getLocalLanguage() { return localLanguage; }
        public List<PlaceImage> getImages() { return images; }
        public List<TopSpot> getSpots() { return spots; }
        public List<TransportOption> getTransportOptions() { return transportOptions; }
    }

    public static class PlaceImage {
        private int id;
        @SerializedName("image_url")
        private String imageUrl;
        public int getId() { return id; }
        public String getImageUrl() { return imageUrl; }
    }

    public static class TopSpot {
        private int id;
        private String name;
        private String description;
        private double latitude;
        private double longitude;
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    public static class TransportOption {
        private int id;
        private String icon;
        private String type;
        private String info;
        public int getId() { return id; }
        public String getIcon() { return icon; }
        public String getType() { return type; }
        public String getInfo() { return info; }
    }
}