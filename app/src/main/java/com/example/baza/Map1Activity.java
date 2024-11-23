package com.example.baza;

import static java.security.AccessController.getContext;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;

import java.io.IOException;
import java.util.Map;

public class Map1Activity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> schroniska = new ArrayList<>();
    private List<Marker> szczyty = new ArrayList<>();
    private List<Marker> stawy = new ArrayList<>();
    private LocationCallback mLocationCallback;
    private LatLng lastLocation = null;
    private LatLng firstLocation = null;
    private int lastTraversedIndex = -1;
    private List<LatLng> przystanki = new ArrayList<>();
    private List<LatLng> waypointsList = new ArrayList<>();
    private Map<String, Marker> markers = new HashMap<>();
    private static final String TAG = "KMLDownloader";
    private FirebaseFirestore db;
    private LinearLayout topBar;
    private FirebaseStorage storage;
    private FrameLayout background;
    private int ile = 1;
    private Button btnskad, RouteButton, btndokad, btnstop1, btnstop2, btnstop3;
    private int ileszczyty = 1;
    private int ilestawy = 1;
    private ImageView markerView;
    private ArrayList<LatLng> trasa = new ArrayList<>();
    private int[] point = {0, 0, 0, 0, 0};
    private LatLng currentLocation = null, skad_location = null, dokad_location = null,
            stop1_location = null, stop2_location = null, stop3_location = null;
    private long navigationStartTime = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private EditText skad_et, dokad_et, stop1, stop2, stop3;
    private Button button_addstop, btnconfirm;
    private PolylineOptions lineOptions; // = new PolylineOptions();
    private static final double DISTANCE_THRESHOLD_METERS = 2.0; // 1 meter
    private FusedLocationProviderClient mFusedLocationClient;
    private TextToSpeech tts;
    private EditText[] stops = {stop1, stop2, stop3};
    private LinearLayout routeInputLayout,menuLayout,mainLayout;
    private Handler handler;
    private boolean widoczne = false;
    private boolean widoczneszczyty = false;
    private boolean widocznestawy = false;
    private LatLng destination = new LatLng(51.27447, 22.55371);// Zakopane
    private LatLng destination1 = new LatLng(49.2598, 19.9667);//20.09156,49.23821
    private List<Polyline> clickedPolylines = new ArrayList<>();
    private List<LatLng> routePoints = new ArrayList<>();
    private List<Polyline> routePolylines = new ArrayList<>();
    private Polyline currentRoutePolyline = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private Button btnchart, btndanger, btnTelefon, btnmap, btnuser, btnMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setTitle("Mapa");
        db = FirebaseFirestore.getInstance();

        // Inicjalizacja mapy
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);

        // Inicjalizacja klienta lokalizacji
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler(Looper.getMainLooper());
        // Inicjalizacja Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.UK);
            }
        });

        markerView = findViewById(R.id.marker_view);

        skad_et = findViewById(R.id.place1);
        dokad_et = findViewById(R.id.place2);

        menuLayout = findViewById(R.id.menuLayout);
        btnchart = findViewById(R.id.chart);
        btnuser = findViewById(R.id.userView);
        btnTelefon = findViewById(R.id.buttonTelefon);
        btndanger = findViewById(R.id.danger);
        btnMenu = findViewById(R.id.showMenuButton);
        background = findViewById(R.id.background);
        btnmap = findViewById(R.id.btnmap);

        stop1 = findViewById(R.id.placestop1);
        stop2 = findViewById(R.id.placestop2);
        stop3 = findViewById(R.id.placestop3);


        skad_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkerAtCurrentPosition(true);
                point[0] = 1;
                Log.e("skad","skas");
                routeInputLayout.setVisibility(View.GONE);
            }
        });

        dokad_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkerAtCurrentPosition(true);
                point[1] = 1;
                Log.e("dokad","skas");
                routeInputLayout.setVisibility(View.GONE);
            }
        });

        button_addstop = findViewById(R.id.button_stop);
        button_addstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText[] stops = {stop1, stop2, stop3};
                Button[] buttons = {btnstop1, btnstop2, btnstop3};
                LatLng[] locations = {stop1_location, stop2_location, stop3_location};

                for (int i = 0; i < stops.length; i++) {
                    if (stops[i].getVisibility() == View.GONE) {
                        stops[i].setVisibility(View.VISIBLE);
                        buttons[i].setVisibility(View.VISIBLE);
                        final int index = i; // Musisz stworzyć lokalną zmienną, aby nie stracić odniesienia w lambdach
                        stops[i].setOnClickListener(v1 -> {
                            addMarkerAtCurrentPosition(true);
                            point[index+2]=1;
                            Log.e("przynste", String.valueOf(index+1));
                            routeInputLayout.setVisibility(View.GONE);
                        });

                        /*buttons[i].setOnClickListener(v12-> {
                            LatLng stopLocation = addMarkerAtCenter("przystanek"+(index+1));
                            Log.d("locationnnnnn", String.valueOf(stopLocation));
                            if (stopLocation != null) {
                                stops[index].setText(getCityAndStreetFromCoordinates(stopLocation));
                                waypointsList.add(stopLocation);

                            }
                            addMarkerAtCurrentPosition(false);
                            Log.d("location", String.valueOf(stopLocation));
                        });*/
                        break; // Wyjdź z pętli po ustawieniu widoczności
                    }
                }
            }
        });


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    // Update the map with the user's location
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    if (lastLocation == null || !userLocation.equals(lastLocation)) {
                        lastLocation = userLocation;
                        updateLocationOnMap(userLocation);
                        showCurrentSegment(userLocation);
                    }
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setOnMapClickListener(this::onMapClick);
        LatLng initialLocation = new LatLng(49.2992, 19.9496);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //loadKmlLayers();
        //KMLDownloader kmlDownloader = new KMLDownloader(this, mMap); // 'this' to kontekst, np. Activity
        //kmlDownloader.processKMLFiles();
        //kmlDownloader.copyKMLFilesFromAssets();
        PolylineOptions testPolylineOptions = new PolylineOptions()
                .add(new LatLng(37.7749, -122.4194)) // Start
                .add(new LatLng(34.0522, -118.2437)) // End
                .color(Color.BLUE)
                .width(10f);
        Polyline testPolyline = mMap.addPolyline(testPolylineOptions);
        testPolyline.setClickable(true);

        KLMFiles kmlfiles = new KLMFiles(this,mMap);
        //downloadAllKMLFiles();
        kmlfiles.processKMLFiles();
    File file = new File("czarn_csg_koscielec.kml");

        //drawKMLPolylines(file);

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Toast.makeText(Map1Activity.this, "Kliknięto na trasę!", Toast.LENGTH_SHORT).show();
                Log.d("PolylineClick", "Polyline kliknięty: " + polyline.getId());
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("wertyu", String.valueOf(latLng));
                Toast.makeText(Map1Activity.this, "Kliknięcie na mapę: " + latLng.toString(), Toast.LENGTH_SHORT).show();
                //handleMapClick(latLng);
            }
        });

    }

    private void drawKMLPolylines(File kmlFile) {
        try {
            // Otwórz plik KML
            FileInputStream fileInputStream = new FileInputStream(kmlFile);

            // Wczytaj warstwę KML
            KmlLayer kmlLayer = new KmlLayer(mMap, fileInputStream, this);

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
                                Toast.makeText(Map1Activity.this, "Kliknięto trasę", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e("KML", "Błąd podczas odczytu pliku: " + e.getMessage());
        }
    }

    private void handleMapClick(LatLng clickedLocation) {
        // Obsługa kliknięcia, np. znajdowanie najbliższej trasy
        KLMFiles klm = new KLMFiles(this,mMap);
        Polyline closestRoute = klm.findClosestRoute(clickedLocation);

        if (closestRoute != null) {
            styleDatasetsLayer();
            closestRoute.setColor(Color.MAGENTA);
            closestRoute.setZIndex(1.0f);
            Toast.makeText(this, "Najbliższa trasa została podświetlona!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Brak tras w pobliżu", Toast.LENGTH_SHORT).show();
        }
    }
    private void styleDatasetsLayer() {
        for (Polyline polyline : routePolylines) {
            polyline.setColor(Color.GRAY); // Resetuj kolor wszystkich tras
            polyline.setClickable(true);   // Ustaw kliknięcie na trasę
        }
    }




    // Funkcja do kodowania wartości lat/long w formacie odpowiednim dla URL
    private String encodeLatLng(LatLng point) {
        return point.latitude + "," + point.longitude;
    }


    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String data = "";
            try {
                data = downloadUrl(urls[0]);
            } catch (Exception e) {
                Log.e("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            clearPreviousRoute();

            new ParserTask().execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws Exception {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            routePoints.clear();
            ArrayList<LatLng> points = new ArrayList<>();
            HashSet<LatLng> uniquePoints = new HashSet<>(); // Zestaw do śledzenia unikalnych punktów
            PolylineOptions lineOptions = new PolylineOptions();
            routePolylines.clear(); // Upewnij się, że lista jest czyszczona przed dodaniem nowych polilinii

            for (int i = 0; i < result.size(); i++) {
                List<HashMap<String, String>> path = result.get(i);
                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    Log.d("punkty", String.valueOf(position));

                    // Sprawdź, czy punkt jest unikalny
                    if (uniquePoints.add(position)) { // Jeśli dodanie do zestawu się powiedzie, to punkt jest unikalny
                        points.add(position);
                    }
                }
            }


            lineOptions.addAll(points);
            lineOptions.width(15);
            lineOptions.color(Color.RED);

            if (mMap != null) {
                // Usuń poprzednią trasę
                clearPreviousRoute();

                // Dodaj nową trasę
                currentRoutePolyline = mMap.addPolyline(lineOptions);
                routePoints.clear();
                routePoints.addAll(points);

                // Ustaw kamerę na ostatni punkt, jeśli punkty są dostępne
                if (!routePoints.isEmpty()) {
                    LatLng endPoint = routePoints.get(routePoints.size() - 1); // Ostatni punkt
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endPoint, 15));
                }
            }
        }

    }


    private void updateLocationOnMap(LatLng userLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable); // Zatrzymaj licznik, gdy aplikacja zostanie zamknięta
    }

    private void addMarkerAtCurrentPosition(boolean sign) {
        // Wyświetlenie widoku markera w odpowiedniej pozycji na mapie
        // Przeliczanie współrzędnych GPS na pozycję na ekranie
        if (sign) {
            markerView.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.VISIBLE);
            RouteButton.setVisibility(View.GONE);
        } else {
            markerView.setVisibility(View.GONE);
            topBar.setVisibility(View.GONE);
        }
        markerView.setTranslationX(0); // Przesunięcie w osi X
        markerView.setTranslationY(0);
    }



    private void drawPolylinesFromKml(KmlLayer kmlLayer) {
        for (KmlContainer container : kmlLayer.getContainers()) {
            for (KmlPlacemark placemark : container.getPlacemarks()) {
                if (placemark.getGeometry() instanceof KmlLineString) {
                    String name = placemark.getProperty("name");
                    int color = Color.GRAY;// = getColorBasedOnName(name); // Pobierz kolor na podstawie nazwy
                    if (name.contains("czerwony")) {
                        color =  Color.RED;
                    } else if (name.contains("niebieski")) {
                        color =  Color.BLUE;
                    } else if (name.contains("zolty")) {
                        color =  Color.YELLOW;
                    } else if (name.contains("zielony")) {
                        color =  Color.GREEN;
                    } else if (name.contains("czarny")) {
                        color =  Color.BLACK;
                    }
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(color)
                            .width(5f)
                            .clickable(true);

                    List<LatLng> points = ((KmlLineString) placemark.getGeometry()).getGeometryObject();
                    Polyline polyline = mMap.addPolyline(polylineOptions.addAll(points));
                    polyline.setTag("name");
                }
            }
        }
    }

    private void clearPreviousRoute() {
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
            timerHandler.removeCallbacks(timerRunnable); // Zatrzymaj timer
            currentRoutePolyline = null;
        }
        routePoints.clear();
    }


    private void showCurrentSegment(LatLng userLocation) {
        if (routePoints.isEmpty()) {
            return;
        }

        LatLng closestPoint = findClosestPoint(userLocation);
        int startIndex = routePoints.indexOf(closestPoint);
        if (startIndex == -1 || startIndex + 1 >= routePoints.size()) {
            return;
        }

        LatLng endPoint = routePoints.get(startIndex + 1);

        // Sprawdzanie i zmiana koloru poprzednich odcinków na szary
        if (lastTraversedIndex >= 0) {
            for (int i = 0; i <= lastTraversedIndex; i++) {
                Polyline polyline = routePolylines.get(i);
                polyline.setColor(Color.GRAY);
            }
        }

        // Dodajemy nowy odcinek trasy jako niebieski
        PolylineOptions options = new PolylineOptions()
                .add(closestPoint)
                .add(endPoint)
                .width(10)
                .color(Color.RED);

        Polyline newPolyline = mMap.addPolyline(options);
        routePolylines.add(newPolyline);

        // Aktualizowanie indeksu ostatniego pokonanego odcinka
        lastTraversedIndex = startIndex;
    }

    private LatLng findClosestPoint(LatLng userLocation) {
        LatLng closestPoint = null;
        float shortestDistance = Float.MAX_VALUE;

        for (LatLng point : routePoints) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.latitude, userLocation.longitude, point.latitude, point.longitude, results);
            float distance = results[0];

            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestPoint = point;
            }
        }

        return closestPoint;
    }

    private void onMapClick(LatLng clickedLocation) {
        Log.d("sdfghj", "likc");
        KLMFiles klm = new KLMFiles(this,mMap);
        Polyline closestRoute = klm.findClosestRoute(clickedLocation);

        if (closestRoute != null) {
            // Zmień kolor najbliższej trasy na wyróżniający
            closestRoute.setColor(Color.MAGENTA);
            Toast.makeText(this, "Najbliższa trasa została podświetlona!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Brak tras w pobliżu", Toast.LENGTH_SHORT).show();
        }
    }

}