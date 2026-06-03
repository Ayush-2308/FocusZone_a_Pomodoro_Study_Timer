package com.focuszone.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.focuszone.data.db.SessionEntity;
import com.focuszone.data.model.SessionType;
import com.focuszone.data.model.TimerState;
import com.focuszone.data.repository.SessionRepository;
import com.focuszone.utils.Constants;
import com.focuszone.utils.NotificationHelper;
import com.focuszone.utils.PreferenceManager;

public class TimerService extends Service {

    private static final long TICK_INTERVAL_MILLIS = 1000L;

    private final IBinder binder = new TimerBinder();
    private final MutableLiveData<Long> remainingSeconds = new MutableLiveData<>(0L);
    private final MutableLiveData<TimerState> timerState = new MutableLiveData<>(TimerState.IDLE);
    private final MutableLiveData<SessionType> currentType = new MutableLiveData<>(SessionType.FOCUS);
    private final MutableLiveData<Integer> currentSessionNumber = new MutableLiveData<>(1);

    private CountDownTimer countDownTimer;
    private SessionRepository repository;
    private PreferenceManager preferences;
    private NotificationHelper notificationHelper;
    private PowerManager.WakeLock wakeLock;

    private long totalSeconds;
    private long remaining;
    private SessionType activeType = SessionType.FOCUS;
    private TimerState activeState = TimerState.IDLE;
    private int sessionNumber = 1;
    private int completedCyclesTotal = 0;
    private boolean breakWarningSent = false;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = SessionRepository.getInstance(this);
        preferences = new PreferenceManager(this);
        notificationHelper = new NotificationHelper(this);
        restoreState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? Constants.ACTION_RESTORE_TIMER : intent.getAction();
        if (action == null) {
            action = Constants.ACTION_RESTORE_TIMER;
        }

        switch (action) {
            case Constants.ACTION_START_TIMER:
                startTimer();
                break;
            case Constants.ACTION_PAUSE_TIMER:
                pauseTimer();
                break;
            case Constants.ACTION_RESUME_TIMER:
                resumeTimer();
                break;
            case Constants.ACTION_RESET_TIMER:
                resetTimer();
                break;
            case Constants.ACTION_SKIP_TIMER:
                skipSession();
                break;
            case Constants.ACTION_START_BREAK:
                startBreakFromNotification();
                break;
            case Constants.ACTION_RESTORE_TIMER:
            default:
                restoreRunningTimerIfNeeded();
                break;
        }

        if (activeState == TimerState.RUNNING || activeState == TimerState.PAUSED) {
            updateForegroundNotification();
        } else {
            stopForeground(true);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        cancelCountDownTimer();
        releaseWakeLock();
        super.onDestroy();
    }

    public LiveData<Long> getRemainingSeconds() {
        return remainingSeconds;
    }

    public LiveData<TimerState> getTimerState() {
        return timerState;
    }

    public LiveData<SessionType> getCurrentType() {
        return currentType;
    }

