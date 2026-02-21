package com.simats.weekend.models;

import java.io.Serializable;

public class TransportResult implements Serializable {
    private String name;
    private String time;
    private double price;

    public TransportResult(String name, String time, double price) {
        this.name = name;
        this.time = time;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }
}