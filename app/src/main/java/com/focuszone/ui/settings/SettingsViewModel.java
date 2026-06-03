package com.focuszone.ui.settings;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.focuszone.data.repository.SessionRepository;
import com.focuszone.receiver.NotificationActionReceiver;
import com.focuszone.utils.Constants;
import com.focuszone.utils.Event;
import com.focuszone.utils.NotificationHelper;
import com.focuszone.utils.PreferenceManager;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsViewModel extends AndroidViewModel {

    private final PreferenceManager preferences;
    private final SessionRepository repository;

    private final MutableLiveData<Integer> focusMinutes = new MutableLiveData<>();
    private final MutableLiveData<Integer> shortBreakMinutes = new MutableLiveData<>();
    private final MutableLiveData<Integer> longBreakMinutes = new MutableLiveData<>();
    private final MutableLiveData<Integer> longBreakInterval = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sessionEndNotificationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> breakReminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dailyReminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<Integer> dailyReminderHour = new MutableLiveData<>();
    private final MutableLiveData<Integer> dailyReminderMinute = new MutableLiveData<>();
    private final MutableLiveData<String> dailyReminderTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> vibrationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> soundEnabled = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedSound = new MutableLiveData<>();
    private final MutableLiveData<Integer> volumePercent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> keepScreenOnEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> quotesEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> counterEnabled = new MutableLiveData<>();
    private final MutableLiveData<Event<File>> exportedFile = new MutableLiveData<>();
    private final MediatorLiveData<Integer> totalStoredSessions = new MediatorLiveData<>();
    private final NotificationHelper notificationHelper;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        preferences = new PreferenceManager(application);
        repository = SessionRepository.getInstance(application);
        notificationHelper = new NotificationHelper(application);
        loadSettings();
        observeSessionCount();
    }

    public LiveData<Integer> getFocusMinutes() {
        return focusMinutes;
    }

    public LiveData<Integer> getShortBreakMinutes() {
        return shortBreakMinutes;
    }

    public LiveData<Integer> getLongBreakMinutes() {
        return longBreakMinutes;
    }

    public MutableLiveData<Integer> getLongBreakInterval() {
        return longBreakInterval;
    }

    public LiveData<Boolean> getSessionEndNotificationEnabled() {
        return sessionEndNotificationEnabled;
    }

    public LiveData<Boolean> getBreakReminderEnabled() {
        return breakReminderEnabled;
    }

    public LiveData<Boolean> getDailyReminderEnabled() {
        return dailyReminderEnabled;
    }

    public MutableLiveData<Integer> getDailyReminderHour() {
        return dailyReminderHour;
    }

    public MutableLiveData<Integer> getDailyReminderMinute() {
        return dailyReminderMinute;
    }

    public LiveData<String> getDailyReminderTime() {
        return dailyReminderTime;
    }

    public LiveData<Boolean> getVibrationEnabled() {
        return vibrationEnabled;
    }

    public LiveData<Boolean> getSoundEnabled() {
        return soundEnabled;
    }

    public LiveData<Integer> getSelectedSound() {
        return selectedSound;
    }

    public LiveData<Integer> getVolumePercent() {
        return volumePercent;
    }

    public LiveData<Boolean> getKeepScreenOnEnabled() {
        return keepScreenOnEnabled;
    }

    public LiveData<Boolean> getQuotesEnabled() {
        return quotesEnabled;
    }

    public LiveData<Boolean> getSessionCounterEnabled() {
        return counterEnabled;
    }

    public LiveData<Event<File>> getExportedFile() {
        return exportedFile;
    }

    public LiveData<Integer> getTotalStoredSessions() {
        return totalStoredSessions;
    }

    public void setFocusMinutes(int value) {
        value = clamp(value, 5, 60);
        preferences.setFocusMinutes(value);
        focusMinutes.setValue(value);
    }

    public void setShortBreakMinutes(int value) {
        value = clamp(value, 1, 15);
        preferences.setShortBreakMinutes(value);
        shortBreakMinutes.setValue(value);
    }

    public void setLongBreakMinutes(int value) {
        value = clamp(value, 5, 30);
        preferences.setLongBreakMinutes(value);
        longBreakMinutes.setValue(value);
    }

    public void setLongBreakInterval(int value) {
        value = clamp(value, 2, 6);
        preferences.setLongBreakInterval(value);
        longBreakInterval.setValue(value);
    }

    public void setSessionEndNotificationEnabled(boolean value) {
        preferences.setSessionEndNotificationEnabled(value);
        sessionEndNotificationEnabled.setValue(value);
    }

    public void setBreakReminderEnabled(boolean value) {
        preferences.setBreakReminderEnabled(value);
        breakReminderEnabled.setValue(value);
    }

    public void setDailyReminderEnabled(boolean value) {
        preferences.setDailyReminderEnabled(value);
        dailyReminderEnabled.setValue(value);
        scheduleOrCancelDailyReminder();
    }

    public void setDailyReminderTime(int hour, int minute) {
        preferences.setDailyReminderTime(hour, minute);
        dailyReminderHour.setValue(hour);
        dailyReminderMinute.setValue(minute);
        dailyReminderTime.setValue(formatReminderTime(hour, minute));
        scheduleOrCancelDailyReminder();
    }

    public void setVibrationEnabled(boolean value) {
        preferences.setVibrationEnabled(value);
        vibrationEnabled.setValue(value);
    }

    public void setSoundEnabled(boolean value) {
        preferences.setSoundEnabled(value);
        soundEnabled.setValue(value);
    }

    public void setSelectedSound(int value) {
        value = clamp(value, 0, 2);
        preferences.setSelectedSound(value);
        selectedSound.setValue(value);
    }

    public void setVolumePercent(int value) {
        value = clamp(value, 0, 100);
        preferences.setVolumePercent(value);
        volumePercent.setValue(value);
    }

    public void setKeepScreenOnEnabled(boolean value) {
        preferences.setKeepScreenOnEnabled(value);
        keepScreenOnEnabled.setValue(value);
    }

    public void setQuotesEnabled(boolean value) {
        preferences.setQuotesEnabled(value);
        quotesEnabled.setValue(value);
    }

    public void setSessionCounterEnabled(boolean value) {
        preferences.setSessionCounterEnabled(value);
        counterEnabled.setValue(value);
    }

    public void exportHistory(Context context) {
        repository.exportSessionsToCSV(context.getApplicationContext(), file -> {
            if (file != null) {
                notificationHelper.showExportCompleteNotification(file.getName());
            }
            exportedFile.postValue(new Event<>(file));
        });
    }

    public void clearHistory() {
        repository.deleteAllSessions();
        totalStoredSessions.setValue(0);
    }

    public void scheduleOrCancelDailyReminder() {
        if (preferences.isDailyReminderEnabled()) {
            scheduleDailyReminder(getApplication(), preferences);
        } else {
            cancelDailyReminder(getApplication());
        }
    }

    public static void scheduleDailyReminder(Context context, PreferenceManager preferences) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, preferences.getDailyReminderHour());
        calendar.set(Calendar.MINUTE, preferences.getDailyReminderMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        PendingIntent pendingIntent = buildDailyReminderPendingIntent(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(buildDailyReminderPendingIntent(context));
        }
    }

    private static PendingIntent buildDailyReminderPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(Constants.ACTION_DAILY_REMINDER);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, Constants.DAILY_REMINDER_REQUEST_CODE, intent, flags);
    }

    private void loadSettings() {
        focusMinutes.setValue(preferences.getFocusMinutes());
        shortBreakMinutes.setValue(preferences.getShortBreakMinutes());
        longBreakMinutes.setValue(preferences.getLongBreakMinutes());
        longBreakInterval.setValue(preferences.getLongBreakInterval());
        sessionEndNotificationEnabled.setValue(preferences.isSessionEndNotificationEnabled());
        breakReminderEnabled.setValue(preferences.isBreakReminderEnabled());
        dailyReminderEnabled.setValue(preferences.isDailyReminderEnabled());
        dailyReminderHour.setValue(preferences.getDailyReminderHour());
        dailyReminderMinute.setValue(preferences.getDailyReminderMinute());
        dailyReminderTime.setValue(formatReminderTime(preferences.getDailyReminderHour(), preferences.getDailyReminderMinute()));
        vibrationEnabled.setValue(preferences.isVibrationEnabled());
        soundEnabled.setValue(preferences.isSoundEnabled());
        selectedSound.setValue(preferences.getSelectedSound());
        volumePercent.setValue(preferences.getVolumePercent());
        keepScreenOnEnabled.setValue(preferences.isKeepScreenOnEnabled());
        quotesEnabled.setValue(preferences.isQuotesEnabled());
        counterEnabled.setValue(preferences.isSessionCounterEnabled());
    }

    private void observeSessionCount() {
        totalStoredSessions.addSource(repository.getAllSessions(), sessions ->
                totalStoredSessions.postValue(sessions == null ? 0 : sessions.size())
        );
    }

    private String formatReminderTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(calendar.getTime());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
