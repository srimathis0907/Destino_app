package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    private boolean status;
    private String message;
    private User data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public User getData() { return data; }

    public static class User {
        private int id;
        private String fullname;
        private String username;
        private String email;
        private String phone;
        @SerializedName("profile_image")
        private String profileImage;

        public int getId() { return id; }
        public String getFullname() { return fullname; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getProfileImage() { return profileImage; }
    }
}