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

        // check if user is already logged in using shared preferences
        String user = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("logged_in_user", null);
        if (user != null) {
            // if they are, just go straight to the main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // link xml variables
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        goToSignup = findViewById(R.id.goToSignup);

        // auth manager handles the database check
        AuthManager authManager = new AuthManager(this);

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
                // save the login state in shared prefs so they stay logged in
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("logged_in_user", email)
                        .apply();
                // go to home screen
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                // show error if it failed
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
            }
        });

        // go to signup if they don't have an account
        goToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }
}
