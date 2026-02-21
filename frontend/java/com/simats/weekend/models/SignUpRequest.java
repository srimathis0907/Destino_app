package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class SignUpRequest {

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public SignUpRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
}