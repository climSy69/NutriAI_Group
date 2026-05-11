package com.example.advancedcomputersciencecn6008_1;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_DEBUG";
    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Register button pressed");
                registerUser();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "registerUser: Initiating request for " + email);

        SupabaseClient.AuthRequest authRequest = new SupabaseClient.AuthRequest(email, password);
        SupabaseClient.AuthService authService = SupabaseClient.getClient().create(SupabaseClient.AuthService.class);

        // Consistent usage of Call<AuthResponse> and Callback<AuthResponse>
        authService.signUp(authRequest).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                Log.d(TAG, "onResponse: SignUp Code " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authData = response.body();
                    Log.d(TAG, "onResponse: Success. Response details: " + authData.toString());
                    
                    // Extract user ID using the helper method in AuthResponse
                    String userId = authData.getUserId();

                    if (userId != null) {
                        Log.d(TAG, "SignUp Successful. Creating profile for User ID: " + userId);
                        createProfile(userId, fullName, email);
                    } else {
                        Log.e(TAG, "SignUp successful but no user ID found in response.");
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Signup successful! Please check your email.", Toast.LENGTH_LONG).show());
                        finish();
                    }
                } else {
                    Log.e(TAG, "onResponse: SignUp failed. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorStr);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Network error: " + t.getMessage(), t);
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void createProfile(String userId, String fullName, String email) {
        SupabaseClient.Profile profile = new SupabaseClient.Profile(userId, fullName, email);
        SupabaseClient.DatabaseService databaseService = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);

        // passing null for token triggers the interceptor to use the anon key
        databaseService.createProfile(null, profile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 201 || response.code() == 204) {
                    Log.d(TAG, "Profile created successfully.");
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                } else {
                    Log.e(TAG, "Profile creation failed: " + response.code());
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Account created, but profile setup failed.", Toast.LENGTH_LONG).show());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Profile creation network error: " + t.getMessage());
                runOnUiThread(() -> finish());
            }
        });
    }
}
