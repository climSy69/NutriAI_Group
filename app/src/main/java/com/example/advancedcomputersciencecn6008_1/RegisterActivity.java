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

    private static final String TAG = "SUPABASE_DEBUG";
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

        // Validation logic
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        Log.d(TAG, "Starting registration for email: " + email);

        // --- STEP 1: SIGN UP WITH SUPABASE AUTH ---
        
        SupabaseClient.AuthRequest authRequest = new SupabaseClient.AuthRequest(email, password);
        SupabaseClient.AuthService authService = SupabaseClient.getClient().create(SupabaseClient.AuthService.class);

        authService.signUp(authRequest).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                Log.d(TAG, "SignUp HTTP Status: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authData = response.body();
                    
                    // Supabase always returns a user object on successful signup
                    if (authData.user != null) {
                        String userId = authData.user.id;
                        String accessToken = authData.getAccessToken(); // Use robust helper
                        
                        Log.d(TAG, "SignUp Successful. User ID: " + userId + " | Token available: " + (accessToken != null));
                        
                        // If accessToken is null, email confirmation is likely required
                        if (accessToken == null) {
                            Log.d(TAG, "Access token is null. Email confirmation may be required.");
                            Toast.makeText(RegisterActivity.this, "Signup successful! Please check your email for confirmation.", Toast.LENGTH_LONG).show();
                            // We still attempt profile creation with the anon key as fallback (if RLS allows)
                        }

                        // --- STEP 2: CREATE PROFILE IN DATABASE ---
                        createProfile(userId, fullName, email, accessToken);
                    } else {
                        Log.e(TAG, "SignUp Successful but User object is missing in response body.");
                        Toast.makeText(RegisterActivity.this, "Internal Error: User data missing.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = extractError(response);
                    Log.e(TAG, "SignUp Error (" + response.code() + "): " + errorMsg);
                    Toast.makeText(RegisterActivity.this, "Registration Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                Log.e(TAG, "SignUp Network Failure: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createProfile(String userId, String fullName, String email, String accessToken) {
        SupabaseClient.Profile profile = new SupabaseClient.Profile(userId, fullName, email);
        SupabaseClient.DatabaseService databaseService = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);

        // If accessToken is null (email confirmation required), we send null. 
        // SupabaseClient interceptor will then use the anon key.
        String authToken = (accessToken != null) ? "Bearer " + accessToken : null;
        
        Log.d(TAG, "Inserting Profile: ID=" + userId + ", Name=" + fullName + ", Email=" + email + " | Using Token: " + (authToken != null));

        databaseService.createProfile(authToken, profile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Profile Creation Status: " + response.code());
                
                if (response.isSuccessful() || response.code() == 201 || response.code() == 204) {
                    Log.d(TAG, "Profile created successfully.");
                    
                    // Store Session Data for subsequent use
                    UserSession.getInstance().setSession(userId, accessToken);
                    
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = extractError(response);
                    Log.e(TAG, "Profile Creation Error (" + response.code() + "): " + errorMsg);
                    Toast.makeText(RegisterActivity.this, "Registration partial success. Profile error (" + response.code() + ").", Toast.LENGTH_LONG).show();
                    // Don't finish(), let user see the error
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Profile Creation Network Failure: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Profile creation network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (IOException e) {
            return "Could not parse server error.";
        }
        return response.message();
    }
}
