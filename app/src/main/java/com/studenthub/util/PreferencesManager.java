package com.studenthub.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton class to manage app preferences.
 * Handles user settings like dark mode, notification preferences, location settings, etc.
 */
public class PreferencesManager {
    
    private static final String PREFS_NAME = "studenthub_prefs";
    
    // Preference Keys
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_CLASS_REMINDER_MINUTES = "class_reminder_minutes";
    private static final String KEY_DUE_TOMORROW_REMINDER = "due_tomorrow_reminder";
    private static final String KEY_DUE_HOUR_REMINDER = "due_hour_reminder";
    private static final String KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled";
    private static final String KEY_QUIET_HOURS_START = "quiet_hours_start";
    private static final String KEY_QUIET_HOURS_END = "quiet_hours_end";
    private static final String KEY_HOME_LAT = "home_latitude";
    private static final String KEY_HOME_LNG = "home_longitude";
    private static final String KEY_HOME_RADIUS = "home_radius";
    private static final String KEY_CAMPUS_LAT = "campus_latitude";
    private static final String KEY_CAMPUS_LNG = "campus_longitude";
    private static final String KEY_CAMPUS_RADIUS = "campus_radius";
    private static final String KEY_MANUAL_MODE = "manual_mode";
    private static final String KEY_CURRENT_MODE = "current_mode";
    private static final String KEY_LAST_SYNC = "last_sync";
    private static final String KEY_FOCUS_DURATION = "focus_duration";
    private static final String KEY_BREAK_DURATION = "break_duration";
    private static final String KEY_LONG_BREAK_DURATION = "long_break_duration";
    private static final String KEY_SESSIONS_UNTIL_LONG_BREAK = "sessions_until_long_break";
    
    // Mode Constants
    public static final int MODE_AUTO = 0;
    public static final int MODE_HOME = 1;
    public static final int MODE_CAMPUS = 2;
    
    private static PreferencesManager instance;
    private final SharedPreferences prefs;
    
    private PreferencesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static void init(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
    }
    
