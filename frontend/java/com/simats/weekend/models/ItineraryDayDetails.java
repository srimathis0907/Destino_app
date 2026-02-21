package com.simats.weekend.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItineraryDayDetails implements Serializable {
    private String dayLabel;
    private List<ItinerarySpotDetails> spots = new ArrayList<>();

    public ItineraryDayDetails(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public List<ItinerarySpotDetails> getSpots() {
        return spots;
    }

    public void addSpot(ItinerarySpotDetails spot) {
        this.spots.add(spot);
    }
}