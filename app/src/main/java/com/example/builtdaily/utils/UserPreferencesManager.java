package com.example.builtdaily.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class UserPreferencesManager {
    private final SQLiteDatabase db;
    private final int userId;

    public UserPreferencesManager(Context context, int userId) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
        this.userId = userId;
    }

    public int getStreak() {
        Cursor c = db.rawQuery(
                "SELECT streak FROM user_state WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        int streak = 0;
        if (c.moveToFirst()) {
            streak = c.getInt(0);
        }
        c.close();
        return streak;
    }

    public String getLastCompletedDate() {
        Cursor c = db.rawQuery(
                "SELECT last_completed_date FROM user_state WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        String date = "";
        if (c.moveToFirst()) {
            date = c.getString(0);
        }
        c.close();
        return date;
    }

    public void saveWorkoutCompletion(String currentDate) {
        String lastDate = getLastCompletedDate();
        int streak = getStreak();
        if (currentDate.equals(lastDate)) return;
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("streak", streak + 1);
        v.put("last_completed_date", currentDate);
        db.insertWithOnConflict("user_state", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean hasWorkoutSchedule() {
        return !getScheduleMap().isEmpty();
    }

    public void saveWorkoutSchedule(Map<String, String> scheduleMap) {
        db.delete("schedule", "user_id=?", new String[]{String.valueOf(userId)});
        for (Map.Entry<String, String> e : scheduleMap.entrySet()) {
            ContentValues v = new ContentValues();
            v.put("user_id", userId);
            v.put("day_key", e.getKey());
            v.put("focus", e.getValue());
            db.insert("schedule", null, v);
        }
    }

    public Map<String, String> getScheduleMap() {
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        Cursor c = db.rawQuery(
                "SELECT day_key, focus FROM schedule WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getString(1));
        }
        c.close();
        return map;
    }

    public String getScheduleDays() {
        return joinKeys(getScheduleMap());
    }

    public String getScheduleFocus() {
        return joinUniqueValues(getScheduleMap());
    }

    public String getScheduleDuration() {
        Cursor c = db.rawQuery(
                "SELECT duration FROM video_preferences WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        String duration = "medium";
        if (c.moveToFirst()) duration = c.getString(0);
        c.close();
        return duration;
    }

    public String getScheduleSummary() {
        Map<String, String> scheduleMap = getScheduleMap();
        if (scheduleMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : scheduleMap.entrySet()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(getDisplayDayName(entry.getKey())).append(": ").append(entry.getValue());
        }
        return builder.toString();
    }

    public String getWorkoutFocusForTodayOrFirst() {
        Map<String, String> scheduleMap = getScheduleMap();
        if (scheduleMap.isEmpty()) {
            return "";
        }
        String todayKey = getTodayKey();
        if (scheduleMap.containsKey(todayKey)) {
            return scheduleMap.get(todayKey);
        }
        return scheduleMap.values().iterator().next();
    }

    public void saveVideoPreferences(String duration, boolean beginnerFriendly, boolean noEquipment) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("duration", duration);
        v.put("beginner", beginnerFriendly ? 1 : 0);
        v.put("no_equipment", noEquipment ? 1 : 0);
        db.insertWithOnConflict("video_preferences", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getPreferredDuration() {
        Cursor c = db.rawQuery(
                "SELECT duration FROM video_preferences WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        String duration = "medium";
        if (c.moveToFirst()) duration = c.getString(0);
        c.close();
        return duration;
    }

    public boolean isBeginnerFriendlyEnabled() {
        Cursor c = db.rawQuery(
                "SELECT beginner FROM video_preferences WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        boolean result = false;
        if (c.moveToFirst()) result = c.getInt(0) == 1;
        c.close();
        return result;
    }

    public boolean isNoEquipmentEnabled() {
        Cursor c = db.rawQuery(
                "SELECT no_equipment FROM video_preferences WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        boolean result = false;
        if (c.moveToFirst()) result = c.getInt(0) == 1;
        c.close();
        return result;
    }

    private String joinKeys(Map<String, String> scheduleMap) {
        StringBuilder builder = new StringBuilder();
        for (String key : scheduleMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(key);
        }
        return builder.toString();
    }

    private String joinUniqueValues(Map<String, String> scheduleMap) {
        Set<String> values = new LinkedHashSet<>(scheduleMap.values());
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private String getTodayKey() {
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return "Mon";
            case Calendar.TUESDAY:
                return "Tue";
            case Calendar.WEDNESDAY:
                return "Wed";
            case Calendar.THURSDAY:
                return "Thu";
            case Calendar.FRIDAY:
                return "Fri";
            case Calendar.SATURDAY:
                return "Sat";
            case Calendar.SUNDAY:
            default:
                return "Sun";
        }
    }

    private String getDisplayDayName(String dayKey) {
        switch (dayKey) {
            case "Mon":
                return "Monday";
            case "Tue":
                return "Tuesday";
            case "Wed":
                return "Wednesday";
            case "Thu":
                return "Thursday";
            case "Fri":
                return "Friday";
            case "Sat":
                return "Saturday";
            case "Sun":
            default:
                return "Sunday";
        }
    }
}