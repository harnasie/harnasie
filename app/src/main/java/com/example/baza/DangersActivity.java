package com.example.baza;

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
        btnSaveDanger.setOnClickListener(v -> {
            if (currentUser != null) {
                saveDangerToDatabase();
            } else {
                Toast.makeText(DangersActivity.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch collections
        fetchCollection("users");
        fetchCollection("dangers");
    }

    private void saveDangerToDatabase() {
        // Get input data from form
        String type = etType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String userId = currentUser.getUid();
        String timeCreated = String.valueOf(System.currentTimeMillis());
        boolean accepted = false;

        if (type.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new danger map
        Map<String, Object> dangerData = new HashMap<>();
        dangerData.put("type", type);
        dangerData.put("location", location);
        dangerData.put("description", description);
        dangerData.put("user", userId);
        dangerData.put("timeCreated", timeCreated);
        dangerData.put("accepted", accepted);

        // Add the new danger to the "dangers" collection
        db.collection("dangers")
                .add(dangerData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(DangersActivity.this, "Danger saved successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Danger added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DangersActivity.this, "Error saving danger", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding danger", e);
                });
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
}
