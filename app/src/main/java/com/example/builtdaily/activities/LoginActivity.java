package com.example.builtdaily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.network.*;
import com.example.builtdaily.utils.*;

public class LoginActivity extends AppCompatActivity {
    // edit texts and button for logging in
    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView goToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // auth manager handles the database check
        AuthManager authManager = new AuthManager(this);

        // check if user is already logged in using shared preferences
        int loggedInUserId = getSharedPreferences("auth_prefs", MODE_PRIVATE).getInt("logged_in_user_id", -1);
        if (loggedInUserId != -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // link xml variables
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        goToSignup = findViewById(R.id.goToSignup);

        loginBtn.setOnClickListener(v -> {
            // get the text from inputs
            String email = emailInput.getText().toString().trim().toLowerCase();
            String password = passwordInput.getText().toString().trim();

            // basic check to see if they entered anything
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // try to log in
            boolean success = authManager.login(email, password);
            if (success) {
                int userId = authManager.getUserIdFromEmail(email);
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt("logged_in_user_id", userId)
                        .apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
            }
        });

        // go to signup if they don't have an account
        goToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }
}