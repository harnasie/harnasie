package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChartActivity extends AppCompatActivity {
    private BarChart barChart;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        setTitle("Wykres pokonanych dystansów");
        barChart = findViewById(R.id.barChart);
        db = FirebaseFirestore.getInstance();
        Intent getintent = getIntent();
        String userName = getintent.getStringExtra("username");
        String uid = getintent.getStringExtra("uid");
        Log.d("iertyu",uid);
        fetchAndDisplayData(uid);
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

                                    // Dodaj dane do wykresu
                                    barEntries.add(new BarEntry(index, distance.floatValue()));
                                    labels.add(uid); // Dodaj datę jako etykietę
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

    private String formatDate(String dateString) {
        // Definiowanie formatu daty, który pasuje do zapisanego formatu w Firebase
        SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a z", Locale.getDefault());
        firebaseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Ustawiamy strefę czasową na UTC

        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()); // Format wyświetlania

        try {
            Date date = firebaseDateFormat.parse(dateString);
            if (date != null) {
                return displayDateFormat.format(date); // Zwracamy datę w formacie "dd MMM yyyy"
            }
        } catch (ParseException e) {
            Log.e("DateError", "Error parsing date", e);
        }
        return dateString; // Zwracamy oryginalną datę w przypadku błędu
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

        // Odświeżenie wykresu
        barChart.invalidate();
    }
}
