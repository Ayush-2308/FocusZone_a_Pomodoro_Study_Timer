package com.focuszone.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.lifecycle.LiveData;

import com.focuszone.data.db.AppDatabase;
import com.focuszone.data.db.SessionDao;
import com.focuszone.data.db.SessionEntity;
import com.focuszone.data.model.AllTimeStats;
import com.focuszone.data.model.SessionSummary;
import com.focuszone.data.model.SessionType;
import com.focuszone.utils.TimeUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SessionRepository {

    public interface Callback<T> {
        void onComplete(T result);
    }

    private static volatile SessionRepository instance;

    private final SessionDao sessionDao;
    private final ExecutorService executorService;

    private SessionRepository(Context context) {
        sessionDao = AppDatabase.getInstance(context).sessionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static SessionRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionRepository.class) {
                if (instance == null) {
                    instance = new SessionRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public LiveData<List<SessionEntity>> getAllSessions() {
        return sessionDao.getAllSessions();
    }

    public LiveData<List<SessionEntity>> getFocusSessions() {
        return sessionDao.getFocusSessions();
    }

    public LiveData<List<SessionEntity>> getBreakSessions() {
        return sessionDao.getBreakSessions();
    }

    public LiveData<List<SessionEntity>> getSessionsToday(long startOfDay) {
        return sessionDao.getSessionsToday(startOfDay);
    }

    public LiveData<Integer> getFocusCountTodayLiveData(long startOfDay) {
        return sessionDao.getFocusCountTodayLiveData(startOfDay);
    }

    public LiveData<Integer> getFocusSecondsTodayLiveData(long startOfDay) {
        return sessionDao.getFocusSecondsTodayLiveData(startOfDay);
    }

    public LiveData<List<Long>> getAllFocusTimestampsLiveData() {
        return sessionDao.getAllFocusTimestampsLiveData();
    }

    public void insertSession(SessionEntity session) {
        executorService.execute(() -> sessionDao.insertSession(session));
    }

    public void insertSession(SessionEntity session, Callback<Boolean> callback) {
        executorService.execute(() -> {
            sessionDao.insertSession(session);
            if (callback != null) {
                callback.onComplete(true);
            }
        });
    }

    public void deleteSession(SessionEntity session) {
        executorService.execute(() -> sessionDao.deleteSession(session));
    }

    public void deleteAllSessions() {
        executorService.execute(sessionDao::deleteAllSessions);
    }

    public void getTodaySummary(Callback<SessionSummary> callback) {
        executorService.execute(() -> {
            long startOfDay = TimeUtils.getStartOfDay(System.currentTimeMillis());
            int sessions = sessionDao.getFocusCountToday(startOfDay);
            int minutes = sessionDao.getFocusSecondsToday(startOfDay) / 60;
            int streak = calculateCurrentStreakBlocking();
            callback.onComplete(new SessionSummary(sessions, minutes, streak));
        });
    }

    public void calculateCurrentStreak(Callback<Integer> callback) {
        executorService.execute(() -> callback.onComplete(calculateCurrentStreakBlocking()));
    }

    public int calculateCurrentStreakBlocking() {
        List<Long> timestamps = sessionDao.getAllFocusTimestamps();
        if (timestamps.isEmpty()) {
            return 0;
        }

        TreeSet<Long> activeDays = new TreeSet<>();
        for (Long timestamp : timestamps) {
            activeDays.add(TimeUtils.getStartOfDay(timestamp));
        }

        long cursor = TimeUtils.getStartOfDay(System.currentTimeMillis());
        if (!activeDays.contains(cursor)) {
            cursor = TimeUtils.addDays(cursor, -1);
        }

        int streak = 0;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = TimeUtils.addDays(cursor, -1);
        }
        return streak;
    }

    public void calculateLongestStreak(Callback<Integer> callback) {
        executorService.execute(() -> callback.onComplete(calculateLongestStreakBlocking()));
    }

    public int calculateLongestStreakBlocking() {
        List<Long> timestamps = sessionDao.getAllFocusTimestamps();
        if (timestamps.isEmpty()) {
            return 0;
        }

        TreeSet<Long> days = new TreeSet<>();
        for (Long timestamp : timestamps) {
            days.add(TimeUtils.getStartOfDay(timestamp));
        }

        int longest = 0;
        int current = 0;
        long previous = Long.MIN_VALUE;
        for (Long day : days) {
            if (previous != Long.MIN_VALUE && day == TimeUtils.addDays(previous, 1)) {
                current++;
            } else {
                current = 1;
            }
            longest = Math.max(longest, current);
            previous = day;
        }
        return longest;
    }

    public void getSessionsGroupedByDate(Callback<Map<String, List<SessionEntity>>> callback) {
        executorService.execute(() -> {
            List<SessionEntity> sessions = sessionDao.getAllSessionsBlocking();
            callback.onComplete(groupByDate(sessions));
        });
    }

    public void getAllTimeStats(Callback<AllTimeStats> callback) {
        executorService.execute(() -> callback.onComplete(buildAllTimeStatsBlocking()));
    }

    public AllTimeStats buildAllTimeStatsBlocking() {
        List<SessionEntity> sessions = sessionDao.getAllSessionsBlocking();
        int totalSessions = sessionDao.getTotalCompletedSessions();
        int focusSeconds = sessionDao.getTotalFocusSeconds();
        int breakSeconds = sessionDao.getTotalBreakSeconds();
        int longestStreak = calculateLongestStreakBlocking();
        int totalActiveDays = Math.max(1, sessionDao.getTotalActiveDays());
        int totalCycles = calculateCompletedCycles(sessions);

        Map<Long, Integer> countsByDay = new HashMap<>();
        Map<Integer, Integer> countsByHour = new HashMap<>();
        for (SessionEntity session : sessions) {
            if (!SessionType.FOCUS.getDatabaseValue().equals(session.getType()) || session.isWasSkipped()) {
                continue;
            }
            long day = TimeUtils.getStartOfDay(session.getCompletedAt());
            countsByDay.put(day, countsByDay.getOrDefault(day, 0) + 1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(session.getCompletedAt());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            countsByHour.put(hour, countsByHour.getOrDefault(hour, 0) + 1);
        }

        long bestDay = 0L;
        int bestDaySessions = 0;
        for (Map.Entry<Long, Integer> entry : countsByDay.entrySet()) {
            if (entry.getValue() > bestDaySessions) {
                bestDaySessions = entry.getValue();
                bestDay = entry.getKey();
            }
        }

        int bestHour = -1;
        int bestHourCount = 0;
        for (Map.Entry<Integer, Integer> entry : countsByHour.entrySet()) {
            if (entry.getValue() > bestHourCount) {
                bestHour = entry.getKey();
                bestHourCount = entry.getValue();
            }
        }

        String bestDayLabel = bestDay == 0L ? "-" : TimeUtils.formatDate(bestDay);
        String productiveHour = bestHour < 0 ? "-" : TimeUtils.formatHourRange(bestHour);
        double average = sessionDao.getTotalFocusSessions() / (double) totalActiveDays;

        return new AllTimeStats(
                totalSessions,
                Math.round((focusSeconds / 3600.0) * 10.0) / 10.0,
                longestStreak,
                bestDayLabel,
                bestDaySessions,
                Math.round(average * 10.0) / 10.0,
                totalCycles,
                productiveHour,
                focusSeconds,
                breakSeconds
        );
    }

    public void getFocusCountsForLastSevenDays(Callback<LinkedHashMap<String, Integer>> callback) {
        executorService.execute(() -> {
            LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
            long today = TimeUtils.getStartOfDay(System.currentTimeMillis());
            for (int i = 6; i >= 0; i--) {
                long start = TimeUtils.addDays(today, -i);
                long end = TimeUtils.addDays(start, 1) - 1;
                result.put(TimeUtils.formatShortDay(start), sessionDao.getFocusCountBetween(start, end));
            }
            callback.onComplete(result);
        });
    }

    public void getFocusMinutesForCurrentWeek(Callback<LinkedHashMap<String, Integer>> callback) {
        executorService.execute(() -> {
            LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
            long weekStart = TimeUtils.getStartOfWeek(System.currentTimeMillis());
            for (int i = 0; i < 7; i++) {
                long start = TimeUtils.addDays(weekStart, i);
                long end = TimeUtils.addDays(start, 1) - 1;
                result.put(TimeUtils.formatShortDay(start), sessionDao.getFocusSecondsBetween(start, end) / 60);
            }
            callback.onComplete(result);
        });
    }

    public void getMonthlyHeatmapData(Callback<Map<Integer, Integer>> callback) {
        executorService.execute(() -> {
            Map<Integer, Integer> data = new HashMap<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            long monthStart = TimeUtils.getStartOfDay(calendar.getTimeInMillis());
            calendar.add(Calendar.MONTH, 1);
            long monthEnd = TimeUtils.getStartOfDay(calendar.getTimeInMillis()) - 1;

            List<SessionEntity> sessions = sessionDao.getSessionsBetween(monthStart, monthEnd);
            Calendar itemCalendar = Calendar.getInstance();
            for (SessionEntity session : sessions) {
                if (!SessionType.FOCUS.getDatabaseValue().equals(session.getType()) || session.isWasSkipped()) {
                    continue;
                }
                itemCalendar.setTimeInMillis(session.getCompletedAt());
                int day = itemCalendar.get(Calendar.DAY_OF_MONTH);
                data.put(day, data.getOrDefault(day, 0) + 1);
            }
            callback.onComplete(data);
        });
    }

    public void exportSessionsToCSV(Context context, Callback<File> callback) {
        executorService.execute(() -> {
            List<SessionEntity> sessions = sessionDao.getAllSessionsBlocking();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(System.currentTimeMillis());
            String fileName = "focuszone_history_" + timestamp + ".csv";
            String csvData = buildCsvData(sessions);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (java.io.OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                        if (os != null) {
                            os.write(csvData.getBytes());
                            callback.onComplete(new File(fileName)); // Returning a dummy file with name for UI
                            return;
                        }
                    } catch (IOException e) {
                        // fallback or fail
                    }
                }
            }

            // Legacy way for older Android or if MediaStore failed
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloads.exists() && !downloads.mkdirs()) {
                callback.onComplete(null);
                return;
            }

            File file = new File(downloads, fileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csvData);
                callback.onComplete(file);
            } catch (IOException e) {
                callback.onComplete(null);
            }
        });
    }

    private String buildCsvData(List<SessionEntity> sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,durationSeconds,completedAt,completedAtReadable,wasSkipped,cycleNumber,sessionNumberInCycle\n");
        for (SessionEntity session : sessions) {
            sb.append(session.getId()).append(",");
            sb.append(escapeCsv(session.getType())).append(",");
            sb.append(session.getDurationSeconds()).append(",");
            sb.append(session.getCompletedAt()).append(",");
            sb.append(escapeCsv(TimeUtils.formatDateTime(session.getCompletedAt()))).append(",");
            sb.append(session.isWasSkipped()).append(",");
            sb.append(session.getCycleNumber()).append(",");
            sb.append(session.getSessionNumberInCycle()).append("\n");
        }
        return sb.toString();
    }

    public Future<?> execute(Runnable runnable) {
        return executorService.submit(runnable);
    }

    private Map<String, List<SessionEntity>> groupByDate(List<SessionEntity> sessions) {
        Map<String, List<SessionEntity>> grouped = new LinkedHashMap<>();
        for (SessionEntity session : sessions) {
            String label = TimeUtils.formatRelativeDateHeader(session.getCompletedAt());
            List<SessionEntity> list = grouped.get(label);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(label, list);
            }
            list.add(session);
        }
        return grouped;
    }

    private int calculateCompletedCycles(List<SessionEntity> sessions) {
        Map<Integer, Integer> focusByCycle = new HashMap<>();
        for (SessionEntity session : sessions) {
            if (SessionType.FOCUS.getDatabaseValue().equals(session.getType()) && !session.isWasSkipped()) {
                int cycle = session.getCycleNumber();
                focusByCycle.put(cycle, focusByCycle.getOrDefault(cycle, 0) + 1);
            }
        }

        int completed = 0;
        for (Integer count : focusByCycle.values()) {
            if (count >= 4) {
                completed++;
            }
        }
        return completed;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
