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
        } else {
            Log.d(TAG, "btnLogin successfully initialized");
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button physically pressed");
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Register link pressed");
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "loginUser() triggered with email: " + email);

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

        // --- SIGN IN WITH SUPABASE ---
        Log.d(TAG, "Initiating Supabase Auth request...");

        SupabaseClient.AuthRequest authRequest = new SupabaseClient.AuthRequest(email, password);
        SupabaseClient.AuthService authService = SupabaseClient.getClient().create(SupabaseClient.AuthService.class);

        authService.signIn(authRequest).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                Log.d(TAG, "Supabase Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    // Store Session Data
                    SupabaseClient.AuthResponse auth = response.body();
                    UserSession.getInstance().setSession(auth.user.id, auth.getAccessToken());
                    
                    Log.d(TAG, "Login successful, User ID: " + auth.user.id);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Login failed. HTTP Status: " + response.code());
                    Toast.makeText(LoginActivity.this, "Login failed: Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network error during login", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
