package com.example.baza;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class DangersActivity extends AppCompatActivity {

    private static final String TAG = "DangerActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private EditText etType, etLocation, etDescription;
    private Button btnSaveDanger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangers);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize UI elements
        etType = findViewById(R.id.et_type);
        etLocation = findViewById(R.id.et_location);
        etDescription = findViewById(R.id.et_description);
        btnSaveDanger = findViewById(R.id.btn_submit);

        // Button to save danger
        btnSaveDanger.setOnClickListener(v -> {saveDangerToDatabase();
            /*if (currentUser != null) {
                saveDangerToDatabase();
            } else {
                Toast.makeText(DangersActivity.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            }*/
        });

        // Fetch collections
        fetchCollection("users");
        fetchCollection("dangers"); //user3@gmail.com
    }

    private void saveDangerToDatabase() {
        Toast.makeText(this, "Rozpoczynam zapis", Toast.LENGTH_SHORT).show();

        if (!isConnectedToInternet()) {
            Log.d("Brak połączenia z Internetem. Spróbuj ponowni", String.valueOf(4));
            Toast.makeText(this, "Brak połączenia z Internetem. Spróbuj ponownie później.", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = etType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        Log.d("Brem. Spróbuj ponowni", String.valueOf(4));

        if (type.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Proszę uzupełnić wszystkie pola.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Map<String, Object> dangerData = new HashMap<>();
            dangerData.put("type", type);
            dangerData.put("location", location);
            dangerData.put("description", description);
            dangerData.put("user", currentUser.getUid());  // Upewnij się, że `currentUser` nie jest null
            dangerData.put("timeCreated", String.valueOf(System.currentTimeMillis()));
            dangerData.put("accepted", false);

            db.collection("dangers")
                    .add(dangerData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(DangersActivity.this, "Zgłoszenie zapisane pomyślnie!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Zgłoszenie dodane z ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DangersActivity.this, "Błąd zapisu zgłoszenia", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Błąd dodawania zgłoszenia", e);
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Wystąpił błąd podczas zapisu", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Błąd wewnątrz saveDangerToDatabase", e);
        }
    }


    private void fetchCollection(String collectionName) {
        CollectionReference collectionRef = db.collection(collectionName);

        collectionRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Log each document or perform desired actions with data
                            Log.d(TAG, collectionName + " Document ID: " + document.getId() + " => " + document.getData());
                        }
                        Toast.makeText(DangersActivity.this, "Fetched " + collectionName + " successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Error getting documents in " + collectionName, task.getException());
                        Toast.makeText(DangersActivity.this, "Failed to fetch " + collectionName, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
