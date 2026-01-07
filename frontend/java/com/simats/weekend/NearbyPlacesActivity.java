package com.simats.weekend;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.adapters.NearbyPlaceAdapter;
import com.simats.weekend.models.NearbyPlace;

import java.util.ArrayList;

public class NearbyPlacesActivity extends AppCompatActivity implements FilterDistanceBottomSheet.FilterListener {

    private RecyclerView recyclerView;
    private NearbyPlaceAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_places);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        String title = getIntent().getStringExtra("CATEGORY_TITLE");
        ArrayList<NearbyPlace> places = getIntent().getParcelableArrayListExtra("NEARBY_PLACES");
        double userLat = getIntent().getDoubleExtra("USER_LAT", 0.0);
        double userLng = getIntent().getDoubleExtra("USER_LNG", 0.0);

        if (title != null) {
            toolbarTitle.setText(title);
        } else {
            toolbarTitle.setText("Nearby Places");
        }

        recyclerView = findViewById(R.id.nearby_places_recycler_view);
        emptyView = findViewById(R.id.empty_view);

        if (places != null && !places.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new NearbyPlaceAdapter(this, places, userLat, userLng);
            recyclerView.setAdapter(adapter);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nearby_places, menu);
        // This code makes the menu icon white
        Drawable icon = menu.getItem(0).getIcon();
        if (icon != null) {
            icon.mutate();
            DrawableCompat.setTint(icon, Color.WHITE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            FilterDistanceBottomSheet bottomSheet = new FilterDistanceBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "FilterDistanceBottomSheet");
            return true;
        }
        return super.onOptionsItemSelected(item);
        
    }

    @Override
    public void onFilterSelected(int filterOption) {

        if (adapter != null) {
            adapter.filter(filterOption);
            emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
