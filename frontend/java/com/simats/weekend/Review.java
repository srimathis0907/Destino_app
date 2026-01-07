package com.simats.weekend;

// This class represents the data model for a single review.
public class Review {
    String placeName;
    String userName;
    float rating;
    String date;
    String reviewText;

    public Review(String placeName, String userName, float rating, String date, String reviewText) {
        this.placeName = placeName;
        this.userName = userName;
        this.rating = rating;
        this.date = date;
        this.reviewText = reviewText;
    }
}
