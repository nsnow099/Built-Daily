package com.example.builtdaily.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AuthManager {
    private final DatabaseHelper dbHelper;

    public AuthManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // SIGNUP
    public boolean signup(String email, String password) {
        email = normalizeEmail(email);
        password = normalizePassword(password);

        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );
        if (cursor.moveToFirst()) {
            cursor.close();
            return false; // user exists
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EMAIL, email);
        values.put(DatabaseHelper.COL_PASSWORD, password);

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean login(String email, String password) {
        email = normalizeEmail(email);
        password = normalizePassword(password);

        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_EMAIL + "=? AND " + DatabaseHelper.COL_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null
        );

        boolean success = cursor.moveToFirst();
        cursor.close();
        return success;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizePassword(String password) {
        return password == null ? "" : password.trim();
    }
}
