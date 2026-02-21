package com.simats.weekend.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Replace the content of your existing ItineraryDay.java with this
public class ItineraryDay implements Serializable {
    private String dateLabel;
    private List<ItinerarySpot> plannedSpots; // Changed from List<TopSpot>

    public ItineraryDay(String dateLabel) {
        this.dateLabel = dateLabel;
        this.plannedSpots = new ArrayList<>();
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public List<ItinerarySpot> getPlannedSpots() {
        return plannedSpots;
    }

    public void addSpot(TopSpot spot) {
        this.plannedSpots.add(new ItinerarySpot(spot));
    }
}