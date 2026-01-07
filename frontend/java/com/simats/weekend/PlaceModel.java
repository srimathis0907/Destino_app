package com.simats.weekend;

import com.simats.weekend.models.Place;
import java.util.ArrayList;
import java.util.List;

public class PlaceModel implements ExploreContract.Model {
    @Override
    public List<Place> getPlaces() {
        List<Place> places = new ArrayList<>();

        // UPDATED: Added sample latitude and longitude to match the Place constructor
        places.add(new Place("Goa Beaches", "Best in November", R.drawable.goa, 15.35, 73.95));
        places.add(new Place("Shimla Mountains", "Best in December", R.drawable.moutain, 31.10, 77.17));
        places.add(new Place("Jaipur Palaces", "Best in February", R.drawable.jaipur, 26.91, 75.78));
        places.add(new Place("Kerala Backwaters", "Best in August", R.drawable.kerala, 9.93, 76.26));
        places.add(new Place("Mumbai City Life", "Best in October", R.drawable.mumbai, 19.07, 72.87));

        return places;
    }
}