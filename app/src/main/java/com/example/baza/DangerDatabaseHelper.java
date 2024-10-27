package com.example.baza;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DangerDatabaseHelper extends SQLiteOpenHelper {

    // Nazwa bazy danych i tabela
    private static final String DATABASE_NAME = "harnas.db";
    private static final String TABLE_DANGER = "dangers";

    // Kolumny tabeli
    private static final String DANGER_COL_1 = "ID";
    private static final String DANGER_COL_2 = "TYPE";
    private static final String DANGER_COL_3 = "LOCATION";
    private static final String DANGER_COL_4 = "USER";
    private static final String DANGER_COL_5 = "DESCRIPTION";
    private static final String DANGER_COL_6 = "CREATE_AT";
    private static final String DANGER_COL_7 = "ACCEPTED";
    private static final String TABLE_USER = "users";

    // Kolumny tabeli
    private static final String USER_COL_1 = "ID";
    private static final String USER_COL_2 = "NAME";
    private static final String USER_COL_3 = "EMAIL";

    private static final String TABLE_DISTANCE = "distance";

    // Kolumny tabeli
    private static final String DISTANCE_COL_1 = "ID";
    private static final String DISTANCE_COL_2 = "USER";
    private static final String DISTANCE_COL_3 = "DISTANCE";
    private static final String DISTANCE_COL_4 = "DAY";

    public DangerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableDanger = "CREATE TABLE " + TABLE_DANGER + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TYPE TEXT, LOCATION TEXT, USER TEXT, DESCRIPTION TEXT, CREATE_AT DATETIME, ACCEPTED INTEGER DEFAULT 0)";
        db.execSQL(createTableDanger);
        String createTable = "CREATE TABLE " + TABLE_USER + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, EMAIL TEXT)";
        db.execSQL(createTable);
        // Tworzenie tabeli distances
        String createDistancesTable = "CREATE TABLE " + TABLE_DISTANCE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USER TEXT, DISTANCE TEXT, DAY TEXT)";
        db.execSQL(createDistancesTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DANGER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISTANCE);
        onCreate(db);
    }

    // Dodawanie nowego rekordu
    public boolean addDanger(String type, String location, String description, String user){///}, String createAt, int accepted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DANGER_COL_2, type);
        contentValues.put(DANGER_COL_3, location);
        contentValues.put(DANGER_COL_4, user);
        contentValues.put(DANGER_COL_5, description);
        contentValues.put(DANGER_COL_6, System.currentTimeMillis());
        contentValues.put(DANGER_COL_7, 0);

        long result = db.insert(TABLE_DANGER, null, contentValues);
        return result != -1;  // Zwróć true, jeśli dodanie rekordu było udane
    }

    public boolean addUser(String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COL_2, name);
        contentValues.put(USER_COL_3, email);
        long result = db.insert(TABLE_USER, null, contentValues);
        return result != -1; // Zwróć true jeśli dane zostały dodane
    }

    public boolean addDistance(String user, String distance, String day){///}, String createAt, int accepted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DISTANCE_COL_2, user);
        contentValues.put(DISTANCE_COL_3, distance);
        contentValues.put(DISTANCE_COL_4, day);

        long result = db.insert(TABLE_DISTANCE, null, contentValues);
        return result != -1;  // Zwróć true, jeśli dodanie rekordu było udane
    }

    // Pobieranie wszystkich rekordów
    public Cursor getAllDangers() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DANGER, null);
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USER, null);
    }

    public Cursor getAllDistances() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DISTANCE, null);
    }


    // Aktualizacja istniejącego rekordu
    public boolean updateDanger(int id, int accepted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        /*contentValues.put(DANGER_COL_2, type);
        contentValues.put(DANGER_COL_3, location);
        contentValues.put(DANGER_COL_4, user);
        contentValues.put(DANGER_COL_5, description);
        contentValues.put(DANGER_COL_6, createAt);*/
        contentValues.put(DANGER_COL_7, accepted);

        int result = db.update(TABLE_DANGER, contentValues, "ID = ?", new String[]{String.valueOf(id)});
        return result > 0;  // Zwróć true, jeśli aktualizacja była udana
    }

    public boolean updateUser(int id, String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COL_2, name);
        contentValues.put(USER_COL_3, email);

        // Aktualizowanie rekordu na podstawie ID
        int result = db.update(TABLE_USER, contentValues, "ID = ?", new String[]{String.valueOf(id)});
        return result > 0; // Zwróć true, jeśli aktualizacja była udana
    }

    // Usuwanie rekordu
    public boolean deleteDanger(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DANGER, "ID = ?", new String[]{String.valueOf(id)});
        return result > 0;  // Zwróć true, jeśli usunięcie było udane
    }
    public boolean deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USER, "ID = ?", new String[]{String.valueOf(id)});
        return result > 0; // Zwróć true, jeśli usunięcie było udane
    }

    public boolean checkUser(String name, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + USER_COL_2 + "=? AND " + USER_COL_3 + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{name, email});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}
