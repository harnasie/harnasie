package com.example.baza;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;
import java.io.IOException;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationCallback mLocationCallback;
    private LatLng lastLocation = null;
    private int lastTraversedIndex = -1;
    private List<LatLng> waypointsList = new ArrayList<>();
    private Map<String, Marker> markers = new HashMap<>();
    private static final String TAG = "KMLDownloader";
    private FirebaseFirestore db;
    private LinearLayout topBar, spine, google_clear, szlak_route;
    private String selectedFile, nameselectedFile;
    private int ile = 1, ileszczyty = 1, ilestawy = 1;
    private Spinner spiner;
    private List<Marker> stawylist = new ArrayList<>();
    private List<Marker> szczytylist = new ArrayList<>();
    private List<Marker> schroniskalist = new ArrayList<>();
    private String url = "";
    private Button RouteButton, btnstop1, btnstop2, btnstop3, button_addstop, btnconfirm;
    private ImageView markerView;
    private int[] point = {0, 0, 0, 0, 0};
    private LatLng currentLocation = null, skad_location = null, dokad_location = null,
            stop1_location = null, stop2_location = null, stop3_location = null;
    private EditText skad_et, dokad_et, stop1, stop2, stop3;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextToSpeech tts;
    private EditText[] stops = new EditText[3];
    private LinearLayout routeInputLayout,menuLayout,mainLayout;
    private boolean widoczne = false;
    private List<LatLng> routePoints = new ArrayList<>();
    private List<Polyline> routePolylines = new ArrayList<>();
    private List<String> szlaki = new ArrayList<>();
    private List<String> szlakisp = new ArrayList<>();
    private Polyline currentRoutePolyline = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private Button spinerview, chosenSzlak, googlesz, clear, btnGoogle;
    private ImageButton btnchart, btndanger, btnTelefon, btnmap, btnuser;
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
        // Inicjalizacja Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.UK);
            }
        });

        markerView = findViewById(R.id.marker_view);
        btnGoogle =  findViewById(R.id.btn_Google);

        ImageButton SzczytyButton = findViewById(R.id.btnSzczyty);
        SzczytyButton.setOnClickListener(v -> {
            ileszczyty++;
            widoczne = ileszczyty % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : szczytylist) {
                marker.setVisible(widoczne);
            }
        });
        ImageButton StawyButton = findViewById(R.id.btnStawy);
        StawyButton.setOnClickListener(v -> {
            ilestawy++;
            widoczne = ilestawy % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : stawylist) {
                marker.setVisible(widoczne);
            }
        });

        ImageButton SchroniskaButton = findViewById(R.id.btnSchroniska);
        SchroniskaButton.setOnClickListener(v -> {
            ile++;
            widoczne = ile % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : schroniskalist) {
                marker.setVisible(widoczne);
            }
        });

        ImageButton LayerButton = findViewById(R.id.btnLayer);
        LayerButton.setOnClickListener(v -> {
            if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Zmiana na normalny widok
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Zmiana na satelitę
            }

        });

        RouteButton = findViewById(R.id.btnRoute);
        googlesz = findViewById(R.id.googleszlak);
        routeInputLayout = findViewById(R.id.route_input_layout);
        RouteButton.setOnClickListener(v -> {
            googlesz.setVisibility(View.GONE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        skad_et.setText(getCityAndStreetFromCoordinates(currentLocation));
                        skad_location=currentLocation;
                    }
                });
            }

            // To jest zewnętrzny click listener dla RouteButton
            if (routeInputLayout.getVisibility() == View.GONE) {
                routeInputLayout.setVisibility(View.VISIBLE); // Pokazuje layout
            } else {
                routeInputLayout.setVisibility(View.GONE); // Ukrywa layout
            }
            szlak_route.setVisibility(View.GONE);
            google_clear.setVisibility(View.GONE);
        });

        spinerview = findViewById(R.id.spinner);
        clear = findViewById(R.id.btnRouteclean);
        spinerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                google_clear.setVisibility(View.GONE);
                szlak_route.setVisibility(View.GONE);
                googlesz.setVisibility(View.GONE);
                spine = findViewById(R.id.spinnerl);
                chosenSzlak = findViewById(R.id.chooseszlak);
                chosenSzlak.setOnClickListener(vv -> {
                    spine.setVisibility(View.GONE);
                    szlak_route.setVisibility(View.VISIBLE);
                    KLMFiles kmlFiles = new KLMFiles(MapActivity.this, mMap);
                    LatLng start = kmlFiles.getRouteStartEnd(selectedFile).get(0);
                    LatLng end = kmlFiles.getRouteStartEnd(selectedFile).get(1);
                    LatLng cam = kmlFiles.getRouteStartEnd(selectedFile).get(2);
                    float bearing = calculateBearing(start, cam);

                    // Tworzenie pozycji kamery z odpowiednim bearing
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(start)  // Punkt początkowy
                            .zoom(20)     // Ustawiona wcześniej wartość zoomu
                            .bearing(bearing)  // Obrót kamery w stronę punktu ennn
                            .tilt(45)     // Opcjonalne: pochylanie kamery (wartość w stopniach)
                            .build();

                    // Przesunięcie kamery
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    googlesz.setText(nameselectedFile + " --> GOOGLE");
                    googlesz.setVisibility(View.VISIBLE);
                    googlesz.setOnClickListener(vvv -> {
                        url = getDirectionsUrlGoogle(start,end);
                        openGoogleMapsWithRoute(url);
                    });
                });

                spine.setVisibility(View.VISIBLE);
                spiner = findViewById(R.id.spinnerszlak);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapActivity.this, R.layout.spinner_with_square,szlakisp){
                // Niestandardowy adapter dla Spinnera z zielonym kwadratem
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        // Inflacja układu dla elementu Spinnera
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View view = inflater.inflate(R.layout.spinner_with_square, parent, false);

                        // Znalezienie i ustawienie tekstu w TextView
                        TextView textView = view.findViewById(R.id.spinner_item_text);
                        textView.setText(getItem(position));
                        View square = view.findViewById(R.id.square_view);
                        if (szlaki.get(position).toLowerCase().contains("czerwony")) {
                            square.setBackgroundColor(Color.RED); // Ustaw kolor czerwony
                        } else if (szlaki.get(position).toLowerCase().contains("niebieski")){
                            square.setBackgroundColor(Color.BLUE); // Domyślny kolor np. zielony
                        } else if (szlaki.get(position).toLowerCase().contains("czarny")){
                            square.setBackgroundColor(Color.BLACK); // Domyślny kolor np. zielony
                        } else if (szlaki.get(position).toLowerCase().contains("zolty")){
                            square.setBackgroundColor(Color.YELLOW); // Domyślny kolor np. zielony
                        }

                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        // Inflacja układu dla rozwiniętej listy Spinnera
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View view = inflater.inflate(R.layout.spinner_with_square, parent, false);

                        // Znalezienie i ustawienie tekstu w TextView
                        TextView textView = view.findViewById(R.id.spinner_item_text);
                        textView.setText(getItem(position));
                        View square = view.findViewById(R.id.square_view);
                        if (szlaki.get(position).toLowerCase().contains("czerwony")) {
                            square.setBackgroundColor(Color.RED); // Ustaw kolor czerwony
                        } else if (szlaki.get(position).toLowerCase().contains("niebieski")){
                            square.setBackgroundColor(Color.BLUE); // Domyślny kolor np. zielony
                        } else if (szlaki.get(position).toLowerCase().contains("czarny_")){
                            square.setBackgroundColor(Color.BLACK); // Domyślny kolor np. zielony
                        } else if (szlaki.get(position).toLowerCase().contains("żółty")){
                            square.setBackgroundColor(Color.YELLOW); // Domyślny kolor np. zielony
                        }

                        return view;
                    }
                };

                spiner.setAdapter(adapter);
                spiner.setVisibility(View.VISIBLE);
                spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Pobieranie wybranego elementu
                        nameselectedFile = szlakisp.get(position);
                        selectedFile = szlaki.get(position);
                        Toast.makeText(MapActivity.this, "Wybrano: " + selectedFile, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Obsługa braku wyboru
                    }
                });

            }
        });

        topBar = findViewById(R.id.topBar);
        Button topBarButton = findViewById(R.id.topBarButton);

        topBarButton.setOnClickListener(v -> {
            LatLng stopLocation = null;
            // Ukrywanie paska i markera po kliknięciu przycisku
            addMarkerAtCurrentPosition(false);
            routeInputLayout.setVisibility(View.VISIBLE);
            for(int i =0; i<point.length; i++){
                if(point[i] == 1){
                    switch (i){
                        case 0:
                        {skad_location = addMarkerAtCenter("start");
                            if(skad_location != null){
                                skad_et.setText(getCityAndStreetFromCoordinates(skad_location));
                            }}
                        point[i] = 0;
                        break;
                        case 1:
                        {dokad_location = addMarkerAtCenter("koniec");
                            if(dokad_location != null){
                                dokad_et.setText(getCityAndStreetFromCoordinates(dokad_location));
                            }}
                        point[i] = 0;
                        break;
                        case 2:
                        case 3:
                        case 4:
                        {stopLocation = addMarkerAtCenter("przystanek "+(i-1));
                            if(stopLocation != null){
                                stops[i-2].setText(getCityAndStreetFromCoordinates(stopLocation));
                                waypointsList.add(stopLocation);
                            }}
                        point[i] = 0;
                        break;
                    }
                }
            }
        });

        skad_et = findViewById(R.id.place1);
        dokad_et = findViewById(R.id.place2);
        btnuser = findViewById(R.id.userView);
        btnTelefon = findViewById(R.id.buttonTelefon);
        btndanger = findViewById(R.id.danger);
        btnmap = findViewById(R.id.btnmap);
        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        btnTelefon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, TelefonActivity.class);
                startActivity(intent);
            }
        });

        btndanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, DangerActivity.class);
                startActivity(intent);
            }
        });

        btnuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(url != ""){
                    url = getDirectionsUrlGoogle(skad_location,dokad_location);
                openGoogleMapsWithRoute(url);}

            }
        });

        btnconfirm = findViewById(R.id.btnConfirm);
        google_clear = findViewById(R.id.google_clear);
        szlak_route = findViewById(R.id.szlak_route);
        btnconfirm.setOnClickListener(v -> {
            szlak_route.setVisibility(View.GONE);
            markerView.setVisibility(View.GONE);
            google_clear.setVisibility(View.VISIBLE);
            clear.setOnClickListener(vv -> {
                clearPreviousRoute();
                String[] markerNames = {"start", "koniec", "przystanek 1", "przystanek 2","przystanek 3"};
                for (String name : markerNames) {
                    Marker existingMarker = markers.get(name);
                    if (existingMarker != null) {
                        existingMarker.remove();
                    }
                }
                google_clear.setVisibility(View.GONE);
                szlak_route.setVisibility(View.VISIBLE);

            });
            if(skad_location != null && dokad_location != null){
                if(waypointsList == null) {
                    url = getDirectionsUrl(skad_location, dokad_location);
                }
                else{
                    url = getDirectionsUrlWITH(skad_location, dokad_location, waypointsList);

                }
            }
            else {
                Toast.makeText(this, "Podaj wszystkie dane trasy", Toast.LENGTH_SHORT).show();            }
            Log.d("linkurl", url);
            new FetchDirectionsTask().execute(url);
            routeInputLayout.setVisibility(View.GONE);
        });


        stop1 = findViewById(R.id.placestop1);
        stop2 = findViewById(R.id.placestop2);
        stop3 = findViewById(R.id.placestop3);
        stops[0] = stop1;
        stops[1] = stop2;
        stops[2] = stop3;

        skad_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkerAtCurrentPosition(true);
                point[0] = 1;
                routeInputLayout.setVisibility(View.GONE);
            }
        });

        dokad_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarkerAtCurrentPosition(true);
                point[1] = 1;
                routeInputLayout.setVisibility(View.GONE);
            }
        });

        btnstop1 = findViewById(R.id.btnstop1);
        btnstop2 = findViewById(R.id.btnstop2);
        btnstop3 = findViewById(R.id.btnstop3);
        button_addstop = findViewById(R.id.button_stop);
        button_addstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button[] buttons = {btnstop1, btnstop2, btnstop3};

                for (int i = 0; i < stops.length; i++) {
                    final int index = i;
                    if (stops[i].getVisibility() == View.GONE) {
                        stops[i].setVisibility(View.VISIBLE);
                        stops[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addMarkerAtCurrentPosition(true);
                                point[index + 2] = 1;
                                routeInputLayout.setVisibility(View.GONE);
                            }
                        });
                        buttons[i].setVisibility(View.VISIBLE);
                        // Musisz stworzyć lokalną zmienną, aby nie stracić odniesienia w lambdach
                        buttons[i].setOnClickListener(v1 -> {
                            stops[index].setVisibility(View.GONE);
                            buttons[index].setVisibility(View.GONE);
                        });
                        break;
                    } else {

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
        LatLng initialLocation = new LatLng(49.2992, 19.9496);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        KLMFiles kmlfiles = new KLMFiles(this,mMap);
        kmlfiles.processKMLFiles();
        szlaki = kmlfiles.getSzlaki();
        for(String file : szlaki){
            String name = file.substring(0, file.lastIndexOf('.'));
            String[] parts = name.split("_");
            String part1 = parts[1].replace("&", " ");
            String part2 = parts[2].replace("&", " ");
            if(parts.length == 4){
                String part3 = parts[3].replace("&", " ");
                name =part1 + " - " + part2 + " (" + part3 + ")";//+ parts[2];
            }
            else{
                name = part1 +  " (" + part2 + ")";
            }
            szlakisp.add(name);
        }

        szczytylist = kmlfiles.getSzczyty();
        stawylist = kmlfiles.getStawy();
        schroniskalist = kmlfiles.getSchroniska();
        getAcceptedDangersLocations();

    }

    private void openGoogleMapsWithRoute(String url) {
        // Tworzymy intent z URL Google Maps
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);

    }

    private float calculateBearing(LatLng start, LatLng end) {
        double lat1 = Math.toRadians(start.latitude);
        double lon1 = Math.toRadians(start.longitude);
        double lat2 = Math.toRadians(end.latitude);
        double lon2 = Math.toRadians(end.longitude);

        double dLon = lon2 - lon1;
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        return (float) Math.toDegrees(Math.atan2(y, x));
    }


    private String encodeLatLng1(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }

    private LatLng addMarkerAtCenter(String name) {
        // Pobranie obecnego widoku mapy i dodanie markera
        LatLng center = mMap.getCameraPosition().target;

        Marker existingMarker = markers.get(name);
        if (existingMarker != null) {
            existingMarker.remove();
        }

        Marker newMarker = mMap.addMarker(new MarkerOptions().position(center).title(name));
        markers.put(name, newMarker);
        Toast.makeText(this, "Marker dodany na: " + center, Toast.LENGTH_SHORT).show();

        return center;
    }


    protected static String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=walking";
        String key = "key=AIzaSyC4KaLnKSYLuF9xCyzudGh8DMCB-6HefJA";
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + key;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private String getDirectionsUrlGoogle(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "travelmode=walking";
        String key = "key=AIzaSyC4KaLnKSYLuF9xCyzudGh8DMCB-6HefJA";
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        return "https://www.google.com/maps/dir/?api=1&" + parameters;
    }

    private String getDirectionsUrlWITH(LatLng origin, LatLng dest, List<LatLng> waypointsList) {
        if (origin == null || dest == null) {
            throw new IllegalArgumentException("Dane trasy są nieuzupełnione");
        }

        String str_origin = "origin=" + encodeLatLng(origin);
        String str_dest = "destination=" + encodeLatLng(dest);
        String mode = "mode=walking";
        String key = "key=AIzaSyC4KaLnKSYLuF9xCyzudGh8DMCB-6HefJA";

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

        StringBuilder parametersBuilder = new StringBuilder(str_origin);
        parametersBuilder.append("&").append(str_dest);
        parametersBuilder.append("&").append(mode);
        parametersBuilder.append("&").append(key);

        if (!waypoints.isEmpty()) {
            parametersBuilder.append("&").append(waypoints);
        }

        return "https://maps.googleapis.com/maps/api/directions/json?" + parametersBuilder.toString();
    }

    private String getCityAndStreetFromCoordinates(LatLng place) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        StringBuilder addressString = new StringBuilder();

        try {
            List<Address> addresses = geocoder.getFromLocation(place.latitude, place.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String street = address.getThoroughfare(); // Ulica
                String city = address.getLocality(); // Miasto

                // Sprawdzenie, czy miasto jest dostępne
                if (city != null) {
                    addressString.append(city);
                } else {
                    return "Nieznane miejsce"; // Jeśli miasto jest niedostępne
                }

                // Dodanie ulicy, jeśli dostępna
                if (street != null) {
                    addressString.append(", ").append(street);
                }
            } else {
                return "Brak adresu dla podanych współrzędnych"; // Jeśli brak adresu
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Błąd przy uzyskiwaniu adresu";
        }

        return addressString.toString(); // Zwraca adres jako string
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
            routePolylines.clear();
            ArrayList<LatLng> points = new ArrayList<>();
            HashSet<LatLng> uniquePoints = new HashSet<>();
            PolylineOptions lineOptions = new PolylineOptions();


            for (int i = 0; i < result.size(); i++) {
                List<HashMap<String, String>> path = result.get(i);
                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    if (uniquePoints.add(position)) {
                        points.add(position);
                    }
                }
            }

            lineOptions.addAll(points);
            lineOptions.width(15);
            lineOptions.color(Color.MAGENTA);

            if (mMap != null) {
                clearPreviousRoute();
                currentRoutePolyline = mMap.addPolyline(lineOptions);
                routePoints.clear();
                routePoints.addAll(points);

                if (!routePoints.isEmpty()) {
                    LatLng startPoint = routePoints.get(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));
                }
            }
        }
    }


    private void updateLocationOnMap(LatLng userLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20));
    }

    private void addMarkerAtCurrentPosition(boolean sign) {
        // Wyświetlenie widoku markera w odpowiedniej pozycji na mapie
        // Przeliczanie współrzędnych GPS na pozycję na ekranie
        if (sign) {
            markerView.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.VISIBLE);
            szlak_route.setVisibility(View.GONE);
        } else {
            markerView.setVisibility(View.GONE);
            topBar.setVisibility(View.GONE);
            szlak_route.setVisibility(View.VISIBLE);

        }
        markerView.setTranslationX(0); // Przesunięcie w osi X
        markerView.setTranslationY(0);
    }

    private void clearPreviousRoute() {
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
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

    private void updateMapWithAcceptedDangers(List<LatLng> locations) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.warning_sign);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 85, 85, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        for (LatLng location : locations) {
            mMap.addMarker(new MarkerOptions().position(location).icon(icon).title("Zagrożenie"));
        }
    }

    private void getAcceptedDangersLocations() {
        db.collection("dangers")
                .whereEqualTo("accepted", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<LatLng> locations = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.contains("location")) {
                                Map<String, Object> locationmap = (Map<String, Object>) documentSnapshot.get("location");

                                if (locationmap != null) {
                                    double latitude = (double) locationmap.get("latitude");
                                    double longitude = (double) locationmap.get("longitude");

                                    LatLng location = new LatLng(latitude, longitude);
                                    locations.add(location);

                                    Log.d("Accepted Danger", "Location: " + latitude + ", " + longitude);
                                }}}
                        if (!locations.isEmpty()) {
                            updateMapWithAcceptedDangers(locations);
                        } else {Toast.makeText(MapActivity.this, "Brak zaakceptowanych zgłoszeń.", Toast.LENGTH_SHORT).show();}
                    } else {Toast.makeText(MapActivity.this, "Brak zgłoszeń w bazie.", Toast.LENGTH_SHORT).show();}
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapActivity.this, "Błąd przy pobieraniu zgłoszeń: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Błąd przy pobieraniu zaakceptowanych zgłoszeń", e);
                });
    }

}