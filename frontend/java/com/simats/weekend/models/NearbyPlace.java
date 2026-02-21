package com.simats.weekend.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class NearbyPlace implements Parcelable {

    @SerializedName("name")
    private String name;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    // Parcelable constructor
    protected NearbyPlace(Parcel in) {
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<NearbyPlace> CREATOR = new Creator<NearbyPlace>() {
        @Override
        public NearbyPlace createFromParcel(Parcel in) {
            return new NearbyPlace(in);
        }

        @Override
        public NearbyPlace[] newArray(int size) {
            return new NearbyPlace[size];
        }
    };

    // Getters
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}