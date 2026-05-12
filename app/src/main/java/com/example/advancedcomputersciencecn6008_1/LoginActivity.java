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
import java.util.List;

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

        if (UserSession.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient.AuthRequest authRequest = new SupabaseClient.AuthRequest(email, password);
        SupabaseClient.AuthService authService = SupabaseClient.getClient().create(SupabaseClient.AuthService.class);

        authService.signIn(authRequest).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authData = response.body();
                    String accessToken = authData.getAccessToken();
                    String userId = authData.getUserId();
                    String userEmail = (authData.user != null) ? authData.user.email :
                                       (authData.session != null && authData.session.user != null) ?
                                       authData.session.user.email : null;

                    if (accessToken != null && userId != null) {
                        fetchProfileAndFinish(userId, accessToken, userEmail);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProfileAndFinish(String userId, String accessToken, String email) {
        // 1. Save session immediately with available data
        UserSession.getInstance(this).setSession(userId, accessToken, email, null);
        
        // 2. Navigate to MainActivity immediately to prevent UI hang
        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        // 3. Fetch profile asynchronously in the background
        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + accessToken;

        service.getProfile(authToken, "eq." + userId).enqueue(new Callback<List<SupabaseClient.Profile>>() {
            @Override
            public void onResponse(Call<List<SupabaseClient.Profile>> call, Response<List<SupabaseClient.Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String username = response.body().get(0).username;
                    if (username != null) {
                        // Update session with username using Application Context as Activity is finished
                        UserSession.getInstance(getApplicationContext()).setUsername(username);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SupabaseClient.Profile>> call, Throwable t) {
                Log.e(TAG, "Background profile fetch failed", t);
            }
        });
    }
}
