package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserListResponse {
    @SerializedName("error")
    public boolean error;

    @SerializedName("users")
    public List<User> users;
}