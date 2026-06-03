package com.focuszone.data.model;

public class AllTimeStats {

    private final int totalSessions;
    private final double totalFocusHours;
    private final int longestStreak;
    private final String bestDayLabel;
    private final int bestDaySessions;
    private final double averageSessionsPerDay;
    private final int totalCyclesCompleted;
    private final String mostProductiveHour;
    private final int totalFocusSeconds;
    private final int totalBreakSeconds;

    public AllTimeStats(int totalSessions,
                        double totalFocusHours,
                        int longestStreak,
                        String bestDayLabel,
                        int bestDaySessions,
                        double averageSessionsPerDay,
                        int totalCyclesCompleted,
                        String mostProductiveHour,
                        int totalFocusSeconds,
                        int totalBreakSeconds) {
        this.totalSessions = totalSessions;
        this.totalFocusHours = totalFocusHours;
        this.longestStreak = longestStreak;
        this.bestDayLabel = bestDayLabel;
        this.bestDaySessions = bestDaySessions;
        this.averageSessionsPerDay = averageSessionsPerDay;
        this.totalCyclesCompleted = totalCyclesCompleted;
        this.mostProductiveHour = mostProductiveHour;
        this.totalFocusSeconds = totalFocusSeconds;
        this.totalBreakSeconds = totalBreakSeconds;
    }

    public static AllTimeStats empty() {
        return new AllTimeStats(0, 0.0, 0, "-", 0, 0.0, 0, "-", 0, 0);
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public double getTotalFocusHours() {
        return totalFocusHours;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public String getBestDayLabel() {
        return bestDayLabel;
    }

    public int getBestDaySessions() {
        return bestDaySessions;
    }

    public double getAverageSessionsPerDay() {
        return averageSessionsPerDay;
    }

    public int getTotalCyclesCompleted() {
        return totalCyclesCompleted;
    }

    public String getMostProductiveHour() {
        return mostProductiveHour;
    }

    public int getTotalFocusSeconds() {
        return totalFocusSeconds;
    }

    public int getTotalBreakSeconds() {
        return totalBreakSeconds;
    }

    public int getFocusPercentage() {
        int total = totalFocusSeconds + totalBreakSeconds;
        if (total == 0) {
            return 0;
        }
        return Math.round((totalFocusSeconds * 100f) / total);
    }
}
