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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_DEBUG";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "LoginActivity onCreate called");

        // Initialize UserSession and check for persisted login
        if (UserSession.getInstance(this).isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to MainActivity");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        if (btnLogin == null) {
            Log.e(TAG, "CRITICAL ERROR: btnLogin is NULL. Check layout IDs.");
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button pressed");
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        Log.d(TAG, "loginUser: Attempting login for: " + email);

        SupabaseClient.AuthRequest authRequest = new SupabaseClient.AuthRequest(email, password);
        SupabaseClient.AuthService authService = SupabaseClient.getClient().create(SupabaseClient.AuthService.class);

        authService.signIn(authRequest).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                Log.d(TAG, "onResponse: Received code " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authData = response.body();
                    Log.d(TAG, "onResponse: Success. Response body: " + authData.toString());
                    
                    String accessToken = authData.getAccessToken();
                    String userId = authData.getUserId();

                    if (accessToken != null && userId != null) {
                        // Save session
                        UserSession.getInstance(LoginActivity.this).setSession(userId, accessToken);
                        
                        Log.d(TAG, "onResponse: Session saved. Navigating to MainActivity.");
                        
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        Log.e(TAG, "onResponse: Login successful but missing User ID or Token. User: " + userId + ", Token: " + (accessToken != null));
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed: Incomplete user data from server", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "onResponse: Login failed. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorStr);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed: Invalid credentials or account issues", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Network error during login: " + t.getMessage(), t);
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
