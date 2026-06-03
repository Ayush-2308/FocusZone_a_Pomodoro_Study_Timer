package com.focuszone.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static String formatSeconds(long seconds) {
        long safeSeconds = Math.max(0L, seconds);
        long minutes = safeSeconds / 60L;
        long remainder = safeSeconds % 60L;
        return String.format(Locale.US, "%02d:%02d", minutes, remainder);
    }

    public static long getStartOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getStartOfWeek(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long addDays(long startOfDayMillis, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfDayMillis);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return getStartOfDay(calendar.getTimeInMillis());
    }

    public static String formatRelativeDateHeader(long millis) {
        long today = getStartOfDay(System.currentTimeMillis());
        long target = getStartOfDay(millis);
        if (target == today) {
            return "Today";
        }
        if (target == addDays(today, -1)) {
            return "Yesterday";
        }
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(millis);
    }

    public static String formatShortDay(long millis) {
        return new SimpleDateFormat("EEE", Locale.getDefault()).format(millis);
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(millis);
    }

    public static String formatDateTime(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(millis);
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(millis);
    }

    public static String formatHourRange(int hourOfDay) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, hourOfDay);
        start.set(Calendar.MINUTE, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.HOUR_OF_DAY, 1);

        SimpleDateFormat formatter = new SimpleDateFormat("h a", Locale.getDefault());
        return formatter.format(start.getTime()) + " - " + formatter.format(end.getTime());
    }

    public static int getDaysInCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getCurrentDayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
}
