package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

// No changes needed in this file
public class Trip implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("place_name")
    private String placeName;
    @SerializedName("place_location")
    private String placeLocation;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("status")
    private String status;
    @SerializedName("place_image")
    private String placeImage;
    @SerializedName("transport_cost")
    private double transportCost;
    @SerializedName("food_cost")
    private double foodCost;
    @SerializedName("hotel_cost")
    private double hotelCost;
    @SerializedName("num_people")
    private int numPeople;
    @SerializedName("num_days")
    private int numDays;
    @SerializedName("other_cost")
    private double otherCost;
    @SerializedName("total_budget")
    private double totalBudget;
    @SerializedName("place_id")
    private int placeId;
    @SerializedName("transport_type")
    private String transportType;
    @SerializedName("has_user_reviewed")
    private boolean hasUserReviewed;
    @SerializedName("media_folder")
    private String mediaFolder;
    @SerializedName("itinerary_details")
    private List<ItineraryItem> itineraryDetails;
    @SerializedName("is_today")
    private boolean isToday;

    public Trip() {}

    // --- Getters ---
    public int getId() { return id; }
    public String getPlaceName() { return placeName; }
    public String getPlaceLocation() { return placeLocation; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public String getPlaceImage() { return placeImage; }
    public double getTransportCost() { return transportCost; }
    public double getFoodCost() { return foodCost; }
    public double getHotelCost() { return hotelCost; }
    public int getNumPeople() { return numPeople; }
    public int getNumDays() { return numDays; }
    public double getOtherCost() { return otherCost; }
    public double getTotalBudget() { return totalBudget; }
    public int getPlaceId() { return placeId; }
    public String getTransportType() { return transportType; }
    public boolean hasUserReviewed() { return hasUserReviewed; }
    public String getMediaFolder() { return mediaFolder; }
    public List<ItineraryItem> getItineraryDetails() { return itineraryDetails; }
    public boolean isToday() { return isToday; }

    public String getDate() {
        if (startDate != null && endDate != null) {
            return startDate + " - " + endDate;
        } else if (startDate != null) {
            return startDate;
        } else if (endDate != null) {
            return endDate;
        } else {
            return "";
        }
    }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public void setPlaceLocation(String placeLocation) { this.placeLocation = placeLocation; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setStatus(String status) { this.status = status; }
    public void setMediaFolder(String mediaFolder) { this.mediaFolder = mediaFolder; }
    public void setPlaceImage(String placeImage) { this.placeImage = placeImage; }

    // Inner class for Itinerary Items
    public static class ItineraryItem implements Serializable {
        @SerializedName("day_number")
        private int dayNumber;
        @SerializedName("item_name")
        private String itemName;
        @SerializedName("item_type")
        private String itemType;
        @SerializedName("parent_spot_name")
        private String parentSpotName;

        public int getDayNumber() { return dayNumber; }
        public String getItemName() { return itemName; }
        public String getItemType() { return itemType; }
        public String getParentSpotName() { return parentSpotName; }
    }
}