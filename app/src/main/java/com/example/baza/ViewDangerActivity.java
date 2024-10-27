package com.example.baza;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ViewDangerActivity extends AppCompatActivity {

    DangerDatabaseHelper dbHelper;
    ListView listViewDangers;
    ArrayList<String> dangerList;
    ArrayList<Integer> dangerIds;
    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_dangers);

        dbHelper = new DangerDatabaseHelper(this);
        listViewDangers = findViewById(R.id.listViewDangers);
        dangerList = new ArrayList<>();
        dangerIds = new ArrayList<>();

        loadDangers();
    }

    // Ładowanie danych z bazy danych
    private void loadDangers() {
        dangerList.clear();
        dangerIds.clear();

        Cursor cursor = dbHelper.getAllDangers();
        if (cursor.getCount() == 0) {
            dangerList.add("Brak rekordów");
        } else {
            while (cursor.moveToNext()) {
                int dangerId = cursor.getInt(0);
                String danger = "ID: " + dangerId + ", Typ: " + cursor.getString(1) + ", Lokalizacja: " + cursor.getString(2) + ", Użytkownik: " + cursor.getString(3) + ", Opis: " + cursor.getString(4) + ", Data: " + formatDate(cursor.getLong(5)) + "Accpeted" + cursor.getString(6);
                dangerList.add(danger);
                dangerIds.add(dangerId);
            }
        }

        customAdapter = new CustomAdapter();
        listViewDangers.setAdapter(customAdapter);
    }

    // Niestandardowy adapter dla listy
    private class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter() {
            super(ViewDangerActivity.this, R.layout.list_item_danger, R.id.textDanger, dangerList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_danger, parent, false);
            }

            TextView textDanger = convertView.findViewById(R.id.textDanger);
            Button buttonAccept = convertView.findViewById(R.id.buttonAccept);
            Button buttonReject = convertView.findViewById(R.id.buttonReject);

            textDanger.setText(dangerList.get(position));

            final int dangerId = dangerIds.get(position);

            // Akcja przycisku Akceptuj
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(buttonAccept.isEnabled()){
                    updateDanger(dangerId, 1);
                    buttonReject.setEnabled(false);
                    buttonReject.setBackgroundColor(Color.GRAY);} // Wyszarzenie przycisku
                    Log.d("ButtonTest", "Odrzuć button: enabled = " + buttonReject.isEnabled());

                }
            });

            // Akcja przycisku Odrzuć
            buttonReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(buttonReject.isEnabled()){
                    updateDanger(dangerId, 0);
                    buttonAccept.setEnabled(false);
                    buttonAccept.setBackgroundColor(Color.GRAY);} // Wyszarzenie przycisku
                    Log.d("ButtonTest", "Akcept button: enabled = " + buttonAccept.isEnabled());
                }
            });

            return convertView;
        }
    }

    // Aktualizacja rekordu w bazie danych
    private void updateDanger(int id, int accepted) {
        boolean isUpdated = dbHelper.updateDanger(id,  accepted);
        if (isUpdated) {
            Toast.makeText(this, accepted == 1 ? "Rekord zaakceptowany" : "Rekord odrzucony", Toast.LENGTH_SHORT).show();
            loadDangers();  // Odśwież listę
        } else {
            Toast.makeText(this, "Błąd przy aktualizacji rekordu", Toast.LENGTH_SHORT).show();
        }
    }

    // Formatowanie znacznika czasu na czytelny format
    public String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
