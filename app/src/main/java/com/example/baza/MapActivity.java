package com.example.baza;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

        /*Button SchroniskaButton = findViewById(R.id.btnSchroniska);
        SchroniskaButton.setOnClickListener(v -> {
            ile++;
            Log.d("ile", String.valueOf(ile));
            widoczne = ile % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : schroniska) {
                marker.setVisible(widoczne);
            }
        });

        Button SzczytyButton = findViewById(R.id.btnSzczyty);
        SzczytyButton.setOnClickListener(v -> {
            ileszczyty++;
            Log.d("ile", String.valueOf(ileszczyty));
            widoczne = ileszczyty % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : szczyty) {
                marker.setVisible(widoczne);
            }
        });
        Button StawyButton = findViewById(R.id.btnStawy);
        StawyButton.setOnClickListener(v -> {
            ilestawy++;
            Log.d("ile", String.valueOf(ilestawy));
            widoczne = ilestawy % 2 == 0;

            // Zaktualizuj widoczność wszystkich markerów
            for (Marker marker : stawy) {
                marker.setVisible(widoczne);
            }
        });*/
        // Inicjalizacja przycisku startu nawigacji


        RouteButton = findViewById(R.id.btnRoute);
        routeInputLayout = findViewById(R.id.route_input_layout);
        RouteButton.setOnClickListener(v -> {
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
        });

        topBar = findViewById(R.id.topBar);
        Button topBarButton = findViewById(R.id.topBarButton);

        topBarButton.setOnClickListener(v -> {
            LatLng stopLocation = null;
            // Ukrywanie paska i markera po kliknięciu przycisku
            addMarkerAtCurrentPosition(false);
            routeInputLayout.setVisibility(View.VISIBLE);
            for(int i =0; i<point.length; i++){
                Log.e("iiiii", String.valueOf(point[i]));
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
                        {stopLocation = addMarkerAtCenter("przystanek"+(i-1));
                            if(stopLocation != null){
                                stop1.setText(getCityAndStreetFromCoordinates(stopLocation));
                                waypointsList.add(stopLocation);
                            }}
                        point[i] = 0;
                        break;
                        case 3:
                        {stopLocation = addMarkerAtCenter("przystanek"+(i-1));
                            if(stopLocation != null){
                                stop2.setText(getCityAndStreetFromCoordinates(stopLocation));
                                waypointsList.add(stopLocation);
                            }}
                        point[i] = 0;
                        break;
                        case 4:
                        {stopLocation = addMarkerAtCenter("przystanek"+(i-1));
                            if(stopLocation != null){
                                stop3.setText(getCityAndStreetFromCoordinates(stopLocation));
                                waypointsList.add(stopLocation);
                            }}
                        point[i] = 0;
                        break;
                    }

                    //addMarkerAtCurrentPosition(false);
                }
            }
        });

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
        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuLayout.setVisibility(View.VISIBLE);
                btnMenu.setVisibility(View.GONE);
            }
        });

        background.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE); // Ukrycie menu
            //background.setVisibility(View.GONE);// Ukrycie tła
            btnMenu.setVisibility(View.VISIBLE);
            Log.d("cldsdf","sdfgh");
        });

        btnchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, ChartActivity.class);
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

        btnconfirm = findViewById(R.id.btnConfirm);
        btnconfirm.setOnClickListener(v -> {
            markerView.setVisibility(View.GONE);
            String url = "";
            if(waypointsList == null) {
                url = getDirectionsUrl(skad_location, dokad_location);
            }
            else{
                url = getDirectionsUrlWITH(skad_location, dokad_location, waypointsList);

            }
            Log.d("linkurl", url);
            new FetchDirectionsTask().execute(url);
            RouteButton.setVisibility(View.VISIBLE);
            routeInputLayout.setVisibility(View.GONE);
            // }
            //});
            //} else {
            //    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            //}

        });
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
        LatLng initialLocation = new LatLng(49.2992, 19.9496);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //loadKmlLayers();
        //KMLDownloader kmlDownloader = new KMLDownloader(this, mMap); // 'this' to kontekst, np. Activity
        //kmlDownloader.processKMLFiles();
        //kmlDownloader.copyKMLFilesFromAssets();
        KLMFiles kmlfiles = new KLMFiles(this,mMap);
        //downloadAllKMLFiles();
        kmlfiles.processKMLFiles();
        getAcceptedDangersLocations();
        /*Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home);
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
        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snowed_mountains);
        scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 85, 85, false);
        icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        LatLng rysy = new LatLng(49.179548, 20.088064);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(rysy)
                .title("Rysy")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng mnich = new LatLng(49.192500, 20.055000);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(mnich)
                .title("Mnich")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng swinica = new LatLng(49.219408, 20.009282);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(swinica)
                .title("Świnica")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng koziWierch = new LatLng(49.218412, 20.028901);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(koziWierch)
                .title("Kozi Wierch")
                .icon(icon)
                .visible(widoczneszczyty)));


        LatLng kasprowyWierch = new LatLng(49.232164, 19.981798);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(kasprowyWierch)
                .title("Kasprowy Wierch")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng krzesanica = new LatLng(49.232490, 19.912055);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(krzesanica)
                .title("Krzesanica")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng rakon = new LatLng(49.215934, 19.758468);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(rakon)
                .title("Rakoń")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng wolowiec = new LatLng(49.207515, 19.763092);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(wolowiec)
                .title("Wołowiec")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng konczystyWierch = new LatLng(49.205654, 19.807529);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(konczystyWierch)
                .title("Kończysty Wierch")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng lopata = new LatLng(49.205009, 19.778566);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(lopata)
                .title("Łopata")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng nosal = new LatLng(49.276698, 19.989603);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(nosal)
                .title("Nosal")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng miguszowieckiSzczytWielki = new LatLng(49.187222, 20.060000);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(miguszowieckiSzczytWielki)
                .title("Mięguszowiecki Szczyt Wielki")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng gesiaSzyja = new LatLng(49.259014, 20.076474);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(gesiaSzyja)
                .title("Gęsia Szyja")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng kopieniecWielki = new LatLng(49.271639, 20.016197);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(kopieniecWielki)
                .title("Kopieniec Wielki")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng sarniaSkala = new LatLng(49.264735, 19.941708);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(sarniaSkala)
                .title("Sarnia Skała")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng krokiew = new LatLng(49.267103, 19.964372);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(krokiew)
                .title("Krokiew (Tatry)")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng kondrackaKopa = new LatLng(49.236256, 19.932193);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(kondrackaKopa)
                .title("Kondracka Kopa")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng goryczkowaCzuba = new LatLng(49.232354, 19.956923);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(goryczkowaCzuba)
                .title("Goryczkowa Czuba")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng przeleczZawrat = new LatLng(49.219160, 20.016535);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(przeleczZawrat)
                .title("Przełęcz Zawrat (2159 m n.p.m.)")
                .icon(icon)
                .visible(widoczneszczyty)));

        LatLng giewont = new LatLng(49.251002, 19.934035);
        szczyty.add(googleMap.addMarker(new MarkerOptions()
                .position(giewont)
                .title("Giewont")
                .icon(icon)
                .visible(widoczneszczyty)));

        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_pin);
        scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 85, 85, false);
        icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        LatLng czarnyStawPolski = new LatLng(49.204640, 20.025915);
        stawy.add(googleMap.addMarker(new MarkerOptions()
                .position(czarnyStawPolski)
                .title("Czarny Staw Polski")
                .icon(icon)
                .visible(widocznestawy)));

        LatLng wielkiStaw = new LatLng(49.208623, 20.039697);
        stawy.add(googleMap.addMarker(new MarkerOptions()
                .position(wielkiStaw)
                .title("Wielki Staw")
                .icon(icon)
                .visible(widocznestawy)));

        LatLng morskieOko = new LatLng(49.197141, 20.070087);
        stawy.add(googleMap.addMarker(new MarkerOptions()
                .position(morskieOko)
                .title("Morskie Oko")
                .icon(icon)
                .visible(widocznestawy)));

        LatLng czarnyStawPodRysami = new LatLng(49.190285, 20.073716);
        stawy.add(googleMap.addMarker(new MarkerOptions()
                .position(czarnyStawPodRysami)
                .title("Czarny Staw pod Rysami")
                .icon(icon)
                .visible(widocznestawy)));

        LatLng dolinaPieciuStawow = new LatLng(49.210956, 20.046530);
        stawy.add(googleMap.addMarker(new MarkerOptions()
                .position(dolinaPieciuStawow)
                .title("Dolina Pięciu Stawów Polskich")
                .icon(icon)
                .visible(widocznestawy)));

        LatLng zadniStawPolski = new LatLng(49.213177, 20.013175);
        stawy.add(googleMap.addMarker(new MarkerOptions()
                .position(zadniStawPolski)
                .title("Zadni Staw Polski")
                .icon(icon)
                .visible(widocznestawy)));*/

    }
    private LatLng addMarkerAtCenter(String name) {
        // Pobranie obecnego widoku mapy i dodanie markera
        LatLng center = mMap.getCameraPosition().target;

        Marker existingMarker = markers.get(name);
        Log.d("sdfghjk", String.valueOf(existingMarker));
        if (existingMarker != null) {
            // Usunięcie istniejącego markera
            existingMarker.remove();
        }

        // Dodanie markera
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(center).title(name));
        // Zapisanie nowego markera w mapie
        markers.put(name, newMarker);
        // Wyświetlenie komunikatu o zapisaniu współrzędnych
        Toast.makeText(this, "Marker dodany na: " + center, Toast.LENGTH_SHORT).show();

        // Logowanie współrzędnych markera
        Log.d("MainActivity", "Współrzędne markera: " + center);

        return center;
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

    private void updateMapWithAcceptedDangers(List<LatLng> locations) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.warning_sign);
        //Bitmap originalBitmap = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.warning_sign));
        //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.warning_sign);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 85, 85, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        // Załóżmy, że masz instancję GoogleMap
        for (LatLng location : locations) {
            // Dodaj marker na mapie dla każdej lokalizacji
            mMap.addMarker(new MarkerOptions().position(location).icon(icon).title("Zagrożenie"));
        }
    }

    private void getAcceptedDangersLocations() {
        // Pobieramy kolekcję "dangers" z Firestore
        db.collection("dangers")
                .whereEqualTo("accepted", true)  // Filtrujemy tylko zaakceptowane zgłoszenia
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Lista, w której przechowamy lokalizacje zaakceptowanych zgłoszeń
                        List<LatLng> locations = new ArrayList<>();
                        // Iterujemy przez dokumenty i wyciągamy lokalizacje
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Sprawdzamy, czy dokument zawiera dane o lokalizacji
                            if (documentSnapshot.contains("location")) {
                                Map<String, Object> locationmap = (Map<String, Object>) documentSnapshot.get("location");

                                if (locationmap != null) {
                                    // Jeśli pole "location" istnieje, pobieramy współrzędne
                                    double latitude = (double) locationmap.get("latitude");
                                    double longitude = (double) locationmap.get("longitude");

                                    // Tworzymy LatLng i dodajemy do listy
                                    LatLng location = new LatLng(latitude, longitude);
                                    locations.add(location);

                                    // Możemy tu dodać dodatkowe działania z lokalizacjami, np. wyświetlanie na mapie
                                    Log.d("Accepted Danger", "Location: " + latitude + ", " + longitude);
                                }
                            }
                        }

                        // Po zakończeniu możemy np. wyświetlić lokalizacje na mapie lub zrobić coś innego
                        if (!locations.isEmpty()) {
                            // Tutaj możesz zrobić coś z listą lokalizacji
                            // Na przykład, zaktualizować mapę
                            updateMapWithAcceptedDangers(locations);
                        } else {
                            Toast.makeText(MapActivity.this, "Brak zaakceptowanych zgłoszeń.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MapActivity.this, "Brak zgłoszeń w bazie.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapActivity.this, "Błąd przy pobieraniu zgłoszeń: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Błąd przy pobieraniu zaakceptowanych zgłoszeń", e);
                });
    }

}