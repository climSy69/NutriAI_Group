package com.example.advancedcomputersciencecn6008_1;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddMealActivity extends AppCompatActivity {

    private EditText etMealName, etCalories, etProtein, etCarbs, etFats;
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
        btnSaveMeal = findViewById(R.id.btnSaveMeal);

        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();
        String protein = etProtein.getText().toString().trim();
        String carbs = etCarbs.getText().toString().trim();
        String fats = etFats.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(calories) || 
            TextUtils.isEmpty(protein) || TextUtils.isEmpty(carbs) || TextUtils.isEmpty(fats)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // For now, just show a success message
        Toast.makeText(this, "Meal saved: " + name, Toast.LENGTH_SHORT).show();
        
        // Close activity and return to dashboard
        finish();
    }
}
