package com.example.builtdaily.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.builtdaily.models.Video;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserPreferencesManager {
    private final SQLiteDatabase db;
    private final int userId;
    private final Context context;

    public UserPreferencesManager(Context context, int userId) {
        this.context = context;
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
        markNeedsRefresh();
    }

    public Map<String, String> getScheduleMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
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

    public void saveVideoPreferences(String duration, boolean beginnerFriendly, boolean noEquipment) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("duration", duration);
        v.put("beginner", beginnerFriendly ? 1 : 0);
        v.put("no_equipment", noEquipment ? 1 : 0);
        db.insertWithOnConflict("video_preferences", null, v, SQLiteDatabase.CONFLICT_REPLACE);
        markNeedsRefresh();
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

    private void markNeedsRefresh() {
        context.getSharedPreferences("builtdaily_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("needs_refresh_" + userId, true)
                .apply();
    }

    public boolean needsRefresh() { //refresh if needs refresh has been set (settings changed) or it's a new week without fetch done
        boolean forcedRefresh = context.getSharedPreferences("builtdaily_prefs", Context.MODE_PRIVATE)
                .getBoolean("needs_refresh_" + userId, false);
        
        if (forcedRefresh) return true;

        long lastStartOfWeek = getLastStartOfWeek();
        long currentStartOfWeek = getCurrentStartOfWeek();
        
        return lastStartOfWeek != currentStartOfWeek;
    }

    private long getLastStartOfWeek() {
        Cursor c = db.rawQuery(
                "SELECT MAX(start_of_week) FROM selected_videos WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        long start = 0;
        if (c.moveToFirst()) {
            start = c.getLong(0);
        }
        c.close();
        return start;
    }

    private long getCurrentStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public void clearCachedVideos() {
        db.delete("selected_videos", "user_id=?", new String[]{String.valueOf(userId)});
        context.getSharedPreferences("builtdaily_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("needs_refresh_" + userId, false)
                .apply();
    }

    public void saveSelectedVideos(List<Video> videos, String dayKey) {
        long startOfWeek = getCurrentStartOfWeek();
        for (Video v : videos) {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("video_id", v.videoId);
            values.put("title", v.title);
            values.put("video_length", v.duration);
            values.put("thumbnail_url", v.thumbnailUrl);
            values.put("day_of_week", dayKey);
            values.put("start_of_week", startOfWeek);
            db.insert("selected_videos", null, values);
        }
    }

    public List<Video> getVideosForDay(String dayKey) {
        List<Video> videos = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT video_id, title, thumbnail_url, video_length FROM selected_videos WHERE user_id=? AND day_of_week=?",
                new String[]{String.valueOf(userId), dayKey}
        );
        while (c.moveToNext()) {
            Video v = new Video(c.getString(1), c.getString(0), c.getString(2));
            v.duration = c.getString(3);
            videos.add(v);
        }
        c.close();
        return videos;
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