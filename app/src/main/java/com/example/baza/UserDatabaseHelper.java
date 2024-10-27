package com.example.baza;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Nazwa bazy danych i tabela
    private static final String DATABASE_NAME = "harnas.db";
    private static final String TABLE_NAME = "users";

    // Kolumny tabeli
    private static final String COL_1 = "ID";
    private static final String COL_2 = "NAME";
    private static final String COL_3 = "EMAIL";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, EMAIL TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Dodawanie użytkownika
    public boolean addUser(String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, email);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // Zwróć true jeśli dane zostały dodane
    }

    // Pobieranie wszystkich użytkowników
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
            // Metoda do edytowania użytkownika
        public boolean updateUser(int id, String name, String email) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_2, name);
            contentValues.put(COL_3, email);

            // Aktualizowanie rekordu na podstawie ID
            int result = db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{String.valueOf(id)});
            return result > 0; // Zwróć true, jeśli aktualizacja była udana
        }

        // Metoda do usuwania użytkownika
        public boolean deleteUser(int id) {
            SQLiteDatabase db = this.getWritableDatabase();
            int result = db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
            return result > 0; // Zwróć true, jeśli usunięcie było udane
        }
    }
