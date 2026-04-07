package com.example.builtdaily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.builtdaily.R;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView emailValue = findViewById(R.id.profileEmailValue);
        Button editScheduleButton = findViewById(R.id.editScheduleBtn);
        Button editPreferencesButton = findViewById(R.id.editPreferencesBtn);
        Button backHomeButton = findViewById(R.id.backHomeBtn);
        Button logoutButton = findViewById(R.id.profileLogoutBtn);

        String user = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("logged_in_user", "Not logged in");
        emailValue.setText(user);

        editScheduleButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, WorkoutScheduleActivity.class)));

        editPreferencesButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, VideoPreferencesActivity.class)));

        backHomeButton.setOnClickListener(v -> finish());

        logoutButton.setOnClickListener(v -> {
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("logged_in_user")
                    .apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
