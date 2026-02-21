package com.simats.weekend;

import com.simats.weekend.models.Place;

import java.util.List;

public interface ExploreContract {
    interface View {
        void displayPlaces(List<Place> places);
        void navigateToLocationSearch();
    }

    interface Presenter {
        void loadPlaces();
        void filterPlaces(String query);
        void onLocationButtonClicked();
        void destroy();
    }

    interface Model {
        List<Place> getPlaces();
    }
}