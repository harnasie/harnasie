package com.example.baza;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;






import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
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
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> trasa = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();

    private List<Marker> schroniska = new ArrayList<>();
    private LocationCallback mLocationCallback;
    private LatLng lastLocation = null;
    private LatLng firstLocation = null;
    private int lastTraversedIndex = -1;
    private TextView percentCompleteTextView;
    private TextView distanceTravelledTextView;
    private float totalRouteDistance = 0; // Całkowita długość trasy
    private float distanceTravelled = 0;  // Dystans pokonany przez użytkownika
    private Marker marker;
    private int ile = 1;
    private ImageView markerView;

    private long navigationStartTime = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private TextView timerTextView;

    private List<LatLng> traversedPoints = new ArrayList<>();
    private List<Integer> traversedSegments = new ArrayList<>();

    private PolylineOptions lineOptions; // = new PolylineOptions();
    private static final double DISTANCE_THRESHOLD_METERS = 2.0; // 1 meter
    private FusedLocationProviderClient mFusedLocationClient;
    private TextToSpeech tts;
    private Handler handler;
    private boolean widoczne = false;
    private ArrayList<LatLng> points = new ArrayList<>();
    private LatLng destination = new LatLng(51.27447, 22.55371);// Zakopane
    private LatLng destination1 = new LatLng(49.2598, 19.9667);//20.09156,49.23821
    private List<Polyline> clickedPolylines = new ArrayList<>();
    private List<LatLng> routePoints = new ArrayList<>();
    private List<Polyline> routePolylines = new ArrayList<>();
    private Polyline currentRoutePolyline = null;
    private boolean isNavigationStarted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private LatLng markerCoordinates = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        percentCompleteTextView = findViewById(R.id.percent_complete);
        distanceTravelledTextView = findViewById(R.id.distance_travelled);
        timerTextView = findViewById(R.id.timer_text_view);
        markerView = findViewById(R.id.marker_view);


        Button SchroniskaButton = findViewById(R.id.btnSchroniska);
        SchroniskaButton.setOnClickListener(v -> {
            ile++;
            Log.d("ile", String.valueOf(ile));
            widoczne = ile % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : schroniska) {
                marker.setVisible(widoczne);
            }
        });
        // Inicjalizacja przycisku startu nawigacji
        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setOnClickListener(v -> {
            if (!isNavigationStarted) {
                startNavigation();
            } else {
                Toast.makeText(this, "Nawigacja już rozpoczęta", Toast.LENGTH_SHORT).show();
            }
        });

        Button RouteButton = findViewById(R.id.btnRoute);
        LinearLayout routeInputLayout = findViewById(R.id.route_input_layout);
        RouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (routeInputLayout.getVisibility() == View.GONE) {
                    routeInputLayout.setVisibility(View.VISIBLE);// Pokazuje layout
                } else {
                    routeInputLayout.setVisibility(View.GONE); // Ukrywa layout
                }
            }
        });

        Button MarkerButton = findViewById(R.id.btnMarker);
        MarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkerAtCurrentPosition();
            }
        });
        Button btnSetMarker = findViewById(R.id.btnSetMarker);
        btnSetMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmMarkerPosition();
            }
        });


        Button route = findViewById(R.id.btnNavigate);
        route.setOnClickListener(v -> {
            markerView.setVisibility(View.GONE);
            // Pobierz lokalizację i rozpocznij żądanie trasy
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Sprawdzamy, czy pierwsza lokalizacja jest ustawiona
                        if (firstLocation == null) {
                            firstLocation = currentLocation;
                        } else {
                            // Porównujemy wartości współrzędnych, aby upewnić się, że lokalizacja się zmieniła
                            if (firstLocation.latitude != currentLocation.latitude || firstLocation.longitude != currentLocation.longitude) {
                                // Usuń poprzednią trasę
                                clearPreviousRoute();

                                // Zaktualizuj pierwszą lokalizację na nową
                                firstLocation = currentLocation;
                            }
                        }
                        String url = "";
                        // Budujemy URL dla nowej trasy
                        if(trasa.size() == 1){
                            url = getDirectionsUrl(currentLocation, trasa.get(0));
                        }
                        else if(trasa.size() == 2){
                            url = getDirectionsUrl(trasa.get(0), trasa.get(1));
                        }
                        else{
                            List<LatLng> waypointsList = new ArrayList<>();
                            Log.d("tras1", String.valueOf(trasa.get(0)));
                            Log.d("trasaooo", String.valueOf(trasa.get(trasa.size()-1)));
                            for(int i=1;i<trasa.size()-1;i++ )
                            {
                                waypointsList.add(trasa.get(i));
                                Log.d("tras", String.valueOf(trasa.get(i)));
                            }
                            url = getDirectionsUrlWITH(trasa.get(0), trasa.get(trasa.size()-1),waypointsList);
                        }
                        Log.d("linkurl", url);
                        new FetchDirectionsTask().execute(url);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - navigationStartTime;
                int seconds = (int) (elapsedMillis / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;
                minutes = minutes % 60;

                // Aktualizacja TextView
                timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));

                // Uruchomenie co sekundę
                timerHandler.postDelayed(this, 1000);
            }
        };

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
                        if (lastLocation != null) {
                            distanceTravelled += calculateDistance(lastLocation, userLocation); // Oblicz dystans pokonany między poprzednią a nową lokalizacją
                            Log.e("dystans", String.valueOf(distanceTravelled));

                            updateDistanceText();
                        }
                        lastLocation = userLocation;
                        updateLocationOnMap(userLocation);
                        showCurrentSegment(userLocation);
                        updatePercentComplete(userLocation);
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
        LatLng initialLocation = new LatLng(49.2992, 19.9496);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //test();
        //loadKmlLayer();
        //setUpMapClickListener22();
        loadKmlLayers();
        //loadKmlLayersclick();
        //addExampleRoute();

        // Skonfiguruj nasłuchiwacz kliknięć
        //setUpMapClickListener();
        //loadKmlLayer();
        //test();
        /*try {
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.czerwony_dolina_panszczyca, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }*/
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 85, 85, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        LatLng schonisko_murowaniec = new LatLng(49.2433878,20.0071087);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_murowaniec).title("Schronisko Murowaniec")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_kondratowa = new LatLng(49.2498074,19.9517708);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_kondratowa).title("Schronisko PTTK Hala Kondratowa")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_kalatowki = new LatLng(49.259801,19.966706);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_kalatowki).title("Hotel Górski PTTK Kalatówki")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_ornak = new LatLng(49.2291175,19.8587185);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_ornak).title("Schronisko PTTK na Hali Ornak")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_stawy = new LatLng(49.2133541,20.0491499);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_stawy).title("Schronisko PTTK w Dolinie Pięciu Stawów Polskich")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_oko = new LatLng(49.2013333,20.0712722);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_oko).title("Schronisko PTTK Morskie Oko")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_roztoce = new LatLng(49.2337222,20.095675);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_roztoce).title("Schronisko im. W. Pola w Roztoce")
                .snippet("Population: 776733")
                .icon(icon).visible(widoczne)));
        LatLng schonisko_chocholowska = new LatLng(49.2360599,19.7874255);
        schroniska.add(googleMap.addMarker(new MarkerOptions().position(schonisko_chocholowska).title("Schronisko PTTK na Polanie Chochołowskiej")
                .icon(icon).visible(widoczne)));
    }

    private void addMarkerAtCenter() {
        // Pobranie obecnego widoku mapy i dodanie markera
        LatLng center = mMap.getCameraPosition().target;

        // Sprawdzenie, czy marker został już dodany
        if (markerCoordinates == null) {
            markerCoordinates = center; // Zapisanie współrzędnych markera

            // Dodanie markera
            mMap.addMarker(new MarkerOptions().position(center).title("Twój Marker"));

            // Wyświetlenie komunikatu o zapisaniu współrzędnych
            Toast.makeText(this, "Marker dodany na: " + center, Toast.LENGTH_SHORT).show();

            // Logowanie współrzędnych markera
            Log.d("MainActivity", "Współrzędne markera: " + markerCoordinates);
        } else {
            Toast.makeText(this, "Marker już dodany!", Toast.LENGTH_SHORT).show();
        }
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=walking";
        String key = "key=AIzaSyC4KaLnKSYLuF9xCyzudGh8DMCB-6HefJA";
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + key;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }


    private String getDirectionsUrlWITH(LatLng origin, LatLng dest, List<LatLng> waypointsList) {
        if (origin == null || dest == null) {
            throw new IllegalArgumentException("Origin and destination cannot be null");
        }

        // Parametry początkowy i końcowy
        String str_origin = "origin=" + encodeLatLng(origin);
        String str_dest = "destination=" + encodeLatLng(dest);
        String mode = "mode=walking";
        String key = "key=AIzaSyC4KaLnKSYLuF9xCyzudGh8DMCB-6HefJA"; // Zmienna do przechowywania klucza API

        // Tworzenie parametru waypoints, jeśli lista punktów pośrednich nie jest pusta
        String waypoints = "";
        if (waypointsList != null && !waypointsList.isEmpty()) {
            StringBuilder waypointsBuilder = new StringBuilder("waypoints=");
            for (int i = 0; i < waypointsList.size(); i++) {
                LatLng point = waypointsList.get(i);
                waypointsBuilder.append(encodeLatLng(point));
                if (i < waypointsList.size() - 1) {
                    waypointsBuilder.append("|");
                }
            }
            waypoints = waypointsBuilder.toString();
        }

        // Składanie wszystkich parametrów do jednego ciągu
        StringBuilder parametersBuilder = new StringBuilder(str_origin);
        parametersBuilder.append("&").append(str_dest);
        parametersBuilder.append("&").append(mode);
        parametersBuilder.append("&").append(key);

        if (!waypoints.isEmpty()) {
            parametersBuilder.append("&").append(waypoints);
        }

        // Zwrócenie pełnego URL-a
        return "https://maps.googleapis.com/maps/api/directions/json?" + parametersBuilder.toString();
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
            PolylineOptions lineOptions = new PolylineOptions();
            routePolylines.clear(); // Upewnij się, że lista jest czyszczona przed dodaniem nowych polilinii

            for (int i = 0; i < result.size(); i++) {
                List<HashMap<String, String>> path = result.get(i);
                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
            }

            totalRouteDistance = 0;
            for (int i = 0; i < points.size() - 1; i++) {
                totalRouteDistance += calculateDistance(points.get(i), points.get(i + 1));
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

                // Dodaj nowe polilinii do mapy i do listy routePolylines
                /*for (int i = 0; i < points.size() - 1; i++) {
                    LatLng startPoint = points.get(i);
                    LatLng endPoint = points.get(i + 1);

                    PolylineOptions segmentOptions = new PolylineOptions()
                            .add(startPoint, endPoint)
                            .width(15)
                            .color(Color.RED);

                    Polyline polyline = mMap.addPolyline(segmentOptions);
                    routePolylines.add(polyline);
                }

                routePoints.addAll(points);*/
                if (!routePoints.isEmpty()) {
                    LatLng startPoint = routePoints.get(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));
                }
            }


        }
    }

    private void startNavigation() {
        if (routePoints.isEmpty()) {
            Toast.makeText(this, "Nie znaleziono trasy, spróbuj ponownie", Toast.LENGTH_SHORT).show();
            return;
        }

        //stopNavigation();

        // Rozpoczęcie śledzenia czasu
        navigationStartTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);

        startLocationUpdates();
        Toast.makeText(this, "Rozpoczęto nawigację", Toast.LENGTH_SHORT).show();
        isNavigationStarted = true;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.e("77777sdfghujiko", String.valueOf(6));

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (lastLocation == null || !userLocation.equals(lastLocation)) {
                        lastLocation = userLocation;
                        //updateLocationOnMap(userLocation);
                        updateTraversedPoints(userLocation);
                        updateTraversedSegments(userLocation);
                        Log.e("sdfghujiko", String.valueOf(6));
                        //showCurrentSegment(userLocation);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void updateLocationOnMap(LatLng userLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20));
    }

    private void stopNavigation() {
        //if (isNavigationStarted) {
        timerHandler.removeCallbacks(timerRunnable);
        //Toast.makeText(this, "Nawigacja zatrzymana", Toast.LENGTH_SHORT).show();
        //isNavigationStarted = false;
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable); // Zatrzymaj licznik, gdy aplikacja zostanie zamknięta
    }

    private void addMarkerAtCurrentPosition() {
        // Wyświetlenie widoku markera w odpowiedniej pozycji na mapie
        // Przeliczanie współrzędnych GPS na pozycję na ekranie
        markerView.setVisibility(View.VISIBLE);
        markerView.setTranslationX(0); // Przesunięcie w osi X
        markerView.setTranslationY(0);
    }

    private void confirmMarkerPosition() {
        // Pobranie bieżącej pozycji kamery
        LatLng currentPosition = mMap.getCameraPosition().target;
        trasa.add(currentPosition);
        // Ustawienie współrzędnych markera
        markerCoordinates = currentPosition;
        mMap.addMarker(new MarkerOptions().position(currentPosition).title("Twój Marker"));
        Toast.makeText(this, "Marker dodany na: " + markerCoordinates, Toast.LENGTH_SHORT).show();

    }

    private float getScreenXFromLatLng(LatLng latLng) {
        // Obliczanie pozycji X w pikselach
        return (float) (mMap.getProjection().toScreenLocation(latLng).x);
    }

    private float getScreenYFromLatLng(LatLng latLng) {
        // Obliczanie pozycji Y w pikselach
        return (float) (mMap.getProjection().toScreenLocation(latLng).y);
    }



    private void handlePolylineClick(Polyline polyline) {
        String name = (String) polyline.getTag();
        // Dodaj polilinię do listy klikniętych, jeśli nie jest już w niej
        if (!clickedPolylines.contains(polyline)) {
            clickedPolylines.add(polyline);
        }

        // Sprawdź, czy liczba klikniętych polilinii wynosi 3
        if (clickedPolylines.size() == 3) {
            combinePolylines();
            clickedPolylines.clear(); // Wyczyść listę po połączeniu
        }
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

    private void combinePolylines() {
        List<LatLng> combinedPoints = new ArrayList<>();

        // Zbieranie punktów z klikniętych polilinii
        for (Polyline polyline : clickedPolylines) {
            combinedPoints.addAll(polyline.getPoints());
        }

        // Rysowanie nowej polilinii
        PolylineOptions combinedPolylineOptions = new PolylineOptions()
                .color(Color.BLUE) // Ustaw kolor nowej polilinii
                .width(8f)
                .addAll(combinedPoints);

        mMap.addPolyline(combinedPolylineOptions);
    }
    private void loadKmlLayer() {
        try {
            // Wczytaj warstwę KML
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.czerwony_dolina_trzydniowianska, getApplicationContext());
            // Dodaj warstwę do mapy
            kmlLayer.addLayerToMap();
            drawPolylinesFromKml(kmlLayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpMapClickListener() {
        mMap.setOnMapClickListener(latLng -> {
            // Iteruj przez wszystkie polilinie
            for (Polyline polyline : polylines) {
                // Sprawdź, czy kliknięcie jest w pobliżu polilinii
                if (PolyUtil.isLocationOnPath(latLng, polyline.getPoints(), false, 50.0)) {
                    // Pobierz nazwę trasy
                    String routeName = (String) polyline.getTag();

                    // Wyświetl powiadomienie Toast z nazwą trasy
                    Toast.makeText(this, "Trasa: " + routeName, Toast.LENGTH_SHORT).show();
                    return; // Zatrzymaj dalsze przeszukiwanie
                }
            }
        });
    }


    /*private void test(){
        try {
            // Wczytaj jedną warstwę KML
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.czarny_gubalowka, getApplicationContext());
            kmlLayer.addLayerToMap();
            Log.d("KML", "Warstwa KML dodana.");

            // Sprawdź, czy kmlLayer zawiera jakieś elementy
            if (kmlLayer.getContainers().size() > 0) {
                // Pobierz pierwszy kontener (zakładając, że masz tylko jeden)
                KmlContainer container = kmlLayer.getContainers().get(0);

                // Pobierz pierwszy placemark z kontenera
                KmlPlacemark placemark = container.getPlacemarks().get(0);

                // Pobierz nazwę trasy
                String nazwaTrasy = placemark.getProperty("name");
                Log.d("KML", "Nazwa trasy: " + nazwaTrasy);

                // Sprawdź, czy geometria to linia
                if (placemark.getGeometry() instanceof KmlLineString) {
                    KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                    List<LatLng> coordinates = lineString.getGeometryObject();
                    Log.d("KML", "Współrzędne: " + coordinates.toString());

                    // Utwórz Polyline i dodaj ją do mapy
                    Polyline polyline = mMap.addPolyline(new PolylineOptions()
                            .addAll(coordinates)
                            .width(12)
                            .color(Color.GRAY)
                            .clickable(true));
                    Log.d("KML", "Dodano Polyline dla: " + nazwaTrasy);

                    // Ustawienie tagu dla Polyline
                    polyline.setTag(nazwaTrasy);
                }
            }
        } catch (Exception e) {
            Log.e("KML", "Błąd przy dodawaniu KML: ", e);
        }
    }*/
    private Polyline examplePolyline;

    private void addExampleRoute() {
        // Lista punktów tworzących trasę
        List<LatLng> routePoints = Arrays.asList(
                new LatLng(49.2992, 19.9486), // Centrum Zakopanego
                new LatLng(49.2791, 19.9815), // Dolina Kościeliska
                new LatLng(49.2591, 19.9831), // Hala Ornak
                new LatLng(49.2432, 20.0060)  // Dolina Chochołowska
        );

        // Dodaj linię na mapie
        examplePolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(10)
                .color(Color.BLUE));

        // Ustaw nazwę trasy (można dodać inne właściwości, jeśli potrzebne)
        examplePolyline.setTag("Przykładowa Trasa");
    }
    private void setUpMapClickListener1() {
        mMap.setOnMapClickListener(latLng -> {
            // Sprawdź, czy kliknięcie jest w pobliżu trasy (w odległości 50 metrów)
            if (examplePolyline != null && PolyUtil.isLocationOnPath(latLng, examplePolyline.getPoints(), false, 50.0)) {
                // Pobierz nazwę trasy
                String routeName = (String) examplePolyline.getTag();

                // Wyświetl powiadomienie Toast z nazwą trasy
                Toast.makeText(this, "Trasa: " + routeName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpMapClickListener22() {
        mMap.setOnMapClickListener(latLng -> {
            Log.d("MapClick", "Clicked at: " + latLng);

            for (Polyline polyline : polylines) {
                if (PolyUtil.isLocationOnPath(latLng, polyline.getPoints(), false, 50.0)) {
                    String routeName = (String) polyline.getTag();
                    Toast.makeText(this, "Trasa: " + routeName, Toast.LENGTH_SHORT).show();
                    return; // Zatrzymaj dalsze przeszukiwanie
                }
            }
        });
    }


    private void loadKmlLayers() {
        int[] kmlResources = {
                R.raw.czerwony_nedzowka, R.raw.czerwony_dolina_trzydniowianska, R.raw.czerwony_dolina_panszczyca,
                R.raw.czerwony_giewont, R.raw.czerwony_dolina_za_bramka, R.raw.czerwony_po_granicy, R.raw.czerwony_nedzowka_przyslo_mietusi,
                R.raw.czerwony_wolowiec_gaborowa_przelecz,R.raw.czerwony_morskie_oko, R.raw.czerwony_morskie_oko_rysy,
                R.raw.zielony_czarny_staw_przelecz_pod_chlopkiem, R.raw.zielon_roztoka_wielki_staw,
                R.raw.zielony_dolinka_kozia, R.raw.zielony_hala_gasienicowa_goly_wierch, R.raw.zielony_dolina_koscieliska_chuda_przelaczka,
                R.raw.zolty_dolina_bialego, R.raw.zolty_dolina_chocholowsk_hala_ornak, R.raw.zolty_dolina_lejowa,
                R.raw.zolty_grzes_dolina_chocholowska, R.raw.zolty_przelecz_miedz_kopami, R.raw.zolty_klasztor_albertynow,
                R.raw.zolty_kasprowy_murowaniec, R.raw.zolty_murowaniec_wielki_staw,
                R.raw.czarny_gubalowka, R.raw.czarny_zakopane_witow, R.raw.czarny_zakopane_kiry,
                R.raw.czarn_csg_koscielec, R.raw.czarny_kozie_czuby, R.raw.czarny_schronisko_murowaniec,
                R.raw.czarny_zleb_kulczynskiego, R.raw.czarny_murowaniec_swinicka_przelecz, R.raw.czarny_dolina_chocholowskiej,
                R.raw.czarny_jaskinia_mrozna, R.raw.czarny_niedaleko_murowanca, R.raw.czarny_polana_pod_woloszynem,
                R.raw.czarny_siwa_przelecz, R.raw.czarny_przedni_staw, R.raw.czarny_sarnia_skala, R.raw.czerwony_wrota_chalubinskiego,
                R.raw.zielony_kasprowy, R.raw.zielony_konczysty_wierch, R.raw.zielony_iwaniacka_przelecz_gaborowa_przelecz,
                R.raw.zielony_dolina_chocholowska, R.raw.zielony_hala_kondratowa_kopa_kondracka, R.raw.zielony_nosal, R.raw.zielony_przelecz_liliowe,
                R.raw.niebieski_bobrowiecka_przelecz_do_wolowiec, R.raw.niebieski_kalatowka, R.raw.niebieski_dolina_malej_laki_malolaczniak, R.raw.niebieski_karb,
                R.raw.niebieski_zawrat_kondracka_przelecz, R.raw.niebieski_palenica, R.raw.niebieski_stoly_do_dolina_koscieliska, R.raw.niebieski_zawrat_moskie_oko
        };

        // Iteruj przez wszystkie trasy i dodawaj je do mapy
        for (int i = 0; i < kmlResources.length; i++) {
            try {
                // Wczytaj warstwę KML
                KmlLayer kmlLayer = new KmlLayer(mMap, kmlResources[i], getApplicationContext());
                kmlLayer.addLayerToMap();
                kmlLayer.setOnFeatureClickListener(feature -> {
                    Geometry geometry = feature.getGeometry();

                    KmlLineString lineString = (KmlLineString) geometry;
                    List<LatLng> points = lineString.getGeometryObject();

                    // Wyświetl nazwę trasy i dodaj marker w pobliżu pierwszego punktu linii
                    String routeName = feature.getProperty("name");

                    // Ustaw marker w pierwszym punkcie trasy dla przykładu
                    LatLng firstPoint = points.get(0);
                    mMap.addMarker(new MarkerOptions().position(firstPoint).title(routeName));});
                for (KmlContainer container : kmlLayer.getContainers()) {
                    for (KmlPlacemark placemark : container.getPlacemarks()) {
                        // Pobierz współrzędne linii
                        Log.d("KML", "Nazwa: " + placemark.getProperty("name")); // Dodany log

                        if (placemark.getGeometry() instanceof KmlLineString) {
                            KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                            List<LatLng> coordinates = lineString.getGeometryObject();
                            Log.d("KML", coordinates.toString()); // Dodany log

                            // Utwórz Polyline i dodaj ją do mapy


                        }
                    }
                }
                drawPolylinesFromKml(kmlLayer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadKmlLayersclick() {
        int[] kmlResources = {
                R.raw.czerwony_nedzowka, R.raw.czerwony_dolina_trzydniowianska, R.raw.czerwony_dolina_panszczyca,
                R.raw.czerwony_giewont, R.raw.czerwony_dolina_za_bramka, R.raw.czerwony_po_granicy, R.raw.czerwony_nedzowka_przyslo_mietusi,
                R.raw.czerwony_wolowiec_gaborowa_przelecz, R.raw.czerwony_morskie_oko, R.raw.czerwony_morskie_oko_rysy,
                R.raw.zielony_czarny_staw_przelecz_pod_chlopkiem, R.raw.zielon_roztoka_wielki_staw,
                R.raw.zielony_dolinka_kozia, R.raw.zielony_hala_gasienicowa_goly_wierch, R.raw.zielony_dolina_koscieliska_chuda_przelaczka,
                R.raw.zolty_dolina_bialego, R.raw.zolty_dolina_chocholowsk_hala_ornak, R.raw.zolty_dolina_lejowa,
                R.raw.zolty_grzes_dolina_chocholowska, R.raw.zolty_przelecz_miedz_kopami, R.raw.zolty_klasztor_albertynow,
                R.raw.zolty_kasprowy_murowaniec, R.raw.zolty_murowaniec_wielki_staw,
                R.raw.czarny_gubalowka, R.raw.czarny_zakopane_witow, R.raw.czarny_zakopane_kiry,
                R.raw.czarn_csg_koscielec, R.raw.czarny_kozie_czuby, R.raw.czarny_schronisko_murowaniec,
                R.raw.czarny_zleb_kulczynskiego, R.raw.czarny_murowaniec_swinicka_przelecz, R.raw.czarny_dolina_chocholowskiej,
                R.raw.czarny_jaskinia_mrozna, R.raw.czarny_niedaleko_murowanca, R.raw.czarny_polana_pod_woloszynem,
                R.raw.czarny_siwa_przelecz, R.raw.czarny_przedni_staw, R.raw.czarny_sarnia_skala, R.raw.czerwony_wrota_chalubinskiego,
                R.raw.zielony_kasprowy, R.raw.zielony_konczysty_wierch, R.raw.zielony_iwaniacka_przelecz_gaborowa_przelecz,
                R.raw.zielony_dolina_chocholowska, R.raw.zielony_hala_kondratowa_kopa_kondracka, R.raw.zielony_nosal, R.raw.zielony_przelecz_liliowe,
                R.raw.niebieski_bobrowiecka_przelecz_do_wolowiec, R.raw.niebieski_kalatowka, R.raw.niebieski_dolina_malej_laki_malolaczniak, R.raw.niebieski_karb,
                R.raw.niebieski_zawrat_kondracka_przelecz, R.raw.niebieski_palenica, R.raw.niebieski_stoly_do_dolina_koscieliska, R.raw.niebieski_zawrat_moskie_oko
        };

        // Przechowuj wszystkie linie KML w liście
        List<KmlLayer> kmlLayers = new ArrayList<>();

        // Iteruj przez wszystkie trasy i dodawaj je do mapy
        for (int kmlResource : kmlResources) {
            try {
                // Wczytaj warstwę KML
                KmlLayer kmlLayer = new KmlLayer(mMap, kmlResource, getApplicationContext());
                kmlLayer.addLayerToMap();

                // Dodaj warstwę KML do listy
                kmlLayers.add(kmlLayer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Dodaj nasłuchiwacz kliknięć na mapie
        mMap.setOnMapClickListener(latLng -> {
            // Sprawdź, czy kliknięcie jest blisko którejkolwiek z tras
            Log.d("clikc", String.valueOf(56));
            for (KmlLayer kmlLayer : kmlLayers) {
                for (KmlPlacemark placemark : kmlLayer.getPlacemarks()) {
                    Geometry geometry = placemark.getGeometry();

                    if (geometry instanceof KmlLineString) {
                        List<LatLng> points = ((KmlLineString) geometry).getGeometryObject();

                        // Sprawdź, czy kliknięcie jest w pobliżu trasy (w odległości 50 metrów)
                        if (PolyUtil.isLocationOnPath(latLng, points, false, 50.0)) {
                            // Pobierz nazwę trasy
                            String routeName = placemark.getProperty("name");

                            // Wyświetl powiadomienie Toast z nazwą trasy
                            Toast.makeText(this, "Trasa: " + routeName, Toast.LENGTH_SHORT).show();
                            return; // Zatrzymaj pętlę po znalezieniu pasującej trasy
                        }
                    }
                }
            }
        });
    }



    // Funkcja do aktualizacji kolorów odcinków na podstawie pozycji użytkownika
    /*private void updateTraversedSegments() {
        if (routePoints.isEmpty() || routePolylines.isEmpty()) {
            return;
        }

        for (int i = 0; i < routePoints.size() - 1; i++) {
            LatLng startPoint = routePoints.get(i);
            LatLng endPoint = routePoints.get(i + 1);

            boolean isTraversed = false;

            for (LatLng traversedPoint : traversedPoints) {
                if (isPointOnSegment(traversedPoint, startPoint, endPoint)) {
                    isTraversed = true;
                    break;
                }
            }

            Polyline polyline = routePolylines.get(i);

            if (isTraversed) {
                if (polyline.getColor() != Color.GRAY) {
                    polyline.setColor(Color.GRAY);
                }
            } else {
                if (polyline.getColor() != Color.GRAY) {
                    polyline.setColor(Color.RED);
                }
            }
        }
    }*/

    // Funkcja do obliczenia odległości punktu (użytkownik) od odcinka trasy
    private double getDistanceFromPointToLineSegment(LatLng point, LatLng start, LatLng end) {
        // Sprawdzenie odległości między punktami za pomocą Google Maps API
        double distanceStartToPoint = SphericalUtil.computeDistanceBetween(start, point);
        double distanceEndToPoint = SphericalUtil.computeDistanceBetween(end, point);

        double distanceStartToEnd = SphericalUtil.computeDistanceBetween(start, end);

        // Obliczamy projekcję punktu na odcinek (lub dystans od najbliższego końca, jeśli poza odcinkiem)
        double area = Math.abs(
                (start.latitude * (end.longitude - point.longitude) +
                        end.latitude * (point.longitude - start.longitude) +
                        point.latitude * (start.longitude - end.longitude)) / 2.0);

        double segmentLength = SphericalUtil.computeDistanceBetween(start, end);

        return (2 * area) / segmentLength; // Odległość punktu od linii w metrach
    }


    private boolean isPointNearLineSegment(LatLng point, LatLng start, LatLng end) {
        float[] results = new float[1];

        // Odległość między punktem a początkiem odcinka
        Location.distanceBetween(point.latitude, point.longitude, start.latitude, start.longitude, results);
        float startToPoint = results[0];

        // Odległość między punktem a końcem odcinka
        Location.distanceBetween(point.latitude, point.longitude, end.latitude, end.longitude, results);
        float endToPoint = results[0];

        // Odległość między początkiem a końcem odcinka
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        float segmentLength = results[0];

        // Odległość do linii odcinka
        float lineDistance = (float) (Math.abs((end.longitude - start.longitude) * (start.latitude - point.latitude) - (end.latitude - start.latitude) * (start.longitude - point.longitude)) / (float) Math.sqrt(Math.pow(end.longitude - start.longitude, 2) + Math.pow(end.latitude - start.latitude, 2)));

        // Sprawdzamy, czy punkt znajduje się blisko odcinka
        return lineDistance < 15; // Możesz dostosować ten próg w zależności od wymagań
    }

    private void updateTraversedPoints(LatLng userLocation) {
        if (routePoints.isEmpty()) {
            return;
        }

        LatLng previousPoint = null;

        for (LatLng currentPoint : routePoints) {
            if (previousPoint != null) {
                double distance = SphericalUtil.computeDistanceBetween(userLocation, previousPoint);
                if (distance < DISTANCE_THRESHOLD_METERS) {
                    // Add points that are near the user's location to the traversed list
                    if (!traversedPoints.contains(previousPoint)) {
                        traversedPoints.add(previousPoint);
                    }
                }
            }
            previousPoint = currentPoint;
        }
    }

    private boolean isPointOnSegment(LatLng point, LatLng start, LatLng end) {
        double distanceToStart = SphericalUtil.computeDistanceBetween(point, start);
        double distanceToEnd = SphericalUtil.computeDistanceBetween(point, end);
        double segmentLength = SphericalUtil.computeDistanceBetween(start, end);

        return distanceToStart + distanceToEnd >= segmentLength - DISTANCE_THRESHOLD_METERS &&
                distanceToStart + distanceToEnd <= segmentLength + DISTANCE_THRESHOLD_METERS;
    }

    private void clearPreviousRoute() {
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
            timerHandler.removeCallbacks(timerRunnable); // Zatrzymaj timer
            timerTextView.setText("00:00:00");
            currentRoutePolyline = null;
        }
        routePoints.clear();
    }

    private void updateTraversedSegments(LatLng userLocation) {
        if (routePoints.isEmpty() || routePolylines.isEmpty()) {
            return;
        }

        // Clear previous traversed segments
        traversedSegments.clear();

        // Check if the route is completed
        LatLng startPointOfRoute = routePoints.get(0);
        LatLng endPointOfRoute = routePoints.get(routePoints.size() - 1);

        double distanceToStart = SphericalUtil.computeDistanceBetween(userLocation, startPointOfRoute);
        double routeLength = SphericalUtil.computeDistanceBetween(startPointOfRoute, endPointOfRoute);

        if (distanceToStart > routeLength) {
            traversedPoints.add(startPointOfRoute); // Ensure the entire route is marked
        }

        boolean inTraversedSegment = false;

        for (int i = 0; i < routePoints.size() - 1; i++) {
            LatLng startPoint = routePoints.get(i);
            LatLng endPoint = routePoints.get(i + 1);

            // Determine if the segment should be marked as traversed
            boolean isTraversed = false;
            boolean isBetweenStartAndUser = false;

            for (LatLng traversedPoint : traversedPoints) {
                if (isPointOnSegment(traversedPoint, startPoint, endPoint)) {
                    isTraversed = true;
                    traversedSegments.add(i);
                    break;
                }
            }

            // Check if user is on or beyond the start of this segment
            if (SphericalUtil.computeDistanceBetween(userLocation, startPoint) <= DISTANCE_THRESHOLD_METERS) {
                inTraversedSegment = true;
            }

            // Get the polyline for the segment
            Polyline polyline = routePolylines.get(i);

            if (isTraversed || inTraversedSegment) {
                polyline.setColor(Color.BLUE);
            } else {
                polyline.setColor(Color.RED);
            }
        }
    }

    private void updatePercentComplete(LatLng userLocation) {
        if (routePoints.isEmpty() || totalRouteDistance == 0) {
            return;
        }

        LatLng closestPoint = findClosestPoint(userLocation);
        float distanceToClosestPoint = 0;

        // Obliczenie dystansu od początku trasy do najbliższego punktu
        for (int i = 0; i < routePoints.indexOf(closestPoint); i++) {
            distanceToClosestPoint += calculateDistance(routePoints.get(i), routePoints.get(i + 1));
        }

        // Obliczenie procentu trasy
        float percentComplete = (distanceToClosestPoint / totalRouteDistance) * 100;
        percentCompleteTextView.setText(String.format("Procent pokonanej trasy: %.2f%%", percentComplete));
    }
    private void updateDistanceText() {
        distanceTravelledTextView.setText(String.format("Pokonany dystans: %.2f m", distanceTravelled));
    }
    private float calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0]*100; // Dystans w metrach
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
}