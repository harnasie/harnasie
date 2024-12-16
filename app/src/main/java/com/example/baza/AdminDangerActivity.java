package com.example.baza;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminDangerActivity extends AppCompatActivity {

    private EditText editTextNotificationMessage;
    private Button buttonSendNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_danger);

        editTextNotificationMessage = findViewById(R.id.editTextDangerMessage);
        buttonSendNotification = findViewById(R.id.buttonSendDanger);

        buttonSendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextNotificationMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendGlobalNotification(message);
                } else {
                    Toast.makeText(AdminDangerActivity.this, "Wpisz treść powiadomienia", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendNotification(String message) {
        try {
            JSONObject notificationContent = new JSONObject();
            notificationContent.put("app_id", "cf851d9a-5124-45fb-be68-591645364d51");
            notificationContent.put("included_segments", new JSONArray().put("All"));
            notificationContent.put("contents", new JSONObject().put("en", message));
            notificationContent.put("headings", new JSONObject().put("en", "Alert"));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://onesignal.com/api/v1/notifications",
                    notificationContent,
                    response -> {
                        Toast.makeText(this, "Powiadomienie wysłane", Toast.LENGTH_SHORT).show();
                        Log.d("OneSignalSuccess", response.toString());
                    },
                    error -> {
                        String errorMessage = error.networkResponse != null ?
                                "HTTP status: " + error.networkResponse.statusCode :
                                "Nieznany błąd";
                        Toast.makeText(this, "Błąd: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("OneSignalError", errorMessage);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Basic os_v2_app_z6cr3gsrerc7xptileleknsnkh4ez656o7oeyxmwlyefgx5327aypwlf3j3qb4meoypoap4qf45v4hbeovdswstpqzlwwapbb5iadli");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            Log.e("JSONException", "Error creating notification JSON", e);
            Toast.makeText(this, "Błąd tworzenia powiadomienia", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendGlobalNotification(String message) {
        String restApiKey = "os_v2_app_z6cr3gsrerc7xptileleknsnkh4ez656o7oeyxmwlyefgx5327aypwlf3j3qb4meoypoap4qf45v4hbeovdswstpqzlwwapbb5iadli";
        String appId = "cf851d9a-5124-45fb-be68-591645364d51";

        JSONObject notification = new JSONObject();
        try {
            notification.put("app_id", appId);
            notification.put("included_segments", new JSONArray().put("All"));
            notification.put("contents", new JSONObject().put("en", message));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://onesignal.com/api/v1/notifications",
                    notification,
                    response -> {
                        Toast.makeText(this, "Powiadomienie wysłane", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(this, "Błąd wysyłania powiadomienia", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Basic " + restApiKey);
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
