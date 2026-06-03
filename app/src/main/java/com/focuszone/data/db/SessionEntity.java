package com.focuszone.data.db;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sessions",
        indices = {
                @Index("completedAt"),
                @Index(value = {"type", "wasSkipped"}),
                @Index("cycleNumber")
        }
)
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String type;
    private int durationSeconds;
    private long completedAt;
    private boolean wasSkipped;
    private int cycleNumber;
    private int sessionNumberInCycle;

    public SessionEntity(String type,
                         int durationSeconds,
                         long completedAt,
                         boolean wasSkipped,
                         int cycleNumber,
                         int sessionNumberInCycle) {
        this.type = type;
        this.durationSeconds = durationSeconds;
        this.completedAt = completedAt;
        this.wasSkipped = wasSkipped;
        this.cycleNumber = cycleNumber;
        this.sessionNumberInCycle = sessionNumberInCycle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isWasSkipped() {
        return wasSkipped;
    }

    public void setWasSkipped(boolean wasSkipped) {
        this.wasSkipped = wasSkipped;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(int cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public int getSessionNumberInCycle() {
        return sessionNumberInCycle;
    }

    public void setSessionNumberInCycle(int sessionNumberInCycle) {
        this.sessionNumberInCycle = sessionNumberInCycle;
    }
}
