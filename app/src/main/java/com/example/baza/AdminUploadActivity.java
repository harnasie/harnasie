package com.example.baza;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminUploadActivity extends AppCompatActivity {

    private Button openMyMapsButton, selectFileButton, uploadFileButton, openConverterButton;
    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload);

        openMyMapsButton = findViewById(R.id.openMyMapsButton);
        selectFileButton = findViewById(R.id.selectFileButton);
        uploadFileButton = findViewById(R.id.uploadFileButton);
        openConverterButton = findViewById(R.id.openConverterButton);

        openMyMapsButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/d/u/0/"));
            startActivity(browserIntent);
        });

        selectFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.google-earth.kml+xml");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Wybierz plik KML"), PICK_FILE_REQUEST);
        });

        uploadFileButton.setOnClickListener(v -> {
            if (fileUri != null) {
                uploadFileToFirebase(fileUri);
            } else {
                Toast.makeText(this, "Nie wybrano pliku!", Toast.LENGTH_SHORT).show();
            }
        });

        openConverterButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mygeodata.cloud/converter/"));
            startActivity(browserIntent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            ContentResolver contentResolver = getContentResolver();
            String mimeType = contentResolver.getType(fileUri);

            if ("application/vnd.google-earth.kml+xml".equals(mimeType)) {
                Toast.makeText(this, "Wybrano plik KML", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nieprawidłowy plik! Wybierz plik KML.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFileToFirebase(Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference kmlRef = storageRef.child("kml_files/" + System.currentTimeMillis() + "_map.kml");

        kmlRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Plik przesłany pomyślnie!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd podczas przesyłania pliku: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
