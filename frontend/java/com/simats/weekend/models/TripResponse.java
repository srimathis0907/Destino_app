package com.simats.weekend.models;

import java.util.List;

public class TripResponse {
    private boolean status;
    private List<Trip> data;

    public boolean isStatus() {
        return status;
    }

    public List<Trip> getData() {
        return data;
    }
}