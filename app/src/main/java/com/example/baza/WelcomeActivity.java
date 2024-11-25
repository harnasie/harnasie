package com.example.baza;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

        storage = FirebaseStorage.getInstance();

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        Button btnSignIn = findViewById(R.id.btn_sign_in);
        Button btnMap = findViewById(R.id.btn_map);

        btnSignUp.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        });

        btnMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(WelcomeActivity.this, MapActivity.class);
            startActivity(mapIntent);
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
        downloadAllKMLFiles("szlaki");
        downloadAllKMLFiles("marker");

        if (isUserLoggedIn()) {
            // Użytkownik jest zalogowany, pobierz rolę i przekieruj
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            fetchRoleAndRedirect(userId);
        } else {
            // Przekieruj na ekran logowania
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void downloadAllKMLFiles(String filename) {
        StorageReference folderRef = storage.getReference().child(filename);

        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        checkAndDownloadFile(fileRef, filename);
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Błąd podczas pobierania listy plików", exception));
    }

    private void checkAndDownloadFile(StorageReference fileRef, String filename) {
        File localDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        File localFile = new File(localDir, fileRef.getName());

        if (localFile.exists()) {
            fileRef.getMetadata().addOnSuccessListener(metadata -> {
                long serverFileSize = metadata.getSizeBytes();
                long localFileSize = localFile.length();

                if (serverFileSize == localFileSize) {
                    Log.d(TAG, "Plik jest już aktualny, nie trzeba pobierać: " + fileRef.getName());
                } else {
                    Log.d(TAG, "Plik różni się od lokalnej wersji, pobieram nową wersję: " + fileRef.getName());
                    downloadFile(fileRef, localFile);
                }
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Błąd pobierania metadanych dla pliku: " + fileRef.getName(), exception);

                if (localFile.exists()) {
                    localFile.delete();
                    Log.d(TAG, "Usunięto lokalny plik, ponieważ nie istnieje na serwerze: " + localFile.getName());
                }

                downloadFile(fileRef, localFile);
            });
        } else {
            fileRef.getMetadata().addOnSuccessListener(metadata -> {
                long serverFileSize = metadata.getSizeBytes();
                Log.d(TAG, "Plik nie istnieje lokalnie, ale jest na serwerze. Pobieram: " + fileRef.getName());
                downloadFile(fileRef, localFile);
            }).addOnFailureListener(exception -> {
                if (localFile.exists()) {
                    localFile.delete();
                    Log.d(TAG, "Usunięto lokalny plik, ponieważ nie istnieje na serwerze: " + localFile.getName());
                }
            });
        }
    }


    private void downloadFile(StorageReference fileRef, File localFile) {
        fileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    if (localFile.exists()) {
                        Log.d(TAG, "Pobrano plik: " + fileRef.getName());
                    } else {
                        Log.e(TAG, "Plik nie istnieje po pobraniu, usuwam: " + fileRef.getName());
                        localFile.delete();
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Błąd pobierania pliku: " + fileRef.getName(), exception);
                    if (localFile.exists()) {
                        Log.e(TAG, "Usuwam plik, ponieważ wystąpił błąd: " + fileRef.getName());
                        localFile.delete();
                    }
                });
    }

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

    private boolean isUserLoggedIn() {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return preferences.getBoolean("isLoggedIn", false);
    }

    private void fetchRoleAndRedirect(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Intent intent;
                        if ("admin".equalsIgnoreCase(role)) {
                            intent = new Intent(this, AdminMenuActivity.class);
                        } else {
                            intent = new Intent(this, UserActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                });
    }

}
