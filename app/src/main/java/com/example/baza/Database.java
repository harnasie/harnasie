//package com.example.baza;
//
//import android.content.Context;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Database {
//
//    private FirebaseFirestore db;
//    private Context context;
//
//    public Database(Context context) {
//        // Inicjalizacja Firebase Firestore
//        //FirebaseApp.initializeApp(context);
//        this.context = context;
//        db = FirebaseFirestore.getInstance();
//    }
//
//    // Dodawanie nowego rekordu do Firestore
//    public void addDanger(String type, String location, String description, String user) {
//        Map<String, Object> dangerData = new HashMap<>();
//        dangerData.put("type", type);
//        dangerData.put("location", location);
//        dangerData.put("user", user);
//        dangerData.put("description", description);
//        dangerData.put("createdAt", System.currentTimeMillis());
//        dangerData.put("accepted", 0); // Domyślnie niezaakceptowane
//
//        // Dodanie rekordu do kolekcji "dangers"
//        db.collection("dangers")
//                .add(dangerData)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Firestore", "Danger added with ID: " + documentReference.getId());
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "Error adding danger", e);
//                });
//    }
//
//    // Dodawanie użytkownika do Firestore
//    public void addUser(String name, String email) {
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("name", name);
//        userData.put("email", email);
//
//        // Dodanie rekordu do kolekcji "users"
//        db.collection("users")
//                .add(userData)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Firestore", "User added with ID: " + documentReference.getId());
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "Error adding user", e);
//                });
//    }
//
//    // Dodanie rekordu distance (odległość przebyta przez użytkownika)
//    public void addDistance(String user, String distance, String day) {
//        Map<String, Object> distanceData = new HashMap<>();
//        distanceData.put("user", user);
//        distanceData.put("distance", distance);
//        distanceData.put("day", day);
//
//        // Dodanie rekordu do kolekcji "distances"
//        db.collection("distances")
//                .add(distanceData)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Firestore", "Distance added with ID: " + documentReference.getId());
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "Error adding distance", e);
//                });
//    }
//
//    // Pobieranie wszystkich rekordów z kolekcji dangers
//    public void getAllDangers() {
//        CollectionReference dangersRef = db.collection("dangers");
//        dangersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d("Firestore", document.getId() + " => " + document.getData());
//                    }
//                } else {
//                    Log.w("Firestore", "Error getting dangers.", task.getException());
//                }
//            }
//        });
//    }
//
//    // Pobieranie wszystkich użytkowników
//    public void getAllUsers() {
//        CollectionReference usersRef = db.collection("users");
//        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d("Firestore", document.getId() + " => " + document.getData());
//                    }
//                } else {
//                    Log.w("Firestore", "Error getting users.", task.getException());
//                }
//            }
//        });
//    }
//
//    // Pobieranie wszystkich odległości
//    public void getAllDistances() {
//        CollectionReference distancesRef = db.collection("distances");
//        distancesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d("Firestore", document.getId() + " => " + document.getData());
//                    }
//                } else {
//                    Log.w("Firestore", "Error getting distances.", task.getException());
//                }
//            }
//        });
//    }
//
//    // Aktualizacja rekordu w kolekcji dangers (np. akceptacja)
//    public void updateDanger(String documentId, int accepted) {
//        db.collection("dangers").document(documentId)
//                .update("accepted", accepted)
//                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Danger successfully updated!"))
//                .addOnFailureListener(e -> Log.w("Firestore", "Error updating danger", e));
//    }
//
//    // Aktualizacja danych użytkownika
//    public void updateUser(String documentId, String name, String email) {
//        db.collection("users").document(documentId)
//                .update("name", name, "email", email)
//                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully updated!"))
//                .addOnFailureListener(e -> Log.w("Firestore", "Error updating user", e));
//    }
//
//    // Usuwanie rekordu z kolekcji dangers
//    public void deleteDanger(String documentId) {
//        db.collection("dangers").document(documentId)
//                .delete()
//                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Danger successfully deleted!"))
//                .addOnFailureListener(e -> Log.w("Firestore", "Error deleting danger", e));
//    }
//
//    // Usuwanie użytkownika
//    public void deleteUser(String documentId) {
//        db.collection("users").document(documentId)
//                .delete()
//                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully deleted!"))
//                .addOnFailureListener(e -> Log.w("Firestore", "Error deleting user", e));
//    }
//
//    // Sprawdzanie, czy użytkownik istnieje
//    public void checkUser(String name, String email) {
//        db.collection("users")
//                .whereEqualTo("name", name)
//                .whereEqualTo("email", email)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                        Log.d("Firestore", "User exists.");
//                    } else {
//                        Log.d("Firestore", "User does not exist.");
//                    }
//                });
//    }
//}
