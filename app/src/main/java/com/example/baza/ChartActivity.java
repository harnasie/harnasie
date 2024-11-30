package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ChartActivity extends AppCompatActivity {
    private BarChart barChart;
    private FirebaseFirestore db;
    private ImageButton btnchart, btnuser, btndanger,btnTelefon, btnmap;
    private String userName = null, uid= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        setTitle("Wykres pokonanych dystansów");
        barChart = findViewById(R.id.barChart);
        db = FirebaseFirestore.getInstance();
        if(userName == null || uid == null){
        Intent getintent = getIntent();
        userName = getintent.getStringExtra("username");
        uid = getintent.getStringExtra("uid");
        Log.d("iertyu",uid);
        fetchAndDisplayData(uid);}

        btnchart = findViewById(R.id.chart);
        btnuser = findViewById(R.id.userView);
        btnTelefon = findViewById(R.id.buttonTelefon);
        btndanger = findViewById(R.id.danger);
        btnmap = findViewById(R.id.btnmap);

        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });


        btnchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });

        btnTelefon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartActivity.this, TelefonActivity.class);
                startActivity(intent);
            }
        });



        btndanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartActivity.this, DangerActivity.class);
                startActivity(intent);
            }
        });

        btnuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

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