    public static PreferencesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PreferencesManager not initialized. Call init() first.");
        }
        return instance;
    }
    
    // Dark Mode
    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
    
    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }
    
    // Class Reminder
    public int getClassReminderMinutes() {
        return prefs.getInt(KEY_CLASS_REMINDER_MINUTES, 15);
    }
    
    public void setClassReminderMinutes(int minutes) {
        prefs.edit().putInt(KEY_CLASS_REMINDER_MINUTES, minutes).apply();
    }
    
    // Assignment Reminders
    public boolean isDueTomorrowReminderEnabled() {
        return prefs.getBoolean(KEY_DUE_TOMORROW_REMINDER, true);
    }
    
    public void setDueTomorrowReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DUE_TOMORROW_REMINDER, enabled).apply();
    }
    
    public boolean isDueHourReminderEnabled() {
        return prefs.getBoolean(KEY_DUE_HOUR_REMINDER, true);
    }
    
    public void setDueHourReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DUE_HOUR_REMINDER, enabled).apply();
    }
    
    // Quiet Hours
    public boolean isQuietHoursEnabled() {
        return prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false);
    }
    
    public void setQuietHoursEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply();
    }
    
    public int getQuietHoursStart() {
        return prefs.getInt(KEY_QUIET_HOURS_START, 22 * 60); // 10:00 PM
    }
    
    public void setQuietHoursStart(int minutesFromMidnight) {
        prefs.edit().putInt(KEY_QUIET_HOURS_START, minutesFromMidnight).apply();
    }
    
    public int getQuietHoursEnd() {
        return prefs.getInt(KEY_QUIET_HOURS_END, 7 * 60); // 7:00 AM
    }
    
    public void setQuietHoursEnd(int minutesFromMidnight) {
        prefs.edit().putInt(KEY_QUIET_HOURS_END, minutesFromMidnight).apply();
    }
    
    // Home Location
    public double getHomeLatitude() {
        return Double.longBitsToDouble(prefs.getLong(KEY_HOME_LAT, Double.doubleToLongBits(0)));
    }
    
    public double getHomeLongitude() {
        return Double.longBitsToDouble(prefs.getLong(KEY_HOME_LNG, Double.doubleToLongBits(0)));
    }
    
    public float getHomeRadius() {
        return prefs.getFloat(KEY_HOME_RADIUS, 200f);
    }
    
    public void setHomeLocation(double lat, double lng, float radius) {
        prefs.edit()
            .putLong(KEY_HOME_LAT, Double.doubleToLongBits(lat))
            .putLong(KEY_HOME_LNG, Double.doubleToLongBits(lng))
            .putFloat(KEY_HOME_RADIUS, radius)
            .apply();
    }
    
    public boolean hasHomeLocation() {
        return getHomeLatitude() != 0 || getHomeLongitude() != 0;
    }
    
    // Campus Location
    public double getCampusLatitude() {
        return Double.longBitsToDouble(prefs.getLong(KEY_CAMPUS_LAT, Double.doubleToLongBits(0)));
    }
    
    public double getCampusLongitude() {
        return Double.longBitsToDouble(prefs.getLong(KEY_CAMPUS_LNG, Double.doubleToLongBits(0)));
    }
    
    public float getCampusRadius() {
        return prefs.getFloat(KEY_CAMPUS_RADIUS, 300f);
    }
    
    public void setCampusLocation(double lat, double lng, float radius) {
        prefs.edit()
            .putLong(KEY_CAMPUS_LAT, Double.doubleToLongBits(lat))
            .putLong(KEY_CAMPUS_LNG, Double.doubleToLongBits(lng))
            .putFloat(KEY_CAMPUS_RADIUS, radius)
            .apply();
    }
    
    public boolean hasCampusLocation() {
        return getCampusLatitude() != 0 || getCampusLongitude() != 0;
    }
    
    // Mode
    public int getManualMode() {
        return prefs.getInt(KEY_MANUAL_MODE, MODE_AUTO);
    }
    
    public void setManualMode(int mode) {
        prefs.edit().putInt(KEY_MANUAL_MODE, mode).apply();
    }
    
    public int getCurrentMode() {
        return prefs.getInt(KEY_CURRENT_MODE, MODE_HOME);
    }
    
    public void setCurrentMode(int mode) {
        prefs.edit().putInt(KEY_CURRENT_MODE, mode).apply();
    }
    
    // Sync
    public long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }
    
    public void setLastSyncTime(long timestamp) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply();
    }
    
    // Focus Mode Settings
    public int getFocusDuration() {
        return prefs.getInt(KEY_FOCUS_DURATION, 25);
    }
    
    public void setFocusDuration(int minutes) {
        prefs.edit().putInt(KEY_FOCUS_DURATION, minutes).apply();
    }
    
    public int getBreakDuration() {
        return prefs.getInt(KEY_BREAK_DURATION, 5);
    }
    
    public void setBreakDuration(int minutes) {
        prefs.edit().putInt(KEY_BREAK_DURATION, minutes).apply();
    }
    
    public int getLongBreakDuration() {
        return prefs.getInt(KEY_LONG_BREAK_DURATION, 15);
    }
    
    public void setLongBreakDuration(int minutes) {
        prefs.edit().putInt(KEY_LONG_BREAK_DURATION, minutes).apply();
    }
    
    public int getSessionsUntilLongBreak() {
        return prefs.getInt(KEY_SESSIONS_UNTIL_LONG_BREAK, 4);
    }
    
    public void setSessionsUntilLongBreak(int sessions) {
        prefs.edit().putInt(KEY_SESSIONS_UNTIL_LONG_BREAK, sessions).apply();
    }
    
    // Clear all preferences
    public void clear() {
        prefs.edit().clear().apply();
    }
}
