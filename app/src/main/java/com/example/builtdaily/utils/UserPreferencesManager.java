package com.example.builtdaily.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class UserPreferencesManager {
    private static final String PREFS_NAME = "user_state_prefs";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_COMPLETED_DATE = "last_completed_date";
    private static final String KEY_SCHEDULE_PLAN = "schedule_plan";
    private static final String KEY_SCHEDULE_DURATION = "schedule_duration";
    private static final String KEY_PREFERENCE_DURATION = "preference_duration";
    private static final String KEY_PREFERENCE_BEGINNER = "preference_beginner";
    private static final String KEY_PREFERENCE_NO_EQUIPMENT = "preference_no_equipment";
    private static final String ENTRY_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "=";

    private final SharedPreferences preferences;

    public UserPreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getStreak() {
        return preferences.getInt(KEY_STREAK, 0);
    }

    public String getLastCompletedDate() {
        return preferences.getString(KEY_LAST_COMPLETED_DATE, "");
    }

    public void saveWorkoutCompletion(String currentDate) {
        String lastCompletedDate = getLastCompletedDate();
        int streak = getStreak();

        if (!currentDate.equals(lastCompletedDate)) {
            preferences.edit()
                    .putInt(KEY_STREAK, streak + 1)
                    .putString(KEY_LAST_COMPLETED_DATE, currentDate)
                    .apply();
        }
    }

    public boolean hasWorkoutSchedule() {
        return !getScheduleMap().isEmpty();
    }

    public void saveWorkoutSchedule(Map<String, String> scheduleMap, String duration) {
        preferences.edit()
                .putString(KEY_SCHEDULE_PLAN, encodeSchedule(scheduleMap))
                .putString(KEY_SCHEDULE_DURATION, duration)
                .apply();
    }

    public Map<String, String> getScheduleMap() {
        String encoded = preferences.getString(KEY_SCHEDULE_PLAN, "");
        LinkedHashMap<String, String> scheduleMap = new LinkedHashMap<>();

        if (encoded == null || encoded.isEmpty()) {
            return scheduleMap;
        }

        String[] entries = encoded.split("\\|");
        for (String entry : entries) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                scheduleMap.put(parts[0], parts[1]);
            }
        }

        return scheduleMap;
    }

    public String getScheduleDays() {
        return joinKeys(getScheduleMap());
    }

    public String getScheduleFocus() {
        return joinUniqueValues(getScheduleMap());
    }

    public String getScheduleDuration() {
        return preferences.getString(KEY_SCHEDULE_DURATION, "medium");
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
        preferences.edit()
                .putString(KEY_PREFERENCE_DURATION, duration)
                .putBoolean(KEY_PREFERENCE_BEGINNER, beginnerFriendly)
                .putBoolean(KEY_PREFERENCE_NO_EQUIPMENT, noEquipment)
                .apply();
    }

    public String getPreferredDuration() {
        return preferences.getString(KEY_PREFERENCE_DURATION, "medium");
    }

    public boolean isBeginnerFriendlyEnabled() {
        return preferences.getBoolean(KEY_PREFERENCE_BEGINNER, false);
    }

    public boolean isNoEquipmentEnabled() {
        return preferences.getBoolean(KEY_PREFERENCE_NO_EQUIPMENT, false);
    }

    private String encodeSchedule(Map<String, String> scheduleMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : scheduleMap.entrySet()) {
            if (builder.length() > 0) {
                builder.append(ENTRY_SEPARATOR);
            }
            builder.append(entry.getKey()).append(VALUE_SEPARATOR).append(entry.getValue());
        }
        return builder.toString();
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
