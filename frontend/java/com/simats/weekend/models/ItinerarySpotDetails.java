package com.simats.weekend.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItinerarySpotDetails implements Serializable {
    private String spotName;
    private List<String> addons = new ArrayList<>();

    public ItinerarySpotDetails(String spotName) {
        this.spotName = spotName;
    }

    public String getSpotName() {
        return spotName;
    }

    public List<String> getAddons() {
        return addons;
    }

    public void addAddon(String addonName) {
        this.addons.add(addonName);
    }
}