    public LiveData<Integer> getCurrentSessionNumber() {
        return currentSessionNumber;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public int getCompletedCyclesTotal() {
        return completedCyclesTotal;
    }

    public void startTimer() {
        if (activeState == TimerState.RUNNING) {
            return;
        }

        if (remaining <= 0 || activeState == TimerState.IDLE || activeState == TimerState.COMPLETED) {
            totalSeconds = getDurationForType(activeType);
            remaining = totalSeconds;
        }

        breakWarningSent = false;
        setState(TimerState.RUNNING);
        acquireWakeLockIfNeeded();
        persistState();
        startCountDown(remaining);
    }

    public void pauseTimer() {
        if (activeState != TimerState.RUNNING) {
            return;
        }

        cancelCountDownTimer();
        releaseWakeLock();
        setState(TimerState.PAUSED);
        persistState();
        updateForegroundNotification();
    }

    public void resumeTimer() {
        if (activeState != TimerState.PAUSED) {
            return;
        }

        setState(TimerState.RUNNING);
        acquireWakeLockIfNeeded();
        persistState();
        startCountDown(remaining);
    }

    public void resetTimer() {
        cancelCountDownTimer();
        releaseWakeLock();
        totalSeconds = getDurationForType(activeType);
        remaining = totalSeconds;
        breakWarningSent = false;
        postRemaining();
        setState(TimerState.IDLE);
        persistState();
        stopForeground(true);
    }

    public void skipSession() {
        cancelCountDownTimer();
        releaseWakeLock();
        advanceToNextMode(false);
        setState(TimerState.IDLE);
        totalSeconds = getDurationForType(activeType);
        remaining = totalSeconds;
        breakWarningSent = false;
        postAllState();
        persistState();
        stopForeground(true);
    }

    public void setCurrentMode(SessionType type) {
        activeType = type;
        totalSeconds = getDurationForType(activeType);
        remaining = totalSeconds;
        breakWarningSent = false;
        postAllState();
        persistState();
    }

    private void startCountDown(long seconds) {
        cancelCountDownTimer();
        countDownTimer = new CountDownTimer(seconds * 1000L, TICK_INTERVAL_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                remaining = Math.max(0L, (long) Math.ceil(millisUntilFinished / 1000.0));
                handleBreakWarning();
                postRemaining();
                persistState();
                updateForegroundNotification();
            }

            @Override
            public void onFinish() {
                remaining = 0L;
                postRemaining();
                completeSession();
            }
        };
        countDownTimer.start();
        updateForegroundNotification();
    }

    private void completeSession() {
        cancelCountDownTimer();
        releaseWakeLock();
        setState(TimerState.COMPLETED);
        SessionType completedType = activeType;

        SessionEntity session = new SessionEntity(
                completedType.getDatabaseValue(),
                (int) totalSeconds,
                System.currentTimeMillis(),
                false,
                completedCyclesTotal + 1,
                sessionNumber
        );

        advanceToNextMode(true);
        SessionType nextType = activeType;

        repository.insertSession(session, saved -> repository.getTodaySummary(summary ->
                notificationHelper.showSessionCompleteNotification(completedType, nextType, summary.getTodaySessions())
        ));

        playCompletionFeedback();
        totalSeconds = getDurationForType(activeType);
        remaining = totalSeconds;
        breakWarningSent = false;
        postAllState();
        setState(TimerState.IDLE);
        persistState();
        stopForeground(true);
    }

    private void advanceToNextMode(boolean completed) {
        if (activeType == SessionType.FOCUS) {
            int longBreakInterval = preferences.getLongBreakInterval();
            if (sessionNumber >= longBreakInterval) {
                activeType = SessionType.LONG_BREAK;
            } else {
                activeType = SessionType.SHORT_BREAK;
            }
            return;
        }

        if (activeType == SessionType.LONG_BREAK && completed) {
            completedCyclesTotal++;
            sessionNumber = 1;
        } else if (activeType == SessionType.SHORT_BREAK) {
            sessionNumber = Math.min(preferences.getLongBreakInterval(), sessionNumber + 1);
        } else if (activeType == SessionType.LONG_BREAK) {
            sessionNumber = 1;
        }

        activeType = SessionType.FOCUS;
    }

    private void startBreakFromNotification() {
        if (activeType == SessionType.FOCUS) {
            activeType = sessionNumber >= preferences.getLongBreakInterval()
                    ? SessionType.LONG_BREAK
                    : SessionType.SHORT_BREAK;
        }
        totalSeconds = getDurationForType(activeType);
        remaining = totalSeconds;
        startTimer();
    }

    private long getDurationForType(SessionType type) {
        if (type == SessionType.SHORT_BREAK) {
            return preferences.getShortBreakMinutes() * 60L;
        }
        if (type == SessionType.LONG_BREAK) {
            return preferences.getLongBreakMinutes() * 60L;
        }
        return preferences.getFocusMinutes() * 60L;
    }

