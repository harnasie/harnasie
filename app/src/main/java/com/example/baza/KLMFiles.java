package com.example.baza;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class KLMFiles {

    private static final String TAG = "KMLDownloader";

    private FirebaseStorage storage;
    private Context context;
    private GoogleMap mMap;
    private List<Polyline> drawnPolylines = new ArrayList<>();
    public boolean widoczne = false;
    public boolean widoczneszczyty = false;
    public boolean widocznestawy = false;
    private List<String> szlaki = new ArrayList<>();

    List<Marker> stawy = new ArrayList<>();
    List<Marker> szczyty = new ArrayList<>();
    List<Marker> schroniska = new ArrayList<>();


    public KLMFiles(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
        this.storage = FirebaseStorage.getInstance();
        enablePolylineClickListener();


    }

    public List<Marker> getStawy() {
        return stawy;
    }

    public List<Marker> getSzczyty() {
        return szczyty;
    }

    public List<Marker> getSchroniska() {
        return schroniska;
    }

    public List<String> getSzlaki() {
        Log.d("pobirwaszlkaki", String.valueOf(szlaki.size()));
        return szlaki;
    }

    public void downloadAllKMLFiles() {
        // Odwołanie do folderu, w którym znajdują się pliki KML
        StorageReference folderRef = storage.getReference().child("szlaki");

        // Pobranie listy plików w folderze
        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        //downloadKMLFile(fileRef);
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd podczas pobierania listy plików", exception));
    }

    private void downloadKMLFile(StorageReference fileRef, String folderName) {
        // Utwórz lokalny folder (np. „szlaki” lub „szlaki2”), jeśli nie istnieje
        File localDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        // Utwórz plik w lokalnym folderze z nazwą taką samą jak na Firebase
        File localFile = new File(localDir, fileRef.getName());

        // Pobierz plik z Firebase Storage
        fileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Pobrano plik: " + fileRef.getName() + " do folderu: " + folderName))
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd pobierania pliku: " + fileRef.getName(), exception));
    }

    // Funkcja do pobierania i przetwarzania plików .kml z folderów szlaki i szlaki2
    public void processKMLFiles() {
        // Przetwarzaj pliki z folderu „szlaki”
        processKMLFilesFromFolder("szlaki");

        // Przetwarzaj pliki z folderu „szlaki2”
        processKMLFilesFromFolderMarker("marker");
    }

    private void processKMLFilesFromFolder(String folderName) {
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);

        // Sprawdź, czy folder istnieje i jest katalogiem
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Sprawdź, czy plik jest plikiem .kml
                    if (file.isFile() && file.getName().endsWith(".kml")) {
                        Log.d(TAG, "Znalazłem plik: " + file.getAbsolutePath());
                        // Odczytaj zawartość pliku KML
                        readAndProcessKMLFile1(file);
                        szlaki.add(file.getName());
                        Log.d(TAG, "Znalazłem plik: " + szlaki.size());
                        //drawKMLPolylines(file);

                    }
                }
            } else {
                Log.d(TAG, "Brak plików w folderze: " + folderName);
            }
        } else {
            Log.d(TAG, "Folder '" + folderName + "' nie istnieje lub nie jest katalogiem.");
        }
    }

    public void VisibilityMarker(String name, boolean visi) {
        Log.d("marker", name);
        switch (name) {
            case "stawy":
                for (Marker marker : stawy) {
                    marker.setVisible(visi);
                }
                break;
            case "szczyty":
                for (Marker marker : szczyty) {
                    marker.setVisible(visi);
                }
                break;
            case "schroniska":
                Log.d("VisibilityMarker", "Number of markers in schroniska: " + schroniska.size());

                for (Marker marker : schroniska) {
                    Log.d("asdfg","sss");
                    marker.setVisible(visi);
                }
                break;
        }
    }

    private void processKMLFilesFromFolderMarker(String folderName) {
        Bitmap iconBitmap = null;
        Bitmap resizedBitmap = null;
        BitmapDescriptor icon = null;
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);

        // Sprawdź, czy folder istnieje i jest katalogiem
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Sprawdź, czy plik jest plikiem .kml
                    if (file.isFile() && file.getName().endsWith(".kml")) {
                        Log.d(TAG, "Znalazłem plik: " + file.getAbsolutePath());
                        // Odczytaj zawartość pliku KML
                        //addMarkerFromKML(file);
                        Log.d("nazwa", file.getName());
                        switch (file.getName()) {
                            case "stawy.kml":
                                iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.location_pin);
                                resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false); // Rozmiar 100x100 pikseli
                                icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                                stawy = addMarkersFromKML(file.getAbsolutePath(),icon);
                                break;
                            case "szczyty.kml":
                                iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snowed_mountains);
                                resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false); // Rozmiar 100x100 pikseli
                                icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                                szczyty = addMarkersFromKML(file.getAbsolutePath(), icon);
                                break;
                            case "schroniska.kml":
                                Log.d("qqqqq", "marker.getTitle()");
                                iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.tent);
                                resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false); // Rozmiar 100x100 pikseli
                                icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                                schroniska = addMarkersFromKML(file.getAbsolutePath(), icon);
                                //for (Marker marker : schroniska){
                                Log.d("qqqqq", String.valueOf(schroniska.get(6).getTitle()));
                                    Log.d("qqqqq", String.valueOf(schroniska.size()));
                                //}
                                break;

                        }
                        //addMarkersFromKML(file.getAbsolutePath(),icon);
                    }
                }
            } else {
                Log.d(TAG, "Brak plików w folderze: " + folderName);
            }
        } else {
            Log.d(TAG, "Folder '" + folderName + "' nie istnieje lub nie jest katalogiem.");
        }
    }


    public static List<LatLng> getRouteStartEnd(String filename) {
        List<LatLng> coordinates = new ArrayList<>();
        try {
            // Load the KML file from the given file path
            File file = new File("/storage/emulated/0/Android/data/com.example.baza/files/Documents/szlaki/"+filename);
            InputStream inputStream = new FileInputStream(file);

            // Parse the KML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            // Find <coordinates> tags
            NodeList coordinatesNodes = document.getElementsByTagName("coordinates");

            for (int i = 0; i < coordinatesNodes.getLength(); i++) {
                String coordinatesText = coordinatesNodes.item(i).getTextContent().trim();

                // Split into individual coordinate entries
                String[] points = coordinatesText.split("\\s+");
                for (String point : points) {
                    String[] latLngAlt = point.split(",");
                    double longitude = Double.parseDouble(latLngAlt[0]);
                    double latitude = Double.parseDouble(latLngAlt[1]);
                    coordinates.add(new LatLng(latitude, longitude));
                }
            }

            if (coordinates.isEmpty()) {
                Log.e("KMLParserFromFile", "No coordinates found in the KML file.");
            }

        } catch (Exception e) {
            Log.e("KMLParserFromFile", "Error parsing KML file: " + e.getMessage(), e);
        }

        // Return start and end coordinates
        List<LatLng> result = new ArrayList<>();
        if (!coordinates.isEmpty()) {
            result.add(coordinates.get(0)); // Start
            result.add(coordinates.get(coordinates.size() - 1)); // End
        }
        return result;
    }

    public void enablePolylineClickListener() {
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline clickedPolyline) {
                // Przetwarzaj kliknięcie na Polyline
                Toast.makeText(context, "Kliknięto trasę!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Kliknięto Polyline: " + clickedPolyline.getId());
            }
        });
    }

    private void drawKMLPolylines(File kmlFile) {
        try {
            // Otwórz plik KML
            FileInputStream fileInputStream = new FileInputStream(kmlFile);

            // Wczytaj warstwę KML
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, context);

            // Iteracja po kontenerach KML i placemarkach
            for (KmlContainer container : kmlLayer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    // Sprawdź, czy geometria placemarka to KmlLineString (linia)
                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        List<LatLng> coordinates = lineString.getGeometryObject();

                        // Tworzymy PolylineOptions z odpowiednimi współrzędnymi
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(coordinates)
                                .color(Color.RED)  // Możesz ustawić dowolny kolor
                                .width(5f); // Ustawienia szerokości linii

                        // Dodaj Polyline do mapy
                        Polyline polyline = mMap.addPolyline(polylineOptions);
                        polyline.setClickable(true); // Możesz ustawić kliknięcie na Polyline

                        // Dodaj listener dla kliknięcia Polyline (opcjonalnie)
                        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                            @Override
                            public void onPolylineClick(Polyline clickedPolyline) {
                                // Wykonaj jakąś akcję po kliknięciu na Polyline
                                Toast.makeText(context, "Kliknięto trasę", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e("KML", "Błąd podczas odczytu pliku: " + e.getMessage());
        }
    }


    // Funkcja do odczytu i przetwarzania pliku .kml
    private void readAndProcessKMLFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, context);

            // Iteracja po kontenerach KML i placemarkach
            for (KmlContainer container : kmlLayer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    String name = placemark.getProperty("name"); // Pobierz nazwę placemarka
                    Log.d("KML", "Nazwa: " + name);

                    int color = Color.GRAY; // Domyślny kolor
                    if (name != null) { // Ustal kolor w zależności od nazwy
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

                    // Sprawdzenie, czy geometria placemarka to KmlLineString
                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        List<LatLng> coordinates = lineString.getGeometryObject();
                        Log.d("KML", "Współrzędne linii: " + coordinates);

                        // Tworzenie PolylineOptions z odpowiednim kolorem
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(coordinates)
                                .color(color)
                                .width(5f);

                        // Dodanie Polyline do mapy
                        Polyline polyline = mMap.addPolyline(polylineOptions);
                        polyline.setClickable(true); // Ustawienie jako klikalne

                        // Dodanie listenera dla kliknięcia na Polyline
                        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                            @Override
                            public void onPolylineClick(Polyline clickedPolyline) {
                                if (clickedPolyline.equals(polyline)) {
                                    Toast.makeText(context, "Kliknięto trasę: " + name, Toast.LENGTH_SHORT).show();
                                    Log.d("PolylineClick", "Kliknięto trasę o nazwie: " + name);
                                }
                            }
                        });
                        drawnPolylines.add(polyline); // Przechowywanie Polyline w liście
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Błąd podczas odczytu pliku: " + e.getMessage());
        }
    }


    // Funkcja do odczytu pliku KML i zwrócenia mapy z Polyline i RouteInfo
    public Map<Polyline, Integer> readAndProcessKMLFile111(File file) {
        Map<Polyline, Integer> routeMap = new HashMap<>(); // Mapa dla Polyline i RouteInfo
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, context);

            // Iteracja po kontenerach KML i placemarkach
            for (KmlContainer container : kmlLayer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    String name = placemark.getProperty("name"); // Pobierz nazwę placemarka
                    Log.d("KML", "Nazwa: " + name);

                    // Sprawdzenie, czy geometria placemarka to KmlLineString
                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        List<LatLng> coordinates = lineString.getGeometryObject();
                        Log.d("KML", "Współrzędne linii: " + coordinates);

                        // Ustalenie koloru trasy
                        int color = getColorByName(name);

                        // Tworzenie PolylineOptions z odpowiednim kolorem
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(coordinates)
                                .color(color)
                                .width(5f);

                        // Dodanie Polyline do mapy
                        Polyline polyline = mMap.addPolyline(polylineOptions);
                        polyline.setClickable(true); // Ustawienie jako klikalne

                        // Dodanie listenera dla kliknięcia na Polyline
                        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                            @Override
                            public void onPolylineClick(Polyline clickedPolyline) {
                                if (clickedPolyline.equals(polyline)) {
                                    Toast.makeText(context, "Kliknięto trasę: " + name, Toast.LENGTH_SHORT).show();
                                    Log.d("PolylineClick", "Kliknięto trasę o nazwie: " + name);
                                }
                            }
                        });

                        // Tworzymy obiekt RouteInfo i dodajemy go do mapy
                        //RouteInfo routeInfo = new RouteInfo(polyline, name, color);
                        routeMap.put(polyline, color); // Mapujemy Polyline na RouteInfo
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return routeMap; // Zwracamy mapę Polyline -> RouteInfo
    }

    private void readAndProcessKMLFile1(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            // Wczytaj warstwę KML
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, context);
            // Dodaj warstwę KML do mapy
            kmlLayer.addLayerToMap();

            //kmlLayer.setMap(mMap);

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

    private int getColorByName(String name) {
        int color = Color.GRAY; // Domyślny kolor
        if (name != null) { // Ustal kolor w zależności od nazwy
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
        return color;
    }


    public Polyline findClosestRoute(LatLng clickedPoint) {
        double minDistance = Double.MAX_VALUE;
        Polyline closestRoute = null;

        for (Polyline polyline : drawnPolylines) {
            for (LatLng point : polyline.getPoints()) {
                double distance = calculateDistance(clickedPoint, point);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestRoute = polyline;
                }
            }
        }

        return closestRoute;
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        final int R = 6371; // Promień Ziemi w kilometrach

        double latDistance = Math.toRadians(point2.latitude - point1.latitude);
        double lonDistance = Math.toRadians(point2.longitude - point1.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Wynik w metrach
    }


    private List<Marker> addMarkersFromKML(String absolutePath, BitmapDescriptor icon) {
        List<Marker> markersList = new ArrayList<>();

        try {
            // Otwórz plik KML z folderu assets
            InputStream kmlInputStream = new FileInputStream(absolutePath);

            // Analiza KML
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(kmlInputStream, "UTF-8");

            String name = null;
            String description = null;
            LatLng coordinates = null;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("name".equals(tagName)) {
                            name = parser.nextText();
                        } else if ("description".equals(tagName)) {
                            description = parser.nextText();
                        } else if ("coordinates".equals(tagName)) {
                            String[] coord = parser.nextText().split(",");
                            double lng = Double.parseDouble(coord[0]);
                            double lat = Double.parseDouble(coord[1]);
                            coordinates = new LatLng(lat, lng);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("Placemark".equals(tagName)) {
                            if (coordinates != null) {
                                // Dodanie markera na mapę z niestandardową ikoną
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(coordinates)
                                        .title(name)
                                        .snippet(description)
                                        .visible(false)
                                        .icon(icon)); // Użyj ikony z drawable
                                coordinates = null;
                                if (marker != null) {
                                    markersList.add(marker);
                                    Log.d(marker.getTitle(),"sdfghj");
                                }

                            }
                        }
                        break;
                }

                eventType = parser.next();
            }

            // Ustawienie kamery na pierwszy marker
            if (mMap != null && coordinates != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 10));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return markersList;
    }

}
