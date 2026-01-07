package com.simats.weekend.models;

// This is a new response wrapper specifically for the user view.
// It is designed to work with your existing standalone 'PlaceDetails' model.
public class UPlaceDetailsResponse {
    private boolean status;
    private String message;
    private PlaceDetails data; // This references your original 'com.example.weekend.models.PlaceDetails'

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public PlaceDetails getData() {
        return data;
    }
}