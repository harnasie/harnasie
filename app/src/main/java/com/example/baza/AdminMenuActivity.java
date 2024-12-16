package com.example.baza;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminMenuActivity extends AppCompatActivity {

    private Button btnNowePowiadomienie, btnZgloszenia, btnDodajKML, btnWyloguj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        setTitle("Panel administratora");

        btnNowePowiadomienie = findViewById(R.id.btnPowiadomieniaOgólne);
        btnZgloszenia = findViewById(R.id.btnZgloszenia);
        btnDodajKML = findViewById(R.id.btnDodajKML);
        btnWyloguj = findViewById(R.id.btnWyloguj);

        btnNowePowiadomienie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMenuActivity.this, AdminDangerActivity.class);
                startActivity(intent);
            }
        });

        btnZgloszenia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMenuActivity.this, ViewDangerActivity.class);
                startActivity(intent);
            }
        });

        btnDodajKML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMenuActivity.this, AdminUploadActivity.class);
                startActivity(intent);
            }
        });

        btnWyloguj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        setLoggedInState(false);
        Intent intent = new Intent(AdminMenuActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
    private void setLoggedInState(boolean state) {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", state);
        editor.apply();
    }

}
