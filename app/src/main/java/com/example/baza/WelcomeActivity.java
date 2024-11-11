package com.example.baza;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

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

        btnSignIn.setOnClickListener(v -> {
            Intent signInIntent = new Intent(WelcomeActivity.this, SignInActivity.class);
            startActivity(signInIntent);
        });

        // Wywołanie pobierania plików KML po kliknięciu przycisku
        downloadAllKMLFiles();
    }

    public void downloadAllKMLFiles() {
        // Odwołanie do folderu, w którym znajdują się pliki KML
        StorageReference folderRef = storage.getReference().child("szlaki");

        // Pobranie listy plików w folderze
        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        downloadKMLFile(fileRef);
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd podczas pobierania listy plików", exception));
    }

    private void downloadKMLFile(StorageReference fileRef) {
        // Utwórz lokalny folder „szlaki”, jeśli nie istnieje
        File localDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "szlaki");
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        // Utwórz plik w lokalnym folderze z nazwą taką samą jak na Firebase
        File localFile = new File(localDir, fileRef.getName());

        // Pobierz plik z Firebase Storage
        fileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Pobrano plik: " + fileRef.getName()))
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd pobierania pliku: " + fileRef.getName(), exception));
    }
}
