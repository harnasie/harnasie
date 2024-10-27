package com.example.baza;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    EditText editName, editEmail;
    DangerDatabaseHelper dbHelper;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Zaloguj się");
        dbHelper = new DangerDatabaseHelper(this);
        editName = findViewById(R.id.editTextName);
        editEmail = findViewById(R.id.editTextEmail);
        btnLogin = findViewById(R.id.buttonLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String email = editEmail.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Pole imię nie może być puste", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Pole email nie może być puste", Toast.LENGTH_SHORT).show();
                }
                // Sprawdzanie poprawności formatu e-mail
                else if (!EMAIL_PATTERN.matcher(email).matches()) {
                    Toast.makeText(LoginActivity.this, "Nieprawidłowy format adresu email", Toast.LENGTH_SHORT).show();
                } else {
                    // Jeśli walidacja jest poprawna
                    DangerDatabaseHelper dbHelper = new DangerDatabaseHelper(LoginActivity.this);
                    boolean userExists = dbHelper.checkUser(name, email);

                    if (userExists) {
                        Toast.makeText(LoginActivity.this, "Użytkownik istnieje w bazie danych", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                        intent.putExtra("username",name);  // Przekazujemy nazwę użytkownika
                        startActivity(intent);  // Uruchamiamy nową aktywność
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Użytkownik nie istnieje w bazie danych", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }
}
