package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SubscriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        Button btnStartPremium = findViewById(R.id.btnStartPremium);
        Button btnMaybeLater = findViewById(R.id.btnMaybeLater);

        // 1. Premium Button: Do nothing (as requested, maybe just a toast)
        btnStartPremium.setOnClickListener(v -> {
            Toast.makeText(SubscriptionActivity.this, "Premium Selected", Toast.LENGTH_SHORT).show();
        });

        // 2. Maybe Later Button: Move to LoginActivity WITHOUT animation
        btnMaybeLater.setOnClickListener(v -> {
            Intent intent = new Intent(SubscriptionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Just finish, no custom transition
        });
    }
}