package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Implement Serializable to pass this object between activities
public class User implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("fullname")
    public String fullname;

    @SerializedName("email")
    public String email;

    @SerializedName("status")
    public String status; // "active" or "blocked"

    // Helper method to generate initials from the full name
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (fullname != null && !fullname.isEmpty()) {
            for (String s : fullname.split(" ")) {
                if (!s.isEmpty()) {
                    initials.append(s.charAt(0));
                }
            }
        }
        return initials.toString().toUpperCase();
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
}