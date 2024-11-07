package com.example.baza;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class KMLDownloader {

    private static final String TAG = "KMLDownloader";
    private Context context;
    private GoogleMap mMap;

    public KMLDownloader(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
    }

    // Funkcja do pobierania i przetwarzania plików .kml z folderu szlaki
    public void processKMLFiles() {
        File szlakiDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "szlaki");

        // Sprawdź, czy folder istnieje i jest katalogiem
        if (szlakiDir.exists() && szlakiDir.isDirectory()) {
            File[] files = szlakiDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Sprawdź, czy plik jest plikiem .kml
                    if (file.isFile() && file.getName().endsWith(".kml")) {
                        Log.d(TAG, "Znalazłem plik: " + file.getAbsolutePath());
                        // Odczytaj zawartość pliku KML
                        readAndProcessKMLFile(file);
                    }
                }
            } else {
                Log.d(TAG, "Brak plików w folderze.");
            }
        } else {
            Log.d(TAG, "Folder 'szlaki' nie istnieje lub nie jest katalogiem.");
        }
    }

    // Funkcja do odczytu i przetwarzania pliku .kml
    private void readAndProcessKMLFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            // Wczytaj warstwę KML
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, context);

            // Dodaj warstwę KML do mapy
            kmlLayer.addLayerToMap();

            // Przetwarzaj kliknięcia na elementy KML
            kmlLayer.setOnFeatureClickListener(feature -> {
                if (feature.getGeometry() instanceof KmlLineString) {
                    KmlLineString lineString = (KmlLineString) feature.getGeometry();
                    List<LatLng> points = lineString.getGeometryObject();

                    // Wyświetl nazwę trasy i dodaj marker w pobliżu pierwszego punktu linii
                    String routeName = feature.getProperty("name");

                    // Ustaw marker w pierwszym punkcie trasy dla przykładu
                    LatLng firstPoint = points.get(0);
                    mMap.addMarker(new MarkerOptions().position(firstPoint).title(routeName));
                }
            });

            // Iteracja po kontenerach KML i placemarkach
            for (KmlContainer container : kmlLayer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    // Pobierz nazwę placemarka
                    String name = placemark.getProperty("name");
                    int color = Color.GRAY; // Domyślny kolor

                    // Określ kolor na podstawie nazwy
                    if (name != null) {
                        if (name.contains("czerwony")) {
                            color = Color.RED;
                        } else if (name.contains("niebieski")) {
                            color = Color.BLUE;
                        } else if (name.contains("zolty")) {
                            color = Color.YELLOW;
                        } else if (name.contains("zielony")) {
                            color = Color.GREEN;
                        } else if (name.contains("czarny")) {
                            color = Color.BLACK;
                        }
                    }

                    // Tworzymy PolylineOptions, ale tylko raz
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(color)
                            .width(5f)
                            .clickable(true);

                    // Sprawdzenie, czy geometria placemarka to KmlLineString
                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        List<LatLng> coordinates = lineString.getGeometryObject();
                        Log.d("KML", coordinates.toString()); // Dodany log

                        // Dodajemy współrzędne do PolylineOptions
                        polylineOptions.addAll(coordinates);

                        // Dodajemy Polyline na mapie
                        mMap.addPolyline(polylineOptions);
                    }
                }
            }
            // Możesz także dodać inną funkcjonalność, np. rysowanie innych elementów KML

        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Błąd podczas odczytu pliku: " + e.getMessage());
        }
    }
}
