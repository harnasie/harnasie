package com.example.baza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    //UserDatabaseHelper dbHelper;
    boolean isDeleted;
    EditText editName, editEmail, editDay, editDistance, editUser;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    Button btnAddUser, btnViewUsers, btnAddDistance, btnViewDangers, btnTelefon, btnMap, btnDelete, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //downloadKMLFile(this,"czarny-csg-koscielec");
        //downloadFileFromURL(this);
        Log.d("polacznue", String.valueOf(2));
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
        // Dodawanie użytkownika

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


    public void downloadKMLFile(Context context, String fileName) {
        // Inicjalizacja Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Ścieżka do pliku .kml w Firebase Storage
        StorageReference fileRef = storageRef.child("ścieżka/do/pliku/" + fileName);

        // Tworzenie folderu „szlaki” wewnątrz prywatnej pamięci aplikacji
        File localFolder = new File(context.getFilesDir(), "szlaki");
        if (!localFolder.exists()) {
            localFolder.mkdirs();
        }

        // Tworzy lokalny plik .kml w folderze aplikacji
        File localFile = new File(localFolder, fileName);

        // Pobieranie pliku z Firebase Storage i zapisywanie go lokalnie w folderze aplikacji
        fileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Firebase", "Pomyślnie pobrano plik: " + fileName);
                    Toast.makeText(context, "Pobrano plik: " + fileName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Błąd pobierania pliku", e);
                    Toast.makeText(context, "Błąd pobierania pliku!", Toast.LENGTH_SHORT).show();
                });
    }

    public static void downloadFileFromURL(Context context) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutput = null;
        // URL do pliku na Firebase Storage
        String fileUrl = "https://firebasestorage.googleapis.com/v0/b/harnasie-2239a.appspot.com/o/szlaki%2Fczarny-csg-koscielec.kml?alt=media";
        String fileName = "czarny-csg-koscielec.kml";


        File localFolder = new File(context.getFilesDir(), "szlaki");
        if (!localFolder.exists()) {
            localFolder.mkdirs();
        }

        // Tworzenie pliku docelowego w folderze szlaki
        File localFile = new File(localFolder, fileName);
        //fileOutput = new FileOutputStream(localFile);
        Log.d("plikkkkkkk", String.valueOf(2));

        try {
            // Tworzenie URL z podanego linku
            URL url = new URL(fileUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            Log.d("polacznue", String.valueOf(2));
            // Sprawdzenie odpowiedzi serwera
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("FileDownloader", "Serwer zwrócił kod HTTP " + urlConnection.getResponseCode()
                        + " " + urlConnection.getResponseMessage());
                Log.d("taaak", String.valueOf(2));
                return;
            }

            // Tworzenie folderu szlaki w pamięci aplikacji
            /*File localFolder = new File(context.getFilesDir(), "szlaki");
            if (!localFolder.exists()) {
                localFolder.mkdirs();
            }

            // Tworzenie pliku docelowego w folderze szlaki
            File localFile = new File(localFolder, fileName);
            fileOutput = new FileOutputStream(localFile);
            Log.d("plikkkkkkk", String.valueOf(2));*/
            // Pobieranie pliku przez InputStream
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }

            Log.d("FileDownloader", "Pomyślnie pobrano plik do: " + localFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e("FileDownloader", "Błąd pobierania pliku", e);

        } finally {
            // Zamykanie strumieni i połączenia
            try {
                if (fileOutput != null) {
                    fileOutput.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("FileDownloader", "Błąd zamykania strumieni", e);
            }
        }

// Create a reference from an HTTPS URL
// Note that in the URL, characters are URL escaped!
        //StorageReference httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/b/bucket/o/images%20stars.jpg");
    }


}
