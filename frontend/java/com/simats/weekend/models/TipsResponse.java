package com.simats.weekend.models;

import java.util.List;

public class TipsResponse {
    private boolean status;
    private List<TravelTip> data;

    public boolean isStatus() { return status; }
    public List<TravelTip> getData() { return data; }
}