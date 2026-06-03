package com.focuszone.utils;

public final class Constants {

    public static final String TIMER_CHANNEL_ID = "focuszone_timer_channel";
    public static final String REMINDER_CHANNEL_ID = "focuszone_reminder_channel";

    public static final int TIMER_NOTIFICATION_ID = 1001;
    public static final int SESSION_COMPLETE_NOTIFICATION_ID = 1002;
    public static final int BREAK_WARNING_NOTIFICATION_ID = 1003;
    public static final int DAILY_REMINDER_NOTIFICATION_ID = 1004;
    public static final int EXPORT_NOTIFICATION_ID = 1005;
    public static final int DAILY_REMINDER_REQUEST_CODE = 2001;

    public static final String ACTION_OPEN_TIMER = "com.focuszone.action.OPEN_TIMER";
    public static final String ACTION_RESTORE_TIMER = "com.focuszone.action.RESTORE_TIMER";
    public static final String ACTION_START_TIMER = "com.focuszone.action.START_TIMER";
    public static final String ACTION_PAUSE_TIMER = "com.focuszone.action.PAUSE_TIMER";
    public static final String ACTION_RESUME_TIMER = "com.focuszone.action.RESUME_TIMER";
    public static final String ACTION_RESET_TIMER = "com.focuszone.action.RESET_TIMER";
    public static final String ACTION_SKIP_TIMER = "com.focuszone.action.SKIP_TIMER";
    public static final String ACTION_START_BREAK = "com.focuszone.action.START_BREAK";
    public static final String ACTION_DAILY_REMINDER = "com.focuszone.action.DAILY_REMINDER";

    public static final String PREFS_NAME = "focuszone_preferences";
    public static final String PREF_FOCUS_MINUTES = "focus_minutes";
    public static final String PREF_SHORT_BREAK_MINUTES = "short_break_minutes";
    public static final String PREF_LONG_BREAK_MINUTES = "long_break_minutes";
    public static final String PREF_LONG_BREAK_INTERVAL = "long_break_interval";
    public static final String PREF_SESSION_END_NOTIFICATION = "session_end_notification";
    public static final String PREF_BREAK_REMINDER = "break_reminder";
    public static final String PREF_DAILY_REMINDER = "daily_reminder";
    public static final String PREF_DAILY_REMINDER_HOUR = "daily_reminder_hour";
    public static final String PREF_DAILY_REMINDER_MINUTE = "daily_reminder_minute";
    public static final String PREF_VIBRATION = "vibration";
    public static final String PREF_SOUND = "sound";
    public static final String PREF_SELECTED_SOUND = "selected_sound";
    public static final String PREF_VOLUME_PERCENT = "volume_percent";
    public static final String PREF_KEEP_SCREEN_ON = "keep_screen_on";
    public static final String PREF_QUOTES = "quotes";
    public static final String PREF_SESSION_COUNTER = "session_counter";
    public static final String PREF_CURRENT_TYPE = "current_type";
    public static final String PREF_TIMER_STATE = "timer_state";
    public static final String PREF_TOTAL_SECONDS = "total_seconds";
    public static final String PREF_REMAINING_SECONDS = "remaining_seconds";
    public static final String PREF_LAST_TICK_MILLIS = "last_tick_millis";
    public static final String PREF_CURRENT_SESSION_NUMBER = "current_session_number";
    public static final String PREF_COMPLETED_CYCLES_TOTAL = "completed_cycles_total";

    public static final int DEFAULT_FOCUS_MINUTES = 25;
    public static final int DEFAULT_SHORT_BREAK_MINUTES = 5;
    public static final int DEFAULT_LONG_BREAK_MINUTES = 15;
    public static final int DEFAULT_LONG_BREAK_INTERVAL = 4;
    public static final int DEFAULT_DAILY_REMINDER_HOUR = 9;
    public static final int DEFAULT_DAILY_REMINDER_MINUTE = 0;
    public static final int DEFAULT_VOLUME_PERCENT = 80;

    public static final String WAKE_LOCK_TAG = "FocusZone:FocusWakeLock";

    private Constants() {
    }
}
