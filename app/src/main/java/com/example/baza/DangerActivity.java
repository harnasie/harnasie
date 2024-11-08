package com.example.baza;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DangerActivity extends AppCompatActivity {
    private Button btnAddDanger, btnViewDangers, btnDelete, btnMapa, btnTelephone, btnDanger;
    TextView type_tv, description_tv;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private Spinner sp;
    private EditText editDescription;
    private LatLng currentLocation = null;
    private FirebaseFirestore db;
    private String uid = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);
        setTitle("Zgłoś zagrożenie");

        btnAddDanger = findViewById(R.id.buttonAddDanger);
        btnViewDangers = findViewById(R.id.buttonViewDangers);
        type_tv = findViewById(R.id.type_tv);
        description_tv = findViewById(R.id.description_tv);
        editDescription = findViewById(R.id.editTextDescription);
        sp = findViewById(R.id.spinnertype);
        db = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        String userName = intent.getStringExtra("username");
        uid = intent.getStringExtra("uid");        Log.d("emmmmmmmmm" , String.valueOf(8));
        btnViewDangers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangerActivity.this, ViewDangerActivity.class);
                startActivity(intent);
            }
        });

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selecteType = parent.getItemAtPosition(position).toString();
                Log.d("wybrano:", selecteType);            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Niezbędna metoda, ale nie musimy nic robić, jeśli nic nie jest wybrane
            }
        });
        Log.d("emmmmm111mmmm" , String.valueOf(8));


        btnAddDanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = editDescription.getText().toString();
                //String name = userName;
                String type = sp.getSelectedItem().toString();
                if (description.isEmpty()) {
                    Toast.makeText(DangerActivity.this, "Pole opis nie może być puste", Toast.LENGTH_SHORT).show();
                }
                // Sprawdzanie poprawności formatu e-mail
                else {
                    addDanger(description,type,uid);
                    // Jeśli walidacja jest poprawna, dodaj użytkownika do bazy danych
                    /*boolean isInserted = dbHelper.addDanger(type, location, description, name);
                    if (isInserted) {
                        Toast.makeText(UserActivity.this, "Dodano zgłoszenie", Toast.LENGTH_SHORT).show();
                        editDescription.setText("");
                        editLocation.setText("");
                    } else {
                        Toast.makeText(UserActivity.this, "Błąd przy dodawaniu zgłoszenia", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        });

        btnViewDangers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangerActivity.this, ViewDangerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addDanger(String description, String type, String uid) {
        checkLocationPermission();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Brak uprawnień do lokalizacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pobieramy ostatnią lokalizację
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("lokalizacja", String.valueOf(currentLocation));

                // Pobieramy dokument użytkownika, aby uzyskać jego dane
                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        // Tworzymy mapę `userMap` z danymi użytkownika
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", userDoc.getString("email"));
                        userMap.put("username", userDoc.getString("username"));
                        userMap.put("uid", uid);

                        // Tworzymy mapę danych dla `danger`
                        Map<String, Object> danger = new HashMap<>();
                        danger.put("description", description);
                        danger.put("location", currentLocation);
                        danger.put("type", type);
                        danger.put("user", userMap); // Zagnieżdżamy obiekt `userMap` w `danger`
                        danger.put("createdAt", Timestamp.now());
                        danger.put("accepted", false);


                        // Zapisujemy dane do kolekcji "dangers" w Firestore
                        db.collection("dangers").add(danger)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(DangerActivity.this, "Dodano zagrożenie z ID: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                                    Log.d("Firestore", "Dodano dokument o ID: " + documentReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreError", "Błąd przy dodawaniu zagrożenia", e);
                                    Toast.makeText(DangerActivity.this, "Błąd przy dodawaniu zagrożenia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Nie znaleziono użytkownika.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Błąd przy pobieraniu danych użytkownika", e);
                    Toast.makeText(this, "Błąd przy pobieraniu danych użytkownika: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(DangerActivity.this, "Nie można uzyskać bieżącej lokalizacji.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Jeśli brak uprawnienia, prosimy o jego przyznanie
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

}
