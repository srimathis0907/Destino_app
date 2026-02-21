package com.simats.weekend.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.FilterDistanceBottomSheet;
import com.simats.weekend.R;
import com.simats.weekend.models.NearbyPlace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NearbyPlaceAdapter extends RecyclerView.Adapter<NearbyPlaceAdapter.PlaceViewHolder> {

    private final List<NearbyPlace> originalPlaces;
    private List<NearbyPlace> filteredPlaces;
    private final Context context;
    private final Location userLocation;

    public NearbyPlaceAdapter(Context context, List<NearbyPlace> places, double userLat, double userLng) {
        this.context = context;
        this.originalPlaces = new ArrayList<>(places);
        this.filteredPlaces = new ArrayList<>(places);
        this.userLocation = new Location("");
        this.userLocation.setLatitude(userLat);
        this.userLocation.setLongitude(userLng);
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_nearby_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        NearbyPlace place = filteredPlaces.get(position);
        holder.placeNameTextView.setText(place.getName());

        Location placeLocation = new Location("");
        placeLocation.setLatitude(place.getLatitude());
        placeLocation.setLongitude(place.getLongitude());
        float distanceInMeters = userLocation.distanceTo(placeLocation);
        float distanceInKm = distanceInMeters / 1000;
        holder.distanceTextView.setText(String.format(Locale.getDefault(), "%.1f km away", distanceInKm));


        holder.viewOnMapButton.setOnClickListener(v -> {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    place.getLatitude(), place.getLongitude(), place.getLatitude(), place.getLongitude(), Uri.encode(place.getName()));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredPlaces.size();
    }

    public void filter(int filterOption) {
        filteredPlaces.clear();
        if (filterOption == FilterDistanceBottomSheet.FILTER_ALL) {
            filteredPlaces.addAll(originalPlaces);
        } else {
            for (NearbyPlace place : originalPlaces) {
                Location placeLocation = new Location("");
                placeLocation.setLatitude(place.getLatitude());
                placeLocation.setLongitude(place.getLongitude());
                float distanceInKm = userLocation.distanceTo(placeLocation) / 1000;

                boolean shouldAdd = false;
                switch (filterOption) {
                    case FilterDistanceBottomSheet.FILTER_1KM:
                        if (distanceInKm < 1) shouldAdd = true;
                        break;
                    case FilterDistanceBottomSheet.FILTER_3KM:
                        if (distanceInKm >= 1 && distanceInKm <= 3) shouldAdd = true;
                        break;
                    case FilterDistanceBottomSheet.FILTER_OVER_3KM:
                        if (distanceInKm > 3) shouldAdd = true;
                        break;
                }
                if (shouldAdd) {
                    filteredPlaces.add(place);
                }
            }
        }
        notifyDataSetChanged();
    }


    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTextView;
        TextView distanceTextView;
        Button viewOnMapButton;

        PlaceViewHolder(View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.place_name_text_view);
            distanceTextView = itemView.findViewById(R.id.distance_text_view);
            viewOnMapButton = itemView.findViewById(R.id.view_on_map_button);
        }
    }
}