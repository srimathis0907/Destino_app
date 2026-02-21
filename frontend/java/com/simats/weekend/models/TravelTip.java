package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TravelTip implements Serializable {
    private int id;
    @SerializedName("tip_title")
    private String title;
    @SerializedName("tip_content")
    private String content;

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    // Setters (useful for editing)
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}