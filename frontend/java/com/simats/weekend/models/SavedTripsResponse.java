package com.simats.weekend.models;

import java.util.List;

public class SavedTripsResponse {
    private boolean status;
    private List<SavedTrip> data;

    public boolean isStatus() { return status; }
    public List<SavedTrip> getData() { return data; }
}