package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class AppUser {

    @SerializedName("id")
    private int id;

    @SerializedName("fullname")
    private String fullname;

    @SerializedName("email")
    private String email;

    // --- Getter Methods ---

    public int getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }
}