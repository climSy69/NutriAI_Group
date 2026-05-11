package com.example.advancedcomputersciencecn6008_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeekDietActivity extends AppCompatActivity {

    private static final String TAG = "WEEK_DIET_DEBUG";
    private LinearLayout containerMeals;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Button btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_diet);

        containerMeals = findViewById(R.id.containerMeals);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnHome = findViewById(R.id.btnHome);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(WeekDietActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        fetchMeals();
    }

    private void fetchMeals() {
        UserSession session = UserSession.getInstance(this);
        if (session == null || !session.isLoggedIn()) {
            Log.e(TAG, "User not logged in");
            Toast.makeText(this, "Please login to view your diet", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Fetching meals for User ID: " + session.getUserId());

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        if (containerMeals != null) containerMeals.removeAllViews();

        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + session.getAccessToken();
        String filter = "eq." + session.getUserId();

        service.getMeals(authToken, filter).enqueue(new Callback<List<SupabaseClient.Meal>>() {
            @Override
            public void onResponse(Call<List<SupabaseClient.Meal>> call, Response<List<SupabaseClient.Meal>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<SupabaseClient.Meal> meals = response.body();
                    Log.d(TAG, "Successfully fetched " + meals.size() + " meals");
                    if (meals.isEmpty()) {
                        if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        displayMeals(meals);
                    }
                } else {
                    Log.e(TAG, "Fetch Meals Error Code: " + response.code());
                    Toast.makeText(WeekDietActivity.this, "Failed to load meals", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SupabaseClient.Meal>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Fetch Meals Network Failure: " + t.getMessage());
                Toast.makeText(WeekDietActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMeal(SupabaseClient.Meal meal) {
        UserSession session = UserSession.getInstance(this);
        if (session == null || !session.isLoggedIn()) return;

        progressBar.setVisibility(View.VISIBLE);
        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + session.getAccessToken();
        String filter = "eq." + meal.id;

        service.deleteMeal(authToken, filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    fetchMeals(); // Refresh the list
                    showUndoSnackbar(meal);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Failed to delete meal", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showUndoSnackbar(SupabaseClient.Meal deletedMeal) {
        Snackbar.make(findViewById(android.R.id.content), "Meal deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> undoDelete(deletedMeal))
                .show();
    }

    private void undoDelete(SupabaseClient.Meal meal) {
        UserSession session = UserSession.getInstance(this);
        if (session == null || !session.isLoggedIn()) return;

        progressBar.setVisibility(View.VISIBLE);
        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + session.getAccessToken();

        SupabaseClient.Meal restoredMeal = new SupabaseClient.Meal(
                meal.userId, meal.mealName, meal.calories, meal.protein, meal.carbs, meal.fats, meal.day, meal.mealType
        );

        service.insertMeal(authToken, restoredMeal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 201 || response.code() == 204) {
                    fetchMeals();
                    Snackbar.make(findViewById(android.R.id.content), "Meal restored", Snackbar.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Failed to restore meal", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Restore error", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMeals(List<SupabaseClient.Meal> meals) {
        if (meals == null || containerMeals == null) return;

        Log.d(TAG, "Preparing to display meals...");
        Map<String, List<SupabaseClient.Meal>> groupedMeals = new LinkedHashMap<>();
        
        String[] weekDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : weekDays) {
            groupedMeals.put(day, new ArrayList<>());
        }

        for (SupabaseClient.Meal meal : meals) {
            if (meal == null) continue;
            String day = (meal.day != null) ? meal.day : "Other";
            
            if (groupedMeals.containsKey(day)) {
                groupedMeals.get(day).add(meal);
            } else {
                if (!groupedMeals.containsKey("Other")) {
                    groupedMeals.put("Other", new ArrayList<>());
                }
                groupedMeals.get("Other").add(meal);
            }
        }

        for (Map.Entry<String, List<SupabaseClient.Meal>> entry : groupedMeals.entrySet()) {
            List<SupabaseClient.Meal> dayMeals = entry.getValue();
            if (dayMeals == null || dayMeals.isEmpty()) continue;

            String dayName = entry.getKey();

            // Day Header
            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextSize(24);
            tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDay.setTextColor(getResources().getColor(android.R.color.black));
            tvDay.setPadding(0, 48, 0, 8);
            containerMeals.addView(tvDay);

            // Daily Totals
            int totalCalories = 0;
            double totalProtein = 0.0, totalCarbs = 0.0, totalFats = 0.0;
            for (SupabaseClient.Meal meal : dayMeals) {
                totalCalories += meal.calories;
                totalProtein += meal.protein;
                totalCarbs += meal.carbs;
                totalFats += meal.fats;
            }

            TextView tvTotals = new TextView(this);
            String totalsText = String.format("Total: %d kcal | P: %.1fg | C: %.1fg | F: %.1fg", 
                                              totalCalories, totalProtein, totalCarbs, totalFats);
            tvTotals.setText(totalsText);
            tvTotals.setTextSize(14);
            tvTotals.setTypeface(null, android.graphics.Typeface.ITALIC);
            tvTotals.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvTotals.setPadding(0, 0, 0, 16);
            containerMeals.addView(tvTotals);

            // Individual Meals with Delete Button
            for (SupabaseClient.Meal meal : dayMeals) {
                LinearLayout mealRow = new LinearLayout(this);
                mealRow.setOrientation(LinearLayout.HORIZONTAL);
                mealRow.setGravity(Gravity.CENTER_VERTICAL);
                mealRow.setPadding(16, 8, 0, 8);

                TextView tvMeal = new TextView(this);
                String mealName = (meal.mealName != null) ? meal.mealName : "Unnamed Meal";
                String mealType = (meal.mealType != null) ? meal.mealType : "Meal";
                tvMeal.setText("• " + mealType + ": " + mealName + " (" + meal.calories + " kcal)");
                tvMeal.setTextSize(16);
                tvMeal.setTextColor(getResources().getColor(android.R.color.black));
                tvMeal.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

                TextView btnDelete = new TextView(this);
                btnDelete.setText("❌");
                btnDelete.setPadding(16, 16, 16, 16);
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Meal")
                            .setMessage("Are you sure you want to delete this meal?")
                            .setPositiveButton("Yes", (dialog, which) -> deleteMeal(meal))
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                mealRow.addView(tvMeal);
                mealRow.addView(btnDelete);
                containerMeals.addView(mealRow);
            }
        }
    }
}
