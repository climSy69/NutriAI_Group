package com.example.advancedcomputersciencecn6008_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DailySummaryActivity extends AppCompatActivity {

    private static final String TAG = "DAILY_SUMMARY_DEBUG";
    private TextView tvTotalCalories, tvTotalProtein, tvTotalCarbs, tvTotalFats, tvNoMeals;
    private ProgressBar progressBar;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_summary);

        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalProtein = findViewById(R.id.tvTotalProtein);
        tvTotalCarbs = findViewById(R.id.tvTotalCarbs);
        tvTotalFats = findViewById(R.id.tvTotalFats);
        tvNoMeals = findViewById(R.id.tvNoMeals);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        fetchTodaySummary();
    }

    private void fetchTodaySummary() {
        UserSession session = UserSession.getInstance(this);
        if (session == null || !session.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + session.getAccessToken();
        String filter = "eq." + session.getUserId();

        service.getMeals(authToken, filter).enqueue(new Callback<List<SupabaseClient.Meal>>() {
            @Override
            public void onResponse(Call<List<SupabaseClient.Meal>> call, Response<List<SupabaseClient.Meal>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    calculateSummary(response.body());
                } else if (response.code() == 401) {
                    handleSessionExpired();
                } else {
                    Toast.makeText(DailySummaryActivity.this, "Failed to load summary", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SupabaseClient.Meal>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network Failure: " + t.getMessage());
                Toast.makeText(DailySummaryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateSummary(List<SupabaseClient.Meal> meals) {
        String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        Log.d(TAG, "Filtering meals for today: " + currentDay);

        int totalCals = 0;
        int totalProtein = 0;
        int totalCarbs = 0;
        int totalFats = 0;
        boolean hasMealsToday = false;

        for (SupabaseClient.Meal meal : meals) {
            if (currentDay.equalsIgnoreCase(meal.day)) {
                totalCals += meal.calories;
                totalProtein += meal.protein;
                totalCarbs += meal.carbs;
                totalFats += meal.fats;
                hasMealsToday = true;
            }
        }

        if (hasMealsToday) {
            tvTotalCalories.setText("Total Calories: " + totalCals + " kcal");
            tvTotalProtein.setText("Total Protein: " + totalProtein + "g");
            tvTotalCarbs.setText("Total Carbs: " + totalCarbs + "g");
            tvTotalFats.setText("Total Fats: " + totalFats + "g");
            tvNoMeals.setVisibility(View.GONE);
        } else {
            tvNoMeals.setVisibility(View.VISIBLE);
        }
    }

    private void handleSessionExpired() {
        UserSession.getInstance(this).clearSession();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
