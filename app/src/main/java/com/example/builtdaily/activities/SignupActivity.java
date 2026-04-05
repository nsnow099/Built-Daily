package com.example.builtdaily.activities;

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
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            boolean created = authManager.signup(email, password);

            if (created) {
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
