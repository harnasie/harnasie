package com.example.baza;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class KLMFiles {

    private static final String TAG = "KMLDownloader";

    private FirebaseStorage storage;
    private Context context;
    private GoogleMap mMap;


    public KLMFiles(Context context,GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
        this.storage = FirebaseStorage.getInstance();


    }

    public void downloadAllKMLFiles() {
        // Odwołanie do folderu, w którym znajdują się pliki KML
        StorageReference folderRef = storage.getReference().child("szlaki");

        // Pobranie listy plików w folderze
        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        downloadKMLFile(fileRef);
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd podczas pobierania listy plików", exception));
    }

    private void downloadKMLFile(StorageReference fileRef) {
        // Utwórz lokalny folder „szlaki”, jeśli nie istnieje
        File localDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "szlaki");
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        // Utwórz plik w lokalnym folderze z nazwą taką samą jak na Firebase
        File localFile = new File(localDir, fileRef.getName());

        // Pobierz plik z Firebase Storage
        fileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Pobrano plik: " + fileRef.getName()))
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd pobierania pliku: " + fileRef.getName(), exception));
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
                    Log.d("KML", "Nazwa: " + name);

                    // Określ kolor na podstawie nazwy
                    int color = Color.GRAY; // Domyślny kolor
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

                    // Tworzymy PolylineOptions z odpowiednim kolorem
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(color)
                            .width(5f);

                    // Sprawdzenie, czy geometria placemarka to KmlLineString
                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        List<LatLng> coordinates = lineString.getGeometryObject();
                        Log.d("KML", coordinates.toString());

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
