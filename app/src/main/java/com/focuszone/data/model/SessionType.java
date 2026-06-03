package com.focuszone.data.model;

import androidx.annotation.ColorInt;

public enum SessionType {
    FOCUS("FOCUS", "Focus", "Focus Session", 0xFFFF6B35),
    SHORT_BREAK("SHORT_BREAK", "Short Break", "Short Break", 0xFF4CAF50),
    LONG_BREAK("LONG_BREAK", "Long Break", "Long Break", 0xFF2196F3);

    private final String databaseValue;
    private final String displayLabel;
    private final String historyLabel;
    private final int color;

    SessionType(String databaseValue, String displayLabel, String historyLabel, @ColorInt int color) {
        this.databaseValue = databaseValue;
        this.displayLabel = displayLabel;
        this.historyLabel = historyLabel;
        this.color = color;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public String getHistoryLabel() {
        return historyLabel;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public boolean isBreak() {
        return this == SHORT_BREAK || this == LONG_BREAK;
    }

    public static SessionType fromDatabaseValue(String value) {
        for (SessionType type : values()) {
            if (type.databaseValue.equals(value)) {
                return type;
            }
        }
        return FOCUS;
    }
}
