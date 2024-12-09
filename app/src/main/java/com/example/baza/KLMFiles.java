package com.example.baza;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
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

    private Context context;
    private GoogleMap mMap;
    private List<Polyline> drawnPolylines = new ArrayList<>();
    private List<String> szlaki = new ArrayList<>();
    private List<Marker> stawy = new ArrayList<>();
    private List<Marker> szczyty = new ArrayList<>();
    private List<Marker> schroniska = new ArrayList<>();


    public KLMFiles(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;


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


    // Funkcja do pobierania i przetwarzania plików .kml z folderów szlaki i szlaki2
    public void processKMLFiles() {
        processKMLFilesFromFolder("szlaki");
        processKMLFilesFromFolderMarker("marker");
    }

    private void processKMLFilesFromFolder(String folderName) {
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);

        // Sprawdź, czy folder istnieje i jest katalogiem
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".kml")) {
                        Log.d(TAG, "Znalazłem plik: " + file.getAbsolutePath());
                        // Odczytaj zawartość pliku KML
                        readAndProcessKMLFile1(file);
                        szlaki.add(file.getName());
                        Log.d(TAG, "Znalazłem plik: " + szlaki.size());

                    }
                }
            } else {
                Log.d(TAG, "Brak plików w folderze: " + folderName);
            }
        } else {
            Log.d(TAG, "Folder '" + folderName + "' nie istnieje lub nie jest katalogiem.");
        }
    }

    private void processKMLFilesFromFolderMarker(String folderName) {
        Bitmap iconBitmap = null;
        Bitmap resizedBitmap = null;
        BitmapDescriptor icon = null;
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".kml")) {
                        switch (file.getName()) {
                            case "stawy.kml":
                                iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.location_pin);
                                resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false);
                                icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                                stawy = addMarkersFromKML(file.getAbsolutePath(),icon);
                                break;
                            case "szczyty.kml":
                                iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snowed_mountains);
                                resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false);
                                icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                                szczyty = addMarkersFromKML(file.getAbsolutePath(), icon);
                                break;
                            case "schroniska.kml":
                                iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.tent);
                                resizedBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false);
                                icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                                schroniska = addMarkersFromKML(file.getAbsolutePath(), icon);
                                break;

                        }
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

        List<LatLng> result = new ArrayList<>();
        if (!coordinates.isEmpty()) {
            result.add(coordinates.get(0)); // Start
            result.add(coordinates.get(coordinates.size() - 1)); // End
            result.add(coordinates.get(27));
        }
        return result;
    }


    private void readAndProcessKMLFile1(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, context);
            kmlLayer.addLayerToMap();
            for (KmlContainer container : kmlLayer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    String name = placemark.getProperty("name");

                    int color = Color.GRAY;
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

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(color)
                            .width(5f);

                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        List<LatLng> coordinates = lineString.getGeometryObject();
                        polylineOptions.addAll(coordinates);
                        mMap.addPolyline(polylineOptions);
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Błąd podczas odczytu pliku: " + e.getMessage());
        }
    }

    private List<Marker> addMarkersFromKML(String absolutePath, BitmapDescriptor icon) {
        List<Marker> markersList = new ArrayList<>();

        try {
            InputStream kmlInputStream = new FileInputStream(absolutePath);
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
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(coordinates)
                                        .title(name)
                                        .snippet(description)
                                        .visible(false)
                                        .icon(icon));
                                coordinates = null;
                                if (marker != null) {
                                    markersList.add(marker);
                                }

                            }
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return markersList;
    }
}
