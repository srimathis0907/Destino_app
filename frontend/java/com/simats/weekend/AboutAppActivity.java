package com.simats.weekend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);

        // Enable and handle the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // We are now setting the title in the Java file
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("About App");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}