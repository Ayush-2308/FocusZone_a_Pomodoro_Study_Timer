package com.focuszone.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.focuszone.R;

public class PreferenceManager {

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public int getFocusMinutes() {
        return preferences.getInt(Constants.PREF_FOCUS_MINUTES, Constants.DEFAULT_FOCUS_MINUTES);
    }

    public void setFocusMinutes(int value) {
        putInt(Constants.PREF_FOCUS_MINUTES, value);
    }

    public int getShortBreakMinutes() {
        return preferences.getInt(Constants.PREF_SHORT_BREAK_MINUTES, Constants.DEFAULT_SHORT_BREAK_MINUTES);
    }

    public void setShortBreakMinutes(int value) {
        putInt(Constants.PREF_SHORT_BREAK_MINUTES, value);
    }

    public int getLongBreakMinutes() {
        return preferences.getInt(Constants.PREF_LONG_BREAK_MINUTES, Constants.DEFAULT_LONG_BREAK_MINUTES);
    }

    public void setLongBreakMinutes(int value) {
        putInt(Constants.PREF_LONG_BREAK_MINUTES, value);
    }

    public int getLongBreakInterval() {
        return preferences.getInt(Constants.PREF_LONG_BREAK_INTERVAL, Constants.DEFAULT_LONG_BREAK_INTERVAL);
    }

    public void setLongBreakInterval(int value) {
        putInt(Constants.PREF_LONG_BREAK_INTERVAL, value);
    }

    public boolean isSessionEndNotificationEnabled() {
        return preferences.getBoolean(Constants.PREF_SESSION_END_NOTIFICATION, true);
    }

    public void setSessionEndNotificationEnabled(boolean value) {
        putBoolean(Constants.PREF_SESSION_END_NOTIFICATION, value);
    }

    public boolean isBreakReminderEnabled() {
        return preferences.getBoolean(Constants.PREF_BREAK_REMINDER, true);
    }

    public void setBreakReminderEnabled(boolean value) {
        putBoolean(Constants.PREF_BREAK_REMINDER, value);
    }

    public boolean isDailyReminderEnabled() {
        return preferences.getBoolean(Constants.PREF_DAILY_REMINDER, false);
    }

    public void setDailyReminderEnabled(boolean value) {
        putBoolean(Constants.PREF_DAILY_REMINDER, value);
    }

    public int getDailyReminderHour() {
        return preferences.getInt(Constants.PREF_DAILY_REMINDER_HOUR, Constants.DEFAULT_DAILY_REMINDER_HOUR);
    }

    public int getDailyReminderMinute() {
        return preferences.getInt(Constants.PREF_DAILY_REMINDER_MINUTE, Constants.DEFAULT_DAILY_REMINDER_MINUTE);
    }

    public void setDailyReminderTime(int hour, int minute) {
        preferences.edit()
                .putInt(Constants.PREF_DAILY_REMINDER_HOUR, hour)
                .putInt(Constants.PREF_DAILY_REMINDER_MINUTE, minute)
                .apply();
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(Constants.PREF_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean value) {
        putBoolean(Constants.PREF_VIBRATION, value);
    }

    public boolean isSoundEnabled() {
        return preferences.getBoolean(Constants.PREF_SOUND, true);
    }

    public void setSoundEnabled(boolean value) {
        putBoolean(Constants.PREF_SOUND, value);
    }

    public int getSelectedSound() {
        return preferences.getInt(Constants.PREF_SELECTED_SOUND, 0);
    }

    public void setSelectedSound(int value) {
        putInt(Constants.PREF_SELECTED_SOUND, value);
    }

    public int getSelectedSoundRawRes() {
        int selected = getSelectedSound();
        if (selected == 1) {
            return R.raw.sound_digital;
        }
        if (selected == 2) {
            return R.raw.sound_chime;
        }
        return R.raw.sound_bell;
    }

    public int getVolumePercent() {
        return preferences.getInt(Constants.PREF_VOLUME_PERCENT, Constants.DEFAULT_VOLUME_PERCENT);
    }

    public void setVolumePercent(int value) {
        putInt(Constants.PREF_VOLUME_PERCENT, value);
    }

    public boolean isKeepScreenOnEnabled() {
        return preferences.getBoolean(Constants.PREF_KEEP_SCREEN_ON, false);
    }

    public void setKeepScreenOnEnabled(boolean value) {
        putBoolean(Constants.PREF_KEEP_SCREEN_ON, value);
    }

    public boolean isQuotesEnabled() {
        return preferences.getBoolean(Constants.PREF_QUOTES, true);
    }

    public void setQuotesEnabled(boolean value) {
        putBoolean(Constants.PREF_QUOTES, value);
    }

    public boolean isSessionCounterEnabled() {
        return preferences.getBoolean(Constants.PREF_SESSION_COUNTER, true);
    }

    public void setSessionCounterEnabled(boolean value) {
        putBoolean(Constants.PREF_SESSION_COUNTER, value);
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
}
