/*
package com.example.baza;

import android.content.Context;
import android.content.res.AssetManager;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class KMLDownloader {

    private static final String TAG = "KMLDownloader";
    private Context context;
    private GoogleMap mMap;

    public KMLDownloader(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
    }

    // Zastąp ścieżkę folderu szlaki w katalogu publicznym
    public void processKMLFiles() {
        File szlakiDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "szlaki");

        // Sprawdź, czy folder istnieje, jeśli nie - utwórz go
        if (!szlakiDir.exists()) {
            boolean isCreated = szlakiDir.mkdirs();
            if (isCreated) {
                Log.d(TAG, "Folder 'szlaki' został utworzony.");
            } else {
                Log.e(TAG, "Nie udało się utworzyć folderu 'szlaki'.");
            }
        }

        // Sprawdź, czy folder jest katalogiem i przeprocesuj pliki KML
        if (szlakiDir.isDirectory()) {
            File[] files = szlakiDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".kml")) {
                        Log.d(TAG, "Znalazłem plik: " + file.getAbsolutePath());
                        readAndProcessKMLFile(file);
                    }
                }
            } else {
                Log.d(TAG, "Brak plików w folderze.");
            }
        } else {
            Log.d(TAG, "Folder 'szlaki' nie jest katalogiem.");
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

    private void downloadKMLFile(String urlString, String fileName) {
        File szlakiDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "szlaki");
        if (!szlakiDir.exists() && !szlakiDir.mkdirs()) {
            Log.e(TAG, "Nie udało się utworzyć folderu 'szlaki'.");
            return;
        }

        File file = new File(szlakiDir, fileName);

        try (BufferedInputStream in = new BufferedInputStream(new URL(urlString).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            Log.d(TAG, "Pobrano plik: " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Błąd pobierania pliku: " + e.getMessage());
        }
    }

    public void copyKMLFilesFromAssets() {
        File szlakiDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "szlaki");
        if (!szlakiDir.exists() && !szlakiDir.mkdirs()) {
            Log.e(TAG, "Nie udało się utworzyć folderu 'szlaki'.");
            return;
        }

        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list("szlaki");
            if (files != null) {
                for (String fileName : files) {
                    File outFile = new File(szlakiDir, fileName);
                    if (!outFile.exists()) {
                        try (InputStream in = assetManager.open("szlaki/" + fileName);
                             FileOutputStream out = new FileOutputStream(outFile)) {

                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                            Log.d(TAG, "Skopiowano plik: " + fileName);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Błąd podczas kopiowania plików: " + e.getMessage());
        }
    }


}
*/
