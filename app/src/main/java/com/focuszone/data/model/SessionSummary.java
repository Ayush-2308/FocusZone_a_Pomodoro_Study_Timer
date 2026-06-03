package com.focuszone.data.model;

public class SessionSummary {

    private final int todaySessions;
    private final int todayMinutes;
    private final int currentStreak;

    public SessionSummary(int todaySessions, int todayMinutes, int currentStreak) {
        this.todaySessions = todaySessions;
        this.todayMinutes = todayMinutes;
        this.currentStreak = currentStreak;
    }

    public int getTodaySessions() {
        return todaySessions;
    }

    public int getTodayMinutes() {
        return todayMinutes;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }
}
