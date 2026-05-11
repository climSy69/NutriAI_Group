package com.example.advancedcomputersciencecn6008_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextView tvId = findViewById(R.id.tvProfileId);
        Button btnLogout = findViewById(R.id.btnProfileLogout);
        Button btnHome = findViewById(R.id.btnHome);

        UserSession session = UserSession.getInstance(this);
        if (session.isLoggedIn()) {
            String email = session.getEmail();
            tvEmail.setText(email != null ? email : "No email");
            
            String username = (email != null && email.contains("@"))
                    ? email.substring(0, email.indexOf("@"))
                    : "User";
            tvId.setText("Username: " + username);
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
}
