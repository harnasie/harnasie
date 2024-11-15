package com.example.baza;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewDangerActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListView listViewDangers;
    private DangerAdapter adapter;
    private List<Map<String, Object>> dangerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_dangers);

        db = FirebaseFirestore.getInstance();
        listViewDangers = findViewById(R.id.listViewDangers);
        dangerList = new ArrayList<>();
        adapter = new DangerAdapter(this, dangerList);
        listViewDangers.setAdapter(adapter);
        setTitle("Zgłoszenia");
        loadDangers();
    }

    private void loadDangers() {
        db.collection("dangers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> danger = new HashMap<>(document.getData());
                            danger.put("id", document.getId()); // Dodaj ID dokumentu do mapy
                            dangerList.add(danger);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Błąd podczas ładowania zagrożeń.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
