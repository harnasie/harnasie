package com.example.baza;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
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
        welcomeTextView = findViewById(R.id.welcomeTextView);
        routeInputLayout = findViewById(R.id.route_input_layout);
        btnWyloguj = findViewById(R.id.logout);
        db = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (userName == null || uid == null) {
            Intent getintent = getIntent();
            userName = getintent.getStringExtra("username");
            uid = getintent.getStringExtra("userId");
            Log.d("iertyu", uid);
        }

        if (userName != null) {
            welcomeTextView.setText("Witaj, " + userName + "!");
            fetchAndDisplayData(uid);
        } else {
            welcomeTextView.setText("Witaj, użytkowniku!");
        }


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
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        btnuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, UserActivity.class);
                intent.putExtra("uid", uid);
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
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void addDanger(String description, String type, String uid) {
        checkLocationPermission();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Brak uprawnień do lokalizacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("lokalizacja", String.valueOf(currentLocation));

                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", userDoc.getString("email"));
                        userMap.put("username", userDoc.getString("username"));
                        userMap.put("uid", uid);

                        Map<String, Object> danger = new HashMap<>();
                        danger.put("description", description);
                        danger.put("location", currentLocation);
                        danger.put("type", type);
                        danger.put("user", userMap);
                        danger.put("createdAt", Timestamp.now());
                        danger.put("accepted", false);


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
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<BarEntry> barEntries = new ArrayList<>();
                        ArrayList<String> labels = new ArrayList<>();
                        int index = 0;
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Double distance = document.getDouble("distance");
                                Date timeee = document.getDate("date");

                                int day = timeee.getDate();
                                int month = timeee.getMonth() + 1;
                                int year = timeee.getYear();
                                String datad = day + "." + month + "." + year;
                                barEntries.add(new BarEntry(index, distance.floatValue()));
                                labels.add(datad);
                                index++;

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
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.setFitBars(true);

        barChart.getDescription().setEnabled(false);

        barChart.getLegend().setEnabled(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);

        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + " km";
            }
        });

        barChart.invalidate();
    }
}