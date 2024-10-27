package com.example.baza;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    //UserDatabaseHelper dbHelper;
    DangerDatabaseHelper dbHelper;
    boolean isDeleted;
    EditText editName, editEmail, editDay, editDistance, editUser;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    Button btnAddUser, btnViewUsers, btnAddDistance, btnViewDangers, btnTelefon, btnMap, btnDelete, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editName = findViewById(R.id.editTextName);
        editEmail = findViewById(R.id.editTextEmail);
        editUser = findViewById(R.id.editTextType);
        editDistance = findViewById(R.id.editTextDescription);
        editDay = findViewById(R.id.editTextLocation);
        btnAddUser = findViewById(R.id.buttonAddUser);
        btnViewUsers = findViewById(R.id.buttonViewUsers);
        btnAddDistance = findViewById(R.id.buttonAddDanger);
        btnViewDangers = findViewById(R.id.buttonViewDangers);
        btnDelete = findViewById(R.id.buttonDelete);
        btnLogin = findViewById(R.id.buttonLogin);
        btnTelefon = findViewById(R.id.buttonTelefon);
        btnMap = findViewById(R.id.buttonMap);
        dbHelper = new DangerDatabaseHelper(this);
        //FirebaseApp.initializeApp(this);
        //database = new Database(this);
        // Dodawanie użytkownika
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String email = editEmail.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Pole imię nie może być puste", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Pole email nie może być puste", Toast.LENGTH_SHORT).show();
                }
                // Sprawdzanie poprawności formatu e-mail
                else if (!EMAIL_PATTERN.matcher(email).matches()) {
                    Toast.makeText(MainActivity.this, "Nieprawidłowy format adresu email", Toast.LENGTH_SHORT).show();
                } else {
                    // Jeśli walidacja jest poprawna, dodaj użytkownika do bazy danych Firestore
                    dbHelper.addUser(name, email);

                    // Obsługa w listenerze w metodzie `addUser`
                    Toast.makeText(MainActivity.this, "Dodano użytkownika", Toast.LENGTH_SHORT).show();
                    editName.setText("");
                    editEmail.setText("");
                }
            }
        });


        // Przejście do listy użytkowników
        btnViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewUsersActivity.class);
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        btnAddDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = editUser.getText().toString();
                String distance = editDistance.getText().toString();
                String day = editDay.getText().toString();
                Log.e("u123", user.toString());
                Log.e("dis123", distance.toString());
                Log.e("d123", day.toString());
                if (user.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Pole user nie może być puste", Toast.LENGTH_SHORT).show();
                } else if (distance.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Pole dystans nie może być puste", Toast.LENGTH_SHORT).show();
                } else if (day.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Pole day nie może być puste", Toast.LENGTH_SHORT).show();
                }
                // Sprawdzanie poprawności formatu e-mail
                else {
                    // Jeśli walidacja jest poprawna, dodaj użytkownika do bazy danych
                    //boolean isInserted = dbHelper.addDistance(user, distance, day);
                    dbHelper.addDistance(user,distance,day);
                    Toast.makeText(MainActivity.this, "Dodano zgłoszenie", Toast.LENGTH_SHORT).show();
                    editUser.setText("");
                    editDistance.setText("");
                    editDay.setText("");
                }
            }
        });

        btnViewDangers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewDangerActivity.class);
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    boolean isDeleted = getApplicationContext().deleteDatabase("harnas.db");
                    if (isDeleted) {
                        Toast.makeText(MainActivity.this, "Baza danych została usunięta", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Błąd przy usuwaniu bazy danych", Toast.LENGTH_SHORT).show();
                    }
                }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        btnTelefon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TelefonActivity.class);
                startActivity(intent);
            }
        });
    }


}
