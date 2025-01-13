package com.example.baza;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocationTrackingService extends Service {

    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private String uid;
    private FirebaseFirestore db;
    private Location lastLocation = null;
    private double totalDistance = 0.0;
    private double dailyDistance = 0.0;
    private String currentDate = null;


    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTrackingService", "Brak uprawnień do lokalizacji");
            stopSelf();
            return;
        }

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Śledzenie lokalizacji")
                .setContentText("Twoja lokalizacja jest śledzona w tle")
                .setSmallIcon(R.drawable.marker_icon)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d("LocationTrackingService", "Lokalizacja: " + location);

                        if (lastLocation != null) {
                            double distance = calculateDistance(location);
                            dailyDistance += distance;
                        }

                        lastLocation = location;
                        saveLocationToFirestore(location, calculateDistance(location));
                        saveDailyDistanceToFirestore();
                    }
                }
            }

        };

        startLocationUpdates();
    }


    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Log.e("LocationTrackingService", "Brak uprawnień do lokalizacji", e);
        }
    }

    private double calculateDistance(Location currentLocation) {
        if (lastLocation != null) {
            double distance = lastLocation.distanceTo(currentLocation);
            totalDistance += distance;
            lastLocation = currentLocation;
            return distance / 1000.0;
        } else {
            lastLocation = currentLocation;
            return 0.0;
        }
    }

    private void saveLocationToFirestore(Location location, double calculatedDistance) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            Log.d("FirebaseAuth", "User UID: " + uid);
        } else {
            Log.e("FirebaseAuth", "No user is logged in!");
        }

        Map<String, Object> activityData = new HashMap<>();
        activityData.put("uid", uid);
        activityData.put("distance", calculatedDistance);
        activityData.put("totalDistance", totalDistance);
        activityData.put("latitude", location.getLatitude());
        activityData.put("longitude", location.getLongitude());
        activityData.put("date", new Date());

        db.collection("walking")
                .add(activityData)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Dane o aktywności dodane"))
                .addOnFailureListener(e -> Log.e("Firestore", "Błąd zapisu aktywności do bazy", e));
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Śledzenie lokalizacji",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private void saveDailyDistanceToFirestore() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            Log.d("FirebaseAuth", "User UID: " + uid);
        } else {
            Log.e("FirebaseAuth", "No user is logged in!");
        }

        if (currentDate == null || !currentDate.equals(today)) {
            currentDate = today;
            dailyDistance = 0.0;
        }

        Map<String, Object> dailyData = new HashMap<>();
        dailyData.put("date", today);
        dailyData.put("distance", dailyDistance);
        dailyData.put("uid", uid);

        db.collection("daily_distances")
                .document(uid + "_" + today)
                .set(dailyData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Dzienny dystans zapisany"))
                .addOnFailureListener(e -> Log.e("Firestore", "Błąd zapisu dziennego dystansu", e));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
