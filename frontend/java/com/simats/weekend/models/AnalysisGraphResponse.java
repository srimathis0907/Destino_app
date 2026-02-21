package com.simats.weekend.models;

import com.google.gson.JsonElement;
import java.util.List;

public class AnalysisGraphResponse {
    private boolean status;
    private List<JsonElement> data; // Use generic JsonElement to handle different chart data structures

    public boolean isStatus() { return status; }
    public List<JsonElement> getData() { return data; }
}