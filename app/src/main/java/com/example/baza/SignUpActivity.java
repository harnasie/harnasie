package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setTitle("Rejestracja");

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etUsername = findViewById(R.id.et_username);
        Button btnSignUp = findViewById(R.id.btn_register);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Wypełnij wszystkie pola.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                addUserToFirestore(user.getUid(), email, username);
                            }
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e("SignUpError", "Sign-up failed: " + errorMessage);
                            Toast.makeText(SignUpActivity.this, "Rejestracja nie powiodła się: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    protected void addUserToFirestore(String uid, String email, String username) {
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Błąd: UID jest null lub pusty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("username", username);
        userMap.put("uid", uid);
        userMap.put("role", "user");

        db.collection("users").document(uid).set(userMap)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(SignUpActivity.this, "Użytkownik dodany do bazy.", Toast.LENGTH_SHORT).show()

                )
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding user", e);
                    Toast.makeText(SignUpActivity.this, "Błąd przy dodawaniu nowego użytkownika: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
