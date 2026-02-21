package com.simats.weekend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TermsPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy);

        Toolbar toolbar = findViewById(R.id.toolbar_terms);
        setSupportActionBar(toolbar);

        // Enable and handle the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Terms & Privacy Policy");
        }

        // This makes the toolbar's back arrow behave like the system back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}