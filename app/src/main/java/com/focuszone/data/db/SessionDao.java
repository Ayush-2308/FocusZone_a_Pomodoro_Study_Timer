package com.focuszone.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(SessionEntity session);

    @Delete
    void deleteSession(SessionEntity session);

    @Query("DELETE FROM sessions")
    void deleteAllSessions();

    @Query("SELECT * FROM sessions ORDER BY completedAt DESC")
    LiveData<List<SessionEntity>> getAllSessions();

    @Query("SELECT * FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 ORDER BY completedAt DESC")
    LiveData<List<SessionEntity>> getFocusSessions();

    @Query("SELECT * FROM sessions WHERE type IN ('SHORT_BREAK', 'LONG_BREAK') AND wasSkipped = 0 ORDER BY completedAt DESC")
    LiveData<List<SessionEntity>> getBreakSessions();

    @Query("SELECT * FROM sessions WHERE completedAt >= :startOfDay AND wasSkipped = 0 ORDER BY completedAt DESC")
    LiveData<List<SessionEntity>> getSessionsToday(long startOfDay);

    @Query("SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= :startOfDay")
    int getFocusCountToday(long startOfDay);

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= :startOfDay")
    int getFocusSecondsToday(long startOfDay);

    @Query("SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= :startOfDay")
    LiveData<Integer> getFocusCountTodayLiveData(long startOfDay);

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= :startOfDay")
    LiveData<Integer> getFocusSecondsTodayLiveData(long startOfDay);

    @Query("SELECT completedAt FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 ORDER BY completedAt ASC")
    LiveData<List<Long>> getAllFocusTimestampsLiveData();

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0")
    int getTotalFocusSeconds();

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type IN ('SHORT_BREAK', 'LONG_BREAK') AND wasSkipped = 0")
    int getTotalBreakSeconds();

    @Query("SELECT COUNT(*) FROM sessions WHERE wasSkipped = 0")
    int getTotalCompletedSessions();

    @Query("SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0")
    int getTotalFocusSessions();

    @Query("SELECT COUNT(DISTINCT date(completedAt/1000, 'unixepoch', 'localtime')) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0")
    int getTotalActiveDays();

    @Query("SELECT completedAt FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 ORDER BY completedAt ASC")
    List<Long> getAllFocusTimestamps();

    @Query("SELECT * FROM sessions WHERE wasSkipped = 0 ORDER BY completedAt DESC")
    List<SessionEntity> getAllSessionsBlocking();

    @Query("SELECT * FROM sessions WHERE completedAt BETWEEN :startMillis AND :endMillis AND wasSkipped = 0 ORDER BY completedAt ASC")
    List<SessionEntity> getSessionsBetween(long startMillis, long endMillis);

    @Query("SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt BETWEEN :startMillis AND :endMillis")
    int getFocusCountBetween(long startMillis, long endMillis);

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt BETWEEN :startMillis AND :endMillis")
    int getFocusSecondsBetween(long startMillis, long endMillis);

    @Query("SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND cycleNumber = :cycleNumber")
    int getFocusCountForCycle(int cycleNumber);
}
