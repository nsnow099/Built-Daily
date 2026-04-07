package com.example.builtdaily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.network.*;
import com.example.builtdaily.utils.*;

public class SignupActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        signupBtn = findViewById(R.id.signupBtn);

        AuthManager authManager = new AuthManager(this);

        signupBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim().toLowerCase();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean created = authManager.signup(email, password);

            if (created) {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("logged_in_user", email)
                        .apply();
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Could not create account", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