    private void restoreState() {
        activeType = SessionType.fromDatabaseValue(preferences.getString(
                Constants.PREF_CURRENT_TYPE,
                SessionType.FOCUS.getDatabaseValue()
        ));
        activeState = TimerState.valueOf(preferences.getString(
                Constants.PREF_TIMER_STATE,
                TimerState.IDLE.name()
        ));
        totalSeconds = preferences.getLong(Constants.PREF_TOTAL_SECONDS, getDurationForType(activeType));
        remaining = preferences.getLong(Constants.PREF_REMAINING_SECONDS, totalSeconds);
        sessionNumber = preferences.getInt(Constants.PREF_CURRENT_SESSION_NUMBER, 1);
        completedCyclesTotal = preferences.getInt(Constants.PREF_COMPLETED_CYCLES_TOTAL, 0);

        if (activeState == TimerState.RUNNING) {
            long lastTick = preferences.getLong(Constants.PREF_LAST_TICK_MILLIS, System.currentTimeMillis());
            long elapsed = Math.max(0L, (System.currentTimeMillis() - lastTick) / 1000L);
            remaining = Math.max(0L, remaining - elapsed);
            if (remaining == 0L) {
                completeSession();
                return;
            }
        }

        postAllState();
    }

    private void restoreRunningTimerIfNeeded() {
        if (activeState == TimerState.RUNNING && remaining > 0) {
            acquireWakeLockIfNeeded();
            startCountDown(remaining);
        }
    }

    private void persistState() {
        preferences.putString(Constants.PREF_CURRENT_TYPE, activeType.getDatabaseValue());
        preferences.putString(Constants.PREF_TIMER_STATE, activeState.name());
        preferences.putLong(Constants.PREF_TOTAL_SECONDS, totalSeconds);
        preferences.putLong(Constants.PREF_REMAINING_SECONDS, remaining);
        preferences.putLong(Constants.PREF_LAST_TICK_MILLIS, System.currentTimeMillis());
        preferences.putInt(Constants.PREF_CURRENT_SESSION_NUMBER, sessionNumber);
        preferences.putInt(Constants.PREF_COMPLETED_CYCLES_TOTAL, completedCyclesTotal);
    }

    private void postAllState() {
        currentType.postValue(activeType);
        currentSessionNumber.postValue(sessionNumber);
        timerState.postValue(activeState);
        remainingSeconds.postValue(remaining);
    }

    private void postRemaining() {
        remainingSeconds.postValue(remaining);
    }

    private void setState(TimerState state) {
        activeState = state;
        timerState.postValue(state);
    }

    private void updateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Constants.TIMER_NOTIFICATION_ID, notificationHelper.buildTimerNotification(
                    activeType,
                    remaining,
                    activeState
            ), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(Constants.TIMER_NOTIFICATION_ID, notificationHelper.buildTimerNotification(
                    activeType,
                    remaining,
                    activeState
            ));
        }
    }

    private void cancelCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void handleBreakWarning() {
        if (breakWarningSent || !activeType.isBreak() || remaining != 60L) {
            return;
        }
        if (preferences.isBreakReminderEnabled()) {
            notificationHelper.showBreakEndingWarning();
        }
        breakWarningSent = true;
    }

    private void playCompletionFeedback() {
        if (preferences.isVibrationEnabled()) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200L);
                }
            }
        }

        if (!preferences.isSoundEnabled()) {
            return;
        }

        int soundRes = preferences.getSelectedSoundRawRes();
        MediaPlayer player = MediaPlayer.create(this, soundRes);
        if (player == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
        }
        float volume = preferences.getVolumePercent() / 100f;
        player.setVolume(volume, volume);
        player.setOnCompletionListener(MediaPlayer::release);
        player.start();
    }

    private void acquireWakeLockIfNeeded() {
        if (!preferences.isKeepScreenOnEnabled() || activeType != SessionType.FOCUS) {
            return;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            return;
        }
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return;
        }
        wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                Constants.WAKE_LOCK_TAG
        );
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(getDurationForType(SessionType.FOCUS) * 1000L);
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;
    }

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
}
