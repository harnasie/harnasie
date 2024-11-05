package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        Button btnSignIn = findViewById(R.id.btn_sign_in);

        btnSignUp.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        });

        btnSignIn.setOnClickListener(v -> {
            Intent signInIntent = new Intent(WelcomeActivity.this, SignInActivity.class);
            startActivity(signInIntent);
        });
    }
}

