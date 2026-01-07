package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrainSearchResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<TrainData> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<TrainData> getData() {
        return data;
    }

    // This class represents a single train in the list
    public static class TrainData {
        @SerializedName("train_name")
        private String trainName;

        @SerializedName("train_number")
        private String trainNumber;

        @SerializedName("from_std")
        private String departureTime; // "from_std" is the departure time

        @SerializedName("to_sta")
        private String arrivalTime; // "to_sta" is the arrival time

        @SerializedName("duration")
        private String duration;

        @SerializedName("distance")
        private int distance;

        public String getTrainName() {
            return trainName;
        }

        public String getTrainNumber() {
            return trainNumber;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public String getDuration() {
            return duration;
        }

        public int getDistance() {
            return distance;
        }
    }
}