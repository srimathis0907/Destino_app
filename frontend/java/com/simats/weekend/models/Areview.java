package com.simats.weekend.models;

import java.util.Date;

public class Areview {
    private final String author;
    private final String comment;
    private final float rating;
    private final Date date;

    public Areview(String author, String comment, float rating, Date date) {
        this.author = author;
        this.comment = comment;
        this.rating = rating;
        this.date = date;
    }

    public String getAuthor() { return author; }
    public String getComment() { return comment; }
    public float getRating() { return rating; }
    public Date getDate() { return date; }
}