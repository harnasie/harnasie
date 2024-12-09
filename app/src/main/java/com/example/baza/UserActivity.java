package com.example.baza;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class UserActivity extends AppCompatActivity {
    private TextView welcomeTextView;
    FirebaseFirestore db;
    private LineChart lineChart;
    private LatLng currentLocation = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private String uid = null;
    private LinearLayout routeInputLayout;
    private LinearLayout menuLayout;
    private Button  btnWyloguj;
    private ImageButton btnuser, btndanger,btnTelefon,btnmap;
    private FrameLayout background;
    private String userName = null;
    private BarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setTitle("Konto użytkownika");
        barChart = findViewById(R.id.barChart);
        // Znalezienie TextView do wyświetlania wiadomości powitalnej
        welcomeTextView = findViewById(R.id.welcomeTextView);
        routeInputLayout = findViewById(R.id.route_input_layout);
        btnWyloguj = findViewById(R.id.logout);
        db = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //checkLocationPermission();
        // Odbieranie nazwy użytkownika przekazanej z LoginActivity
        if(userName == null || uid == null){
            Intent getintent = getIntent();
            userName = getintent.getStringExtra("username");
            uid = getintent.getStringExtra("userId");
            Log.d("iertyu",uid);
        }

        //Cursor res = dbHelper.getAllDistances();
        /*dbHelper.addDistance("Magda", "1000", "2023-09-01");
        dbHelper.addDistance("Magda", "4000", "2023-09-02");
        dbHelper.addDistance("Magda", "9000", "2023-09-03");
*/
        /*        Cursor res = dbHelper.getAllDistances();

         */
        // Wyświetlenie powitania z nazwą użytkownika
        if (userName != null) {
            welcomeTextView.setText("Witaj, " + userName + "!");
            fetchAndDisplayData(uid);
        } else {
            welcomeTextView.setText("Witaj, użytkowniku!");
        }

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
        btnuser = findViewById(R.id.userView);
        btnuser = findViewById(R.id.userView);
        btnTelefon = findViewById(R.id.buttonTelefon);
        btndanger = findViewById(R.id.danger);
        btnmap = findViewById(R.id.btnmap);
        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });


        btnTelefon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, TelefonActivity.class);
                startActivity(intent);
            }
        });

        btndanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, DangerActivity.class);
                startActivity(intent);
            }
        });

        btnuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        btnWyloguj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAndRedirect();
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


    private void logoutAndRedirect() {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void fetchAndDisplayData(String uid){
        db.collection("walking")
                .whereEqualTo("uid", uid) // Filtrujemy po polu "uid"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<BarEntry> barEntries = new ArrayList<>();
                        ArrayList<String> labels = new ArrayList<>();
                        int index = 0;
                        // Sprawdzamy, czy znaleziono dokumenty
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Pobieramy wartość "distance"
                                Double distance = document.getDouble("distance");
                                Date timeee = document.getDate("date");

                                int day = timeee.getDate();
                                int month = timeee.getMonth() + 1;
                                int year = timeee.getYear();
                                String datad = day + "." + month + "." + year;
                                // Dodaj dane do wykresu
                                barEntries.add(new BarEntry(index, distance.floatValue()));
                                labels.add(datad); // Dodaj datę jako etykietę
                                index++;


                                // Wyświetl dane na wykresie
                                showBarChart(barEntries, labels);
                            }
                        } else {
                            Log.d("Firebase", "No documents found for UID: " + uid);
                        }
                    } else {
                        Log.e("Firebase", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showBarChart(ArrayList<BarEntry> barEntries, ArrayList<String> labels) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "Distance");
        barDataSet.setColor(getResources().getColor(android.R.color.holo_blue_light)); // Ustaw kolor słupków

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f); // Szerokość słupków

        // Ustawienie danych wykresu
        barChart.setData(barData);
        barChart.setFitBars(true);

        // Wyłączenie opisu wykresu
        barChart.getDescription().setEnabled(false);

        // Wyłączenie legendy
        barChart.getLegend().setEnabled(false);

        // Ustawienie etykiet na osi X
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);

        // Wyłączenie pionowych linii siatki na osi Y
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);

        // Wyłączenie prawej osi Y (jeśli nie jest potrzebna)
        barChart.getAxisRight().setEnabled(false);

        // Dodanie jednostki "km" do osi Y
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + " km";  // Dodajemy jednostkę km do wartości
            }
        });

        // Odświeżenie wykresu
        barChart.invalidate();
    }

}