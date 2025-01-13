package com.example.baza;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
                    Toast.makeText(AdminDangerActivity.this, "Powiadomienie utworzone prawidłowo, ale funkcjonalność nie działa zgodnie z dokumentacją.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDangerActivity.this, "Wpisz treść powiadomienia", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
