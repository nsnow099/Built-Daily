package com.example.builtdaily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.builtdaily.R;
import com.example.builtdaily.network.*;
import com.example.builtdaily.utils.*;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView goToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String user = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("logged_in_user", null);
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        goToSignup = findViewById(R.id.goToSignup);

        AuthManager authManager = new AuthManager(this);

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            boolean success = authManager.login(email, password);

            if (success) {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("logged_in_user", email)
                        .apply();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
            }
        });

        goToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }
}
