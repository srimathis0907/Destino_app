package com.simats.weekend; // Make sure this matches your package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView; // ADDED

public class UcontactActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCardView cardEmailAdmin; // ADDED
    private MaterialCardView cardCallAdmin; // ADDED

    // Contact Details
    private static final String ADMIN_EMAIL = "srimathisivan09@gmail.com";
    private static final String ADMIN_PHONE = "9876543211";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ucontact);

        toolbar = findViewById(R.id.toolbar);
        cardEmailAdmin = findViewById(R.id.card_email_admin); // UPDATED
        cardCallAdmin = findViewById(R.id.card_call_admin); // UPDATED

        setupToolbar();

        // Set click listener for Email Card
        cardEmailAdmin.setOnClickListener(v -> {
            sendEmailToAdmin();
        });

        // Set click listener for Call Card
        cardCallAdmin.setOnClickListener(v -> {
            callAdmin();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Title is set in XML
        }
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    // New method to open Email app
    private void sendEmailToAdmin() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ADMIN_EMAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from Weekend App User");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Admin,\n\nI need help with...");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    // New method to open Dialer
    private void callAdmin() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + ADMIN_PHONE));
        try {
            startActivity(dialIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Could not open phone dialer.", Toast.LENGTH_SHORT).show();
        }
    }
}