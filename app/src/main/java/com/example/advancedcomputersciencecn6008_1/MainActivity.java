package com.example.advancedcomputersciencecn6008_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        Button btnAddMeal = findViewById(R.id.btnAddMeal);
        Button btnViewSummary = findViewById(R.id.btnViewSummary);
        Button btnWeekDiet = findViewById(R.id.btnWeekDiet);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Navigation Logic
        btnAddMeal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddMealActivity.class)));
        btnViewSummary.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DailySummaryActivity.class)));
        btnWeekDiet.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WeekDietActivity.class)));

        btnLogout.setOnClickListener(v -> {
            UserSession.getInstance(MainActivity.this).clearSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
