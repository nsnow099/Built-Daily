package com.example.builtdaily.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AuthManager {
    // keeping the db helper class to handle table stuff
    private final DatabaseHelper dbHelper;

    public AuthManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // SIGNUP METHOD
    public boolean signup(String email, String password) {
        // clean up the inputs
        email = normalizeEmail(email);
        password = normalizePassword(password);

        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // check if someone already has this email
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );
        if (cursor.moveToFirst()) {
            cursor.close();
            return false; // return false if email exists
        }
        cursor.close();

        // put values in content values to insert
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EMAIL, email);
        values.put(DatabaseHelper.COL_PASSWORD, password);

        // insert the row and return if it worked
        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        return result != -1;
    }

    // LOGIN METHOD
    public boolean login(String email, String password) {
        email = normalizeEmail(email);
        password = normalizePassword(password);

        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }

        // read the database for matching user
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_EMAIL + "=? AND " + DatabaseHelper.COL_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null
        );

        // if move to first is true then we found a match
        boolean success = cursor.moveToFirst();
        cursor.close();
        return success;
    }

    // util to make emails lowercase and trim spaces
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    // trim spaces from password
    private String normalizePassword(String password) {
        return password == null ? "" : password.trim();
    }
}
