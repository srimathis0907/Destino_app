package com.simats.weekend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView; // Make sure this import is here

public class AdminContactSupport extends AppCompatActivity {

    // Contact Details
    private static final String DEV_EMAIL = "ranjithsuriya12345@gmail.com";
    private static final String DEV_PHONE = "9876543210";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);

        Toolbar toolbar = findViewById(R.id.toolbar_contact);
        setSupportActionBar(toolbar);

        // Enable and handle the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Contact Developer");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // **FIXED SECTION**
        // We now find the MaterialCardViews by their correct IDs
        MaterialCardView cardEmailDev = findViewById(R.id.card_email_dev);
        MaterialCardView cardCallDev = findViewById(R.id.card_call_dev);

        // The old button listeners are removed, and new card listeners are used
        cardEmailDev.setOnClickListener(v -> {
            sendEmailToDev();
        });

        cardCallDev.setOnClickListener(v -> {
            callDev();
        });
    }

    // New method to open Email app
    private void sendEmailToDev() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{DEV_EMAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from Weekend Admin");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Developer,\n\nI'm experiencing an issue...");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    // New method to open Dialer
    private void callDev() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + DEV_PHONE));
        try {
            startActivity(dialIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Could not open phone dialer.", Toast.LENGTH_SHORT).show();
        }
    }
}