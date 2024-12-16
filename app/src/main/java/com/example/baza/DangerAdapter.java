package com.example.baza;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class DangerAdapter extends ArrayAdapter<Map<String, Object>> {
    private final Context context;
    private final List<Map<String, Object>> dangers;
    private final FirebaseFirestore db;

    public DangerAdapter(Context context, List<Map<String, Object>> dangers) {
        super(context, R.layout.list_item_danger, dangers);
        this.context = context;
        this.dangers = dangers;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_danger, parent, false);
        }

        // Pobieramy widoki z layoutu
        TextView descriptionTextView = convertView.findViewById(R.id.textViewDescription);
        TextView typeTextView = convertView.findViewById(R.id.textViewType);
        Button acceptButton = convertView.findViewById(R.id.buttonAcceptDanger);

        // Pobieramy dane zagrożenia
        Map<String, Object> danger = dangers.get(position);

        // Ustawiamy opis
        String description = (String) danger.get("description");
        descriptionTextView.setText("Opis:" + description);

        // Ustawiamy typ
        String type = (String) danger.get("type");
        typeTextView.setText(type);
        Log.d("lololo", String.valueOf(6789));
        /*LatLng location = (LatLng) danger.get("location");
        Log.d("lololo", String.valueOf(location));*/
        // Pobieramy ID zagrożenia z dokumentu
        String dangerId = (String) danger.get("id");

        // Akcja przycisku "Akceptuj"
        acceptButton.setOnClickListener(v -> {
            acceptDanger(dangerId);
            markerDanger(dangerId);
        });

        return convertView;
    }

    private void acceptDanger(String dangerId) {
        db.collection("dangers").document(dangerId)
                .update("accepted", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Zagrożenie zaakceptowane.", Toast.LENGTH_SHORT).show();
                    sendNotificationToNearbyUsers(dangerId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Błąd przy akceptacji zagrożenia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToNearbyUsers(String dangerId) {
        db.collection("dangers").document(dangerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Pobierz lokalizację jako Map
                        Map<String, Object> locationMap = (Map<String, Object>) documentSnapshot.get("location");

                        if (locationMap != null) {
                            // Pobierz współrzędne z Mapy
                            double latitude = (double) locationMap.get("latitude");
                            double longitude = (double) locationMap.get("longitude");

                            // Utwórz obiekt GeoPoint
                            GeoPoint location = new GeoPoint(latitude, longitude);

                            // Wywołaj funkcję do wysyłania powiadomień
                            notifyNearbyUsers(location);
                        } else {
                            Log.e("LocationError", "Brak lokalizacji w zgłoszeniu.");
                        }
                    } else {
                        Log.e("FirestoreError", "Dokument nie istnieje.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Błąd przy pobieraniu dokumentu: " + e.getMessage());
                });
    }



    private void markerDanger(String dangerId) {
        db.collection("dangers").document(dangerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> location = (Map<String, Object>) documentSnapshot.get("location");

                        if (location != null) {
                            double latitude = (double) location.get("latitude");
                            double longitude = (double) location.get("longitude");

                            Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                        } else {
                            Log.e("Location", "Brak lokalizacji w zgłoszeniu.");
                        }
                    } else {
                        Log.e("Firestore", "Zgłoszenie nie istnieje.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Błąd przy pobieraniu dokumentu: " + e.getMessage());
                });

    }

    public static void sendPushNotification(double latitude, double longitude, String message) {
        try {
            JSONObject notificationContent = new JSONObject();
            notificationContent.put("contents", new JSONObject().put("en", message));

            // Filtr lokalizacyjny – użytkownicy w promieniu 500 metrów
            notificationContent.put("filters", new org.json.JSONArray()
                    .put(new JSONObject()
                            .put("field", "location")
                            .put("radius", 0.5)
                            .put("latitude", latitude)
                            .put("longitude", longitude)));

            OneSignal.postNotification(
                    notificationContent,
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.d("OneSignal", "Powiadomienie wysłane: " + response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject error) {
                            Log.e("OneSignal", "Błąd wysyłania powiadomienia: " + error.toString());
                        }
                    }
            );
        } catch (JSONException e) {
            Log.e("OneSignal", "Błąd tworzenia JSON: " + e.getMessage());
        }
    }

    private void notifyNearbyUsers(GeoPoint location) {
        String message = "Zgłoszono nowe zagrożenie w Twojej okolicy!";

        try {
            JSONObject notificationContent = new JSONObject();
            notificationContent.put("contents", new JSONObject().put("en", message));
            notificationContent.put("included_segments", new JSONArray().put("All")); // Wysyła do wszystkich użytkowników

            // Dodaj dane lokalizacyjne (opcjonalnie, jeśli chcesz filtrować po stronie klienta)
            JSONObject data = new JSONObject();
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            notificationContent.put("data", data);

            OneSignal.postNotification(
                    notificationContent,
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Toast.makeText(getContext(), "Powiadomienie wysłane!", Toast.LENGTH_SHORT).show();
                            Log.d("OneSignalSuccess", response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject error) {
                            Toast.makeText(getContext(), "Błąd wysyłania powiadomienia: " + error.toString(), Toast.LENGTH_SHORT).show();
                            Log.e("OneSignalError", error.toString());
                        }
                    });
        } catch (JSONException e) {
            Log.e("NotificationError", "Błąd tworzenia powiadomienia: " + e.getMessage());
        }
    }


}
