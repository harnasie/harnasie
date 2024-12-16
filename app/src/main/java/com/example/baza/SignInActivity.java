package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setTitle("Logowanie");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnSignIn = findViewById(R.id.btn_login);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Wypełnij wszystkie pola.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            String userId = auth.getCurrentUser().getUid();
                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String role = documentSnapshot.getString("role");
                                            String username = documentSnapshot.getString("username");
                                            Log.d("DEBUG", "Rola użytkownika: " + role);
                                            setLoggedInState(true, userId, username);
                                            if ("admin".equalsIgnoreCase(role)) {
                                                Log.d("DEBUG", "POSZLO");
                                                Intent intent = new Intent(SignInActivity.this, AdminMenuActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(SignInActivity.this, UserActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                            if (user != null) {
                                fetchUsernameFromFirestore(user.getUid());
                            }
                        } else {
                            Toast.makeText(SignInActivity.this, "Logowanie nie powiodło się.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void fetchUsernameFromFirestore(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = document.getString("username");
                            String role = document.getString("role");
                            String id = document.getId();

                            if ("admin".equalsIgnoreCase(role)) {
                                Intent intent = new Intent(SignInActivity.this, AdminMenuActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("uid", id);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(SignInActivity.this, UserActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("uid", id);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Nie znaleziono użytkownika", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Coś się nie powiodło.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoggedInState(boolean state, String id, String username) {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", state);
        editor.putString("userId", id);
        editor.putString("username", username);
        editor.apply();
    }
}
