package com.example.builtdaily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.network.*;
import com.example.builtdaily.utils.*;

public class SignupActivity extends AppCompatActivity {
    // fields for email and password
    EditText emailInput, passwordInput;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // link xml variables
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        signupBtn = findViewById(R.id.signupBtn);

        // create auth manager to save user to db
        AuthManager authManager = new AuthManager(this);

        signupBtn.setOnClickListener(v -> {
            // get user input
            String email = emailInput.getText().toString().trim().toLowerCase();
            String password = passwordInput.getText().toString().trim();

            // validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // try to sign up
            boolean created = authManager.signup(email, password);

            if (created) {
                // if it worked, save login status and go to main activity
                int userId = authManager.getUserIdFromEmail(email);
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt("logged_in_user_id", userId)
                        .apply();
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                // clear activity stack so back button doesn't go back to login/signup
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // error if signup failed
                Toast.makeText(this, "Could not create account", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
