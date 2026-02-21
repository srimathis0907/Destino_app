package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class UserStatsResponse {
    @SerializedName("error")
    public boolean error;

    @SerializedName("total_users")
    public int totalUsers;

    @SerializedName("active_users")
    public int activeUsers;

    @SerializedName("blocked_users")
    public int blockedUsers;
}