package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private static final String TAG = "KMLDownloader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Inicjalizacja Firebase Storage
        storage = FirebaseStorage.getInstance();

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        Button btnSignIn = findViewById(R.id.btn_sign_in);

        btnSignUp.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            //Intent signInIntent = new Intent(WelcomeActivity.this, UserActivity.class);
            //startActivity(signInIntent);
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        // Wywołanie pobierania plików KML po kliknięciu przycisku
        downloadAllKMLFiles("szlaki");
        downloadAllKMLFiles("marker");
    }

    public void downloadAllKMLFiles(String filename) {
        // Odwołanie do folderu, w którym znajdują się pliki KML
        StorageReference folderRef = storage.getReference().child(filename);

        // Pobranie listy plików w folderze
        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        // Dla każdego pliku sprawdzamy, czy go pobrać
                        checkAndDownloadFile(fileRef, filename);
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd podczas pobierania listy plików", exception));
    }

    private void checkAndDownloadFile(StorageReference fileRef, String filename) {
        // Utwórz lokalny folder, jeśli nie istnieje
        File localDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        // Utwórz plik w lokalnym folderze z nazwą taką samą jak na Firebase
        File localFile = new File(localDir, fileRef.getName());

        // Sprawdź, czy plik już istnieje lokalnie
        if (localFile.exists()) {
            // Oblicz hash lokalnego pliku
            String localFileHash = getFileHash(localFile);

            // Pobierz metadane pliku z Firebase, aby porównać hash
            fileRef.getMetadata().addOnSuccessListener(metadata -> {
                // Możesz dodać logikę porównania hash'y, jeśli Firebase przechowuje hash w metadanych
                // Na przykład załóżmy, że mamy do porównania jakąś wartość z metadanych:
                String serverFileHash = metadata.getCustomMetadata("hash");  // Przykład: jeśli hash jest zapisany w metadanych
                if (serverFileHash != null && serverFileHash.equals(localFileHash)) {
                    Log.d(TAG, "Plik jest już aktualny, nie trzeba pobierać: " + fileRef.getName());
                } else {
                    // Jeśli hash się różni, pobierz plik
                    downloadFile(fileRef, localFile);
                }
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Błąd pobierania metadanych dla pliku: " + fileRef.getName(), exception);
                // Jeśli wystąpił błąd przy pobieraniu metadanych, pobierz plik
                downloadFile(fileRef, localFile);
            });
        } else {
            // Jeśli plik nie istnieje lokalnie, pobierz go
            downloadFile(fileRef, localFile);
        }
    }

    private void downloadFile(StorageReference fileRef, File localFile) {
        // Pobierz plik z Firebase Storage
        fileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    // Sprawdź, czy plik został pobrany
                    if (localFile.exists()) {
                        Log.d(TAG, "Pobrano plik: " + fileRef.getName());
                    } else {
                        Log.e(TAG, "Plik nie istnieje po pobraniu, usuwam: " + fileRef.getName());
                        localFile.delete();  // Usuwamy plik, jeśli nie istnieje po pobraniu
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Błąd pobierania pliku: " + fileRef.getName(), exception);
                    // Jeśli wystąpił błąd, usuń plik, jeśli istnieje
                    if (localFile.exists()) {
                        Log.e(TAG, "Usuwam plik, ponieważ wystąpił błąd: " + fileRef.getName());
                        localFile.delete();  // Usuwamy plik, jeśli wystąpił błąd
                    }
                });
    }

    // Funkcja pomocnicza do obliczania hash'a pliku
    private String getFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
            fis.close();
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Błąd obliczania hash'a pliku: " + e.getMessage());
            return null;
        }
    }

}
