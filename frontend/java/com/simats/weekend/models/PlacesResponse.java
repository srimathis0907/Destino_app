package com.simats.weekend.models;

import java.util.List;

public class PlacesResponse {
    private boolean status;
    private String message;
    private List<AdminPlace> data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public List<AdminPlace> getData() { return data; }
}