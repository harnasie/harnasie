package com.example.baza;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.baza.UserDatabaseHelper;

import java.util.ArrayList;

public class ViewUsersActivity extends AppCompatActivity {

    DangerDatabaseHelper dbHelper;
    ListView listViewUsers;
    ArrayList<String> userList;
    ArrayList<Integer> userIds;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        dbHelper = new DangerDatabaseHelper(this);
        listViewUsers = findViewById(R.id.listViewUsers);
        userList = new ArrayList<>();
        userIds = new ArrayList<>();

        loadUsers();

        // Kliknięcie na użytkownika
        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int userId = userIds.get(position);
                showManageUserDialog(userId, position);
            }
        });
    }

    // Pobieranie użytkowników z bazy danych
    private void loadUsers() {
        userList.clear();
        userIds.clear();
        Cursor cursor = dbHelper.getAllUsers();
        if (cursor.getCount() == 0) {
            userList.add("Brak użytkowników");
        } else {
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(0);
                String user = "ID: " + userId + ", Imię: " + cursor.getString(1) + ", Email: " + cursor.getString(2);
                userList.add(user);
                userIds.add(userId); // Przechowywanie ID użytkownika
            }
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        listViewUsers.setAdapter(arrayAdapter);
    }

    // Dialog do zarządzania użytkownikiem (Edycja / Usuwanie)
    private void showManageUserDialog(int userId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zarządzanie użytkownikiem");

        String[] options = {"Edytuj", "Usuń"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Edytuj użytkownika
                    showEditUserDialog(userId, position);
                } else if (which == 1) {
                    // Usuń użytkownika
                    deleteUser(userId, position);
                }
            }
        });

        builder.create().show();
    }

    // Dialog do edycji użytkownika
    private void showEditUserDialog(int userId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edytuj użytkownika");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        builder.setView(view);

        EditText editName = view.findViewById(R.id.editUserName);
        EditText editEmail = view.findViewById(R.id.editUserEmail);

        // Załaduj bieżące dane użytkownika
        Cursor cursor = dbHelper.getAllUsers();
        if (cursor.moveToPosition(position)) {
            editName.setText(cursor.getString(1));
            editEmail.setText(cursor.getString(2));
        }

        builder.setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editName.getText().toString();
                String newEmail = editEmail.getText().toString();
                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(ViewUsersActivity.this, "Pola nie mogą być puste", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isUpdated = dbHelper.updateUser(userId, newName, newEmail);
                    if (isUpdated) {
                        Toast.makeText(ViewUsersActivity.this, "Dane użytkownika zaktualizowane", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Odśwież listę
                    } else {
                        Toast.makeText(ViewUsersActivity.this, "Błąd aktualizacji", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Anuluj", null);
        builder.create().show();
    }

    // Usuwanie użytkownika
    private void deleteUser(int userId, int position) {
        boolean isDeleted = dbHelper.deleteUser(userId);
        if (isDeleted) {
            Toast.makeText(this, "Użytkownik usunięty", Toast.LENGTH_SHORT).show();
            userList.remove(position);  // Usuń z listy
            userIds.remove(position);   // Usuń z ID
            arrayAdapter.notifyDataSetChanged();  // Odśwież widok
        } else {
            Toast.makeText(this, "Błąd przy usuwaniu użytkownika", Toast.LENGTH_SHORT).show();
        }
    }
}
