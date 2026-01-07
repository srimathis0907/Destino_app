package com.simats.weekend.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.AdminTripDetailsActivity;
import com.simats.weekend.R;
import com.simats.weekend.models.AdminTrip;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminTripAdapter extends RecyclerView.Adapter<AdminTripAdapter.TripViewHolder> {

    private Context context;
    private List<AdminTrip> tripList;

    public AdminTripAdapter(Context context, List<AdminTrip> tripList) {
        this.context = context;
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        AdminTrip trip = tripList.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public void filterList(List<AdminTrip> filteredList) {
        tripList = filteredList;
        notifyDataSetChanged();
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripName, tvUserName, tvDate, tvStatus, tvLocation;
        Button btnViewDetails;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripName = itemView.findViewById(R.id.tv_trip_name);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_trip_date);
            tvStatus = itemView.findViewById(R.id.tv_trip_status);
            tvLocation = itemView.findViewById(R.id.tv_location);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }

        void bind(AdminTrip trip) {
            tvTripName.setText(trip.placeName);
            tvUserName.setText(trip.userName);

            // Use the dynamic location from the model
            if (trip.placeLocation != null && !trip.placeLocation.isEmpty()) {
                tvLocation.setText(trip.placeLocation);
            } else {
                tvLocation.setText("Location not available"); // Fallback text
            }

            String dateRange = formatDate(trip.startDate) + " - " + formatDate(trip.endDate);
            tvDate.setText(dateRange);

            // Null-safe status handling
            String statusRaw = trip.status == null ? "" : trip.status.trim().toLowerCase(Locale.ROOT);

            switch (statusRaw) {
                case "future":
                    tvStatus.setText("Upcoming");
                    // upcoming drawable starting with 'a'
                    tvStatus.setBackgroundResource(R.drawable.a_status_bg_upcoming);
                    break;
                case "active":
                case "ongoing":
                    tvStatus.setText("Upcoming");
                    // use active drawable for currently active/ongoing trips
                    tvStatus.setBackgroundResource(R.drawable.a_status_bg_active);
                    break;
                case "finished":
                case "completed":
                    tvStatus.setText("Completed");
                    // completed drawable starting with 'a'
                    tvStatus.setBackgroundResource(R.drawable.a_status_bg_completed);
                    break;
                case "cancelled":
                case "canceled": // handle American spelling if present
                    tvStatus.setText("Cancelled");
                    // cancelled drawable starting with 'a'
                    tvStatus.setBackgroundResource(R.drawable.a_status_bg_cancelled);
                    break;
                default:
                    // fallback: show raw status if present, and use existing default bg
                    if (!statusRaw.isEmpty()) {
                        // Capitalize first letter for display
                        tvStatus.setText(statusRaw.substring(0, 1).toUpperCase(Locale.ROOT) + statusRaw.substring(1));
                    } else {
                        tvStatus.setText("Unknown");
                    }
                    tvStatus.setBackgroundResource(R.drawable.status_bg_default);
                    break;
            }

            btnViewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminTripDetailsActivity.class);
                intent.putExtra("TRIP_DETAILS", trip);
                context.startActivity(intent);
            });
        }

        private String formatDate(String dateStr) {
            if (dateStr == null) return "";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            try {
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return dateStr;
            }
        }
    }
}
