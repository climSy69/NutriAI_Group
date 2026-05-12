package com.example.advancedcomputersciencecn6008_1;

import android.content.Intent;
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
    private EditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
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
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
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

        authService.signUp(authRequest).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authData = response.body();
                    String userId = authData.getUserId();
                    String accessToken = authData.getAccessToken();
                    String userEmail = (authData.user != null) ? authData.user.email :
                                       (authData.session != null && authData.session.user != null) ?
                                       authData.session.user.email : null;

                    if (userId != null) {
                        if (accessToken != null) {
                            UserSession.getInstance(RegisterActivity.this).setSession(userId, accessToken, userEmail);
                        }
                        createProfile(userId, accessToken, fullName, email, username);
                    } else {
                        finish();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void createProfile(String userId, String accessToken, String fullName, String email, String username) {
        // userId is mapped to the "id" field in the Profile model
        SupabaseClient.Profile profile = new SupabaseClient.Profile(userId, fullName, email, username);
        SupabaseClient.DatabaseService databaseService = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);

        String authToken = accessToken != null ? "Bearer " + accessToken : null;

        databaseService.createProfile(authToken, profile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 201 || response.code() == 204) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                        if (UserSession.getInstance().isLoggedIn()) {
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        finish();
                    });
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Profile creation failed: " + response.code() + " " + errorMsg);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Profile creation network failure", t);
                finish();
            }
        });
    }
}
