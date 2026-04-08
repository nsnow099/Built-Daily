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

        // link layout IDs
        TextView emailValue = findViewById(R.id.profileEmailValue);
        Button editScheduleButton = findViewById(R.id.editScheduleBtn);
        Button editPreferencesButton = findViewById(R.id.editPreferencesBtn);
        Button backHomeButton = findViewById(R.id.backHomeBtn);
        Button logoutButton = findViewById(R.id.profileLogoutBtn);

        // get the email from shared prefs to show it on the profile screen
        String user = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("logged_in_user", "Not logged in");
        emailValue.setText(user);

        // open schedule editor
        editScheduleButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, WorkoutScheduleActivity.class)));

        // open video preferences
        editPreferencesButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, VideoPreferencesActivity.class)));

        // just close this activity to go back
        backHomeButton.setOnClickListener(v -> finish());

        // handle logout
        logoutButton.setOnClickListener(v -> {
            // clear the login info so it shows login screen next time
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("logged_in_user")
                    .apply();
            // go back to login screen
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // clear the stack so they can't go back to profile
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
