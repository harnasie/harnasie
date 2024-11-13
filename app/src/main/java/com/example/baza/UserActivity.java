package com.example.baza;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private TextView welcomeTextView;
    Button btnViewDangers, btnGoDanger, btnMapa, btnTelephone, btnDanger;
    FirebaseFirestore db;
    private LineChart lineChart;
    private LatLng currentLocation = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private String uid = null;
    private LinearLayout routeInputLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setTitle("Konto użytkownika");

        // Znalezienie TextView do wyświetlania wiadomości powitalnej
        welcomeTextView = findViewById(R.id.welcomeTextView);
        btnViewDangers = findViewById(R.id.buttonViewDangers);
        btnTelephone = findViewById(R.id.buttonTelephone);
        btnGoDanger = findViewById(R.id.buttonGoDanger);
        btnMapa = findViewById(R.id.buttonMapa);
        lineChart = findViewById(R.id.lineChart);
        routeInputLayout = findViewById(R.id.route_input_layout);
        btnDanger = findViewById(R.id.buttonDanger);
        db = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //checkLocationPermission();
        // Odbieranie nazwy użytkownika przekazanej z LoginActivity
        /*Intent getintent = getIntent();
        String userName = getintent.getStringExtra("username");
        uid = getintent.getStringExtra("uid");
        Log.d("iertyu",uid);*/

        //Cursor res = dbHelper.getAllDistances();
        /*dbHelper.addDistance("Magda", "1000", "2023-09-01");
        dbHelper.addDistance("Magda", "4000", "2023-09-02");
        dbHelper.addDistance("Magda", "9000", "2023-09-03");
*/
/*        Cursor res = dbHelper.getAllDistances();
        // Budujemy stringa zawierającego wszystkie dane
        StringBuilder stringBuffer = new StringBuilder();
        while (res.moveToNext()) {
            Log.d("Database", "ID: " + res.getString(0));
            Log.d("Database", "USER: " + res.getString(1));
            Log.d("Database", "DISTANCE: " + res.getString(2));
            Log.d("Database", "DAY: " + res.getString(3));
        }
*/
        // Wyświetlenie powitania z nazwą użytkownika
        /*if (userName != null) {
            welcomeTextView.setText("Witaj, " + userName + "!");
        } else {
            welcomeTextView.setText("Witaj, użytkowniku!");
        }*/

        /*ArrayList<Entry> entries = new ArrayList<>();
        for (int x = -10; x <= 10; x++) {
            float y = x * x;  // Funkcja y = x^2
            entries.add(new Entry(x, y));
        }

        // Tworzenie zestawu danych
        LineDataSet lineDataSet = new LineDataSet(entries, "y = x^2");
        lineDataSet.setColor(getResources().getColor(R.color.purple_200));
        lineDataSet.setLineWidth(2f);

        // Dodanie danych do wykresu
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // Odśwież wykres
        lineChart.invalidate();*/

        btnDanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, ViewDangerActivity.class);
                startActivity(intent);
            }
        });

        btnViewDangers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, ViewDangerActivity.class);
                startActivity(intent);
            }
        });

        btnTelephone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, TelefonActivity.class);
                startActivity(intent);
            }
        });

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        btnGoDanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, DangerActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
            }
        });

    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Jeśli brak uprawnienia, prosimy o jego przyznanie
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Metoda do wczytywania danych do wykresu
    /*private void loadDistanceDataForChart() {
        // Pobierz dane dystansów i dat z bazy danych
        Cursor cursor = dbHelper.getAllDistances(); // Zakładam, że masz metodę, która pobiera wszystkie rekordy
        Log.e("dziala", String.valueOf(4));

        // Lista Entry do trzymania danych wykresu
        List<Entry> entries = new ArrayList<>();

        // Indeks do używania w Entry (numer dnia)
        int index = 5; // Rozpocznij numerację dni od 5

        // Sprawdzenie, czy cursor ma dane
        if (cursor.moveToFirst()) {
            // Indeks kolumny, upewnij się, że są różne od -1
            int distanceIndex = cursor.getColumnIndex("DISTANCE");
            int dateIndex = cursor.getColumnIndex("DAY");

            // Sprawdzamy, czy indeksy są poprawne (czy nie są -1)
            if (distanceIndex != -1) {
                do {
                    // Pobieranie wartości z bazy danych
                    int distance = cursor.getInt(distanceIndex); // Pobieranie wartości dystansu
                    Log.e("dismag", String.valueOf(distance));

                    // Dodanie punktu na wykresie: oś X to numer dnia, oś Y to dystans
                    entries.add(new Entry(index, distance));
                    index++; // Zwiększ numer dnia

                } while (cursor.moveToNext() && index <= 10); // Pętla działa tylko do 10. dnia
            } else {
                // Obsługa błędu, jeśli któryś z indeksów jest -1
                Log.e("UserActivity", "Błąd: Kolumna 'DISTANCE' nie została znaleziona.");
                Toast.makeText(this, "Błąd: Kolumny w bazie danych nie zostały znalezione.", Toast.LENGTH_SHORT).show();
            }
        }

        // Tworzenie zbioru danych dla wykresu
        LineDataSet dataSet = new LineDataSet(entries, "Dystans (metry)");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // Ustawienie kolorów dla linii
        dataSet.setLineWidth(2f); // Grubość linii
        dataSet.setValueTextSize(10f); // Rozmiar tekstu na wykresie
        dataSet.setDrawCircles(true); // Ustawienie widocznych punktów na linii
        dataSet.setCircleColor(ColorTemplate.COLORFUL_COLORS[0]); // Kolor punktów na linii

        // Tworzenie danych dla wykresu
        LineData data = new LineData(dataSet);
        Log.e("dane", String.valueOf(data));

        // Tworzenie i ustawienie danych na wykresie
        lineChart.setData(data);
        lineChart.invalidate(); // Odśwież wykres

        // Ustawienia osi X (numer dnia)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(5f);  // Minimum na osi X (5. dzień)
        xAxis.setAxisMaximum(10f); // Maksimum na osi X (10. dzień)
        xAxis.setGranularity(1f);  // Zapewnienie, że każda jednostka na osi X to jeden dzień
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Oś X na dole wykresu

        // Ustawienia osi Y (dystans)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);   // Minimum na osi Y (dystans = 0)
        leftAxis.setAxisMaximum(2000f); // Maksimum na osi Y (dystans = 2000)
        leftAxis.setGranularity(500f);  // Skok co 500 jednostek (opcjonalnie)

        // Ustawienia prawej osi Y, jeśli nie chcesz jej używać
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Wyłączenie prawej osi Y (opcjonalne)
    }
*/
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
                                    Toast.makeText(UserActivity.this, "Dodano zagrożenie z ID: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                                    Log.d("Firestore", "Dodano dokument o ID: " + documentReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreError", "Błąd przy dodawaniu zagrożenia", e);
                                    Toast.makeText(UserActivity.this, "Błąd przy dodawaniu zagrożenia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Nie znaleziono użytkownika.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Błąd przy pobieraniu danych użytkownika", e);
                    Toast.makeText(this, "Błąd przy pobieraniu danych użytkownika: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(UserActivity.this, "Nie można uzyskać bieżącej lokalizacji.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
