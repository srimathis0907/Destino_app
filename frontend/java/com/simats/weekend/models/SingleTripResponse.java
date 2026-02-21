package com.simats.weekend.models;

import java.io.Serializable;

public class SingleTripResponse implements Serializable {
    private boolean status;
    private Trip data;

    public boolean isStatus() {
        return status;
    }

    public Trip getData() {
        return data;
    }
}