package com.example.advancedcomputersciencecn6008_1;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMealActivity extends AppCompatActivity {

    private static final String TAG = "SUPABASE_DEBUG";
    private EditText etMealName, etCalories, etProtein, etCarbs, etFats;
    private MaterialAutoCompleteTextView spinnerDay, spinnerMealType;
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
        
        // Fix: Casting to MaterialAutoCompleteTextView instead of Spinner
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        
        btnSaveMeal = findViewById(R.id.btnSaveMeal);

        setupDropdowns();

        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }

    private void setupDropdowns() {
        String[] days = getResources().getStringArray(R.array.days_array);
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, days);
        spinnerDay.setAdapter(daysAdapter);

        String[] mealTypes = getResources().getStringArray(R.array.meal_types_array);
        ArrayAdapter<String> mealTypesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mealTypes);
        spinnerMealType.setAdapter(mealTypesAdapter);
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String fatsStr = etFats.getText().toString().trim();
        
        // Use getText().toString() for MaterialAutoCompleteTextView
        String selectedDay = spinnerDay.getText().toString();
        String selectedMealType = spinnerMealType.getText().toString();

        // Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) || 
            TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(carbsStr) || TextUtils.isEmpty(fatsStr)) {
            Toast.makeText(this, "Please fill in all nutrient fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedDay) || selectedDay.equals("Select Day")) {
            Toast.makeText(this, "Please select a day", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedMealType) || selectedMealType.equals("Select Meal Type")) {
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
            int protein = (int) Double.parseDouble(proteinStr);
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
                if (response.isSuccessful() || response.code() == 201 || response.code() == 204) {
                    Toast.makeText(AddMealActivity.this, "Meal saved successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(AddMealActivity.this, "Failed to save meal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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
        // Reset dropdowns
        spinnerDay.setText("", false);
        spinnerMealType.setText("", false);
        etMealName.requestFocus();
    }
}
