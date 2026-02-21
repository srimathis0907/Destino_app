package com.simats.weekend.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItinerarySpot implements Serializable {
    private TopSpot topSpot;
    private List<NearbyPlace> selectedNearbyPlaces;

    public ItinerarySpot(TopSpot topSpot) {
        this.topSpot = topSpot;
        this.selectedNearbyPlaces = new ArrayList<>();
    }

    public TopSpot getTopSpot() {
        return topSpot;
    }

    public List<NearbyPlace> getSelectedNearbyPlaces() {
        return selectedNearbyPlaces;
    }

    public void addSelectedNearbyPlace(NearbyPlace place) {
        // Simple logic: allow one hotel, one restaurant, etc.
        // You can make this more complex later.
        selectedNearbyPlaces.add(place);
    }
}