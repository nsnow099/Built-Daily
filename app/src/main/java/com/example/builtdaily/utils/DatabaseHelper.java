package com.example.builtdaily.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "builtdaily.db";
    private static final int DB_VERSION = 2;

    // USERS
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // USER INFO
    public static final String TABLE_USER_STATE = "user_state";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_STREAK = "streak";
    public static final String COL_LAST_COMPLETED = "last_completed_date";

    // SCHEDULE
    public static final String TABLE_SCHEDULE = "schedule";
    public static final String COL_DAY_KEY = "day_key";
    public static final String COL_FOCUS = "focus";

    // VIDEO PREFERENCES
    public static final String TABLE_VIDEO_PREFS = "video_preferences";
    public static final String COL_DURATION = "duration";
    public static final String COL_BEGINNER = "beginner";
    public static final String COL_NO_EQUIPMENT = "no_equipment";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_EMAIL + " TEXT UNIQUE, " +
                        COL_PASSWORD + " TEXT)"
        );

        // USER STATE (streaks etc)
        db.execSQL(
                "CREATE TABLE " + TABLE_USER_STATE + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY, " +
                        COL_STREAK + " INTEGER DEFAULT 0, " +
                        COL_LAST_COMPLETED + " TEXT)"
        );

        // WORKOUT SCHEDULE
        db.execSQL(
                "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                        COL_USER_ID + " INTEGER, " +
                        COL_DAY_KEY + " TEXT, " +
                        COL_FOCUS + " TEXT)"
        );

        // VIDEO PREFERENCES
        db.execSQL(
                "CREATE TABLE " + TABLE_VIDEO_PREFS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY, " +
                        COL_DURATION + " TEXT, " +
                        COL_BEGINNER + " INTEGER, " +
                        COL_NO_EQUIPMENT + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEO_PREFS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STATE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}