package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class AnalysisStatsResponse {
    private boolean status;
    private StatsData data;

    public boolean isStatus() { return status; }
    public StatsData getData() { return data; }

    public static class StatsData {
        @SerializedName("total_users")
        private int totalUsers;
        @SerializedName("ongoing_trips")
        private int ongoingTrips;
        @SerializedName("cancellations")
        private int cancellations;
        @SerializedName("avg_completed_cost")
        private double avgCompletedCost;

        public int getTotalUsers() { return totalUsers; }
        public int getOngoingTrips() { return ongoingTrips; }
        public int getCancellations() { return cancellations; }
        public double getAvgCompletedCost() { return avgCompletedCost; }
    }
}