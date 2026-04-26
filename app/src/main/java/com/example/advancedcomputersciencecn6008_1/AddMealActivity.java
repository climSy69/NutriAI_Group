package com.example.advancedcomputersciencecn6008_1;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMealActivity extends AppCompatActivity {

    private static final String TAG = "SUPABASE_DEBUG";
    private EditText etMealName, etCalories, etProtein, etCarbs, etFats;
    private Spinner spinnerDay, spinnerMealType;
    private Button btnSaveMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Initialize views
        etMealName = findViewById(R.id.etMealName);
        etCalories = findViewById(R.id.etCalories);
        etProtein = findViewById(R.id.etProtein);
        etCarbs = findViewById(R.id.etCarbs);
        etFats = findViewById(R.id.etFats);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);

        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String fatsStr = etFats.getText().toString().trim();
        
        String selectedDay = spinnerDay.getSelectedItem().toString();
        String selectedMealType = spinnerMealType.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) || 
            TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(carbsStr) || TextUtils.isEmpty(fatsStr)) {
            Toast.makeText(this, "Please fill in all nutrient fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerDay.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a day", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerMealType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a meal type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Session Check
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int calories = Integer.parseInt(caloriesStr);
            int protein = (int) Double.parseDouble(proteinStr); // Handle potential decimals
            int carbs = (int) Double.parseDouble(carbsStr);
            int fats = (int) Double.parseDouble(fatsStr);

            SupabaseClient.Meal meal = new SupabaseClient.Meal(
                session.getUserId(),
                name,
                calories,
                protein,
                carbs,
                fats,
                selectedDay,
                selectedMealType
            );

            Log.d(TAG, "Attempting to save meal: " + name + " for " + selectedDay + " " + selectedMealType);
            
            saveMealToSupabase(meal, session.getAccessToken());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMealToSupabase(SupabaseClient.Meal meal, String accessToken) {
        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + accessToken;

        service.insertMeal(authToken, meal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Meal Insertion HTTP Status: " + response.code());
                
                if (response.isSuccessful() || response.code() == 201 || response.code() == 204) {
                    Log.d(TAG, "Meal saved successfully");
                    Toast.makeText(AddMealActivity.this, "Meal saved successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    String errorMsg = "Failed to save meal";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Log.e(TAG, "Error: " + errorMsg);
                    Toast.makeText(AddMealActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                Toast.makeText(AddMealActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        etMealName.setText("");
        etCalories.setText("");
        etProtein.setText("");
        etCarbs.setText("");
        etFats.setText("");
        spinnerDay.setSelection(0);
        spinnerMealType.setSelection(0);
        etMealName.requestFocus();
    }
}
