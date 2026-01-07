package com.simats.weekend.models;

public class ExploreDestination {
    private String title;
    private String location;
    private String temperature;
    private String duration;
    private String bestTime;
    private int imageResId;

    public ExploreDestination(String title, String location, String temperature, String duration, String bestTime, int imageResId) {
        this.title = title;
        this.location = location;
        this.temperature = temperature;
        this.duration = duration;
        this.bestTime = bestTime;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getTemperature() { return temperature; }
    public String getDuration() { return duration; }
    public String getBestTime() { return bestTime; }
    public int getImageResId() { return imageResId; }
}
