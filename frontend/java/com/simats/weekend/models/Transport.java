package com.simats.weekend.models;

import android.content.Context;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Transport implements Serializable {
    // These names now match the JSON keys from get_place_details.php
    @SerializedName("icon")
    private String icon;

    @SerializedName("type")
    private String type;

    @SerializedName("info")
    private String info;

    // Getters
    public String getIcon() { return icon; }
    public String getType() { return type; }
    public String getInfo() { return info; }

    /**
     * ADDED: This method converts the icon name string (e.g., "ic_bus")
     * into an actual drawable resource ID that the app can use to display the image.
     *
     * @param context The context needed to access app resources.
     * @return The integer ID of the drawable resource.
     */
    public int getIconResourceId(Context context) {
        if (icon == null || icon.isEmpty()) {
            // Return a default icon if none is specified
            return context.getResources().getIdentifier("ic_public_transport", "drawable", context.getPackageName());
        }
        return context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
    }
}