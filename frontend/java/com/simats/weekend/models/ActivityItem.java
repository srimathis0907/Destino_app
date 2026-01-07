package com.simats.weekend.models;

public class ActivityItem {
    public enum ActivityType { FAVORITE, REVIEW, NEW_TRIP, CANCELLED, UPLOAD, PASSWORD }

    public ActivityType type;
    public String title;
    public String subtitle;
    public String rating; // Only for reviews

    public ActivityItem(ActivityType type, String title, String subtitle, String rating) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.rating = rating;
    }
}