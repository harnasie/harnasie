package com.example.baza;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class DangerAdapter extends ArrayAdapter<Map<String, Object>> {
    private final Context context;
    private final List<Map<String, Object>> dangers;
    private final FirebaseFirestore db;

    public DangerAdapter(Context context, List<Map<String, Object>> dangers) {
        super(context, R.layout.list_item_danger, dangers);
        this.context = context;
        this.dangers = dangers;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_danger, parent, false);
        }

        // Pobieramy widoki z layoutu
        TextView descriptionTextView = convertView.findViewById(R.id.textViewDescription);
        TextView typeTextView = convertView.findViewById(R.id.textViewType);
        Button acceptButton = convertView.findViewById(R.id.buttonAcceptDanger);

        // Pobieramy dane zagrożenia
        Map<String, Object> danger = dangers.get(position);

        // Ustawiamy opis
        String description = (String) danger.get("description");
        descriptionTextView.setText("Opis:" + description);

        // Ustawiamy typ
        String type = (String) danger.get("type");
        typeTextView.setText(type);

        // Pobieramy ID zagrożenia z dokumentu
        String dangerId = (String) danger.get("id");

        // Akcja przycisku "Akceptuj"
        acceptButton.setOnClickListener(v -> {
            acceptDanger(dangerId);
        });

        return convertView;
    }

    private void acceptDanger(String dangerId) {
        // Zmieniamy status "accepted" na true
        db.collection("dangers").document(dangerId)
                .update("accepted", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Zagrożenie zaakceptowane.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Błąd przy akceptacji zagrożenia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
