package com.simats.weekend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.simats.weekend.models.Place;
public class ExplorePresenter implements ExploreContract.Presenter {

    private ExploreContract.View view;
    private final ExploreContract.Model model;
    private final List<Place> allPlaces;

    public ExplorePresenter(ExploreContract.View view, ExploreContract.Model model) {
        this.view = view;
        this.model = model;
        this.allPlaces = new ArrayList<>(model.getPlaces());
    }

    @Override
    public void loadPlaces() {
        if (view != null) {
            view.displayPlaces(allPlaces);
        }
    }

    @Override
    public void filterPlaces(String query) {
        if (view != null) {
            if (query.isEmpty()) {
                view.displayPlaces(allPlaces);
            } else {
                List<Place> filteredList = allPlaces.stream()
                        .filter(place -> place.getName().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
                view.displayPlaces(filteredList);
            }
        }
    }

    @Override
    public void onLocationButtonClicked() {
        if (view != null) {
            view.navigateToLocationSearch();
        }
    }

    @Override
    public void destroy() {
        view = null; // To avoid memory leaks
    }
}