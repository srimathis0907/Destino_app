package com.simats.weekend.models;

import java.io.Serializable;

public class Vehicle implements Serializable {
    private String name;
    private int iconResId;
    private int quantity = 0;
    private double mileage = 0;
    private int fuelTypeIndex = 0;
    // ===============================================================
    // == NEW: FIELD TO STORE THE FUEL PRICE FOR THIS VEHICLE ==
    // ===============================================================
    private double fuelPrice = 0;


    public Vehicle(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getMileage() { return mileage; }
    public void setMileage(double mileage) { this.mileage = mileage; }
    public int getFuelTypeIndex() { return fuelTypeIndex; }
    public void setFuelTypeIndex(int fuelTypeIndex) { this.fuelTypeIndex = fuelTypeIndex; }

    // ===============================================================
    // == NEW: GETTER AND SETTER FOR THE FUEL PRICE ==
    // ===============================================================
    public double getFuelPrice() { return fuelPrice; }
    public void setFuelPrice(double fuelPrice) { this.fuelPrice = fuelPrice; }
}