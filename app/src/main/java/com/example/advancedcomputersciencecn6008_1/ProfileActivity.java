package com.example.advancedcomputersciencecn6008_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail, tvId;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvEmail = findViewById(R.id.tvProfileEmail);
        tvId = findViewById(R.id.tvProfileId);
        Button btnLogout = findViewById(R.id.btnProfileLogout);
        Button btnHome = findViewById(R.id.btnHome);

        session = UserSession.getInstance(this);
        if (session.isLoggedIn()) {
            tvEmail.setText(session.getEmail());
            String username = session.getUsername();
            tvId.setText("Username: " + (username != null ? username : "User"));
            
            if (username == null || username.equals("User")) {
                fetchProfileFromSupabase();
            }
        }

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            session.clearSession();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchProfileFromSupabase() {
        SupabaseClient.DatabaseService service = SupabaseClient.getClient().create(SupabaseClient.DatabaseService.class);
        String authToken = "Bearer " + session.getAccessToken();

        service.getProfile(authToken, "eq." + session.getUserId()).enqueue(new Callback<List<SupabaseClient.Profile>>() {
            @Override
            public void onResponse(Call<List<SupabaseClient.Profile>> call, Response<List<SupabaseClient.Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    SupabaseClient.Profile profile = response.body().get(0);
                    if (profile.username != null) {
                        session.setUsername(profile.username);
                        tvId.setText("Username: " + profile.username);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SupabaseClient.Profile>> call, Throwable t) {
                // Silently fail or log
            }
        });
    }
}
