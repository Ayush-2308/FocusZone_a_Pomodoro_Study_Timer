package com.focuszone.ui.timer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.focuszone.data.model.SessionSummary;
import com.focuszone.data.model.SessionType;
import com.focuszone.data.model.TimerState;
import com.focuszone.data.repository.SessionRepository;
import com.focuszone.service.TimerService;
import com.focuszone.utils.Constants;
import com.focuszone.utils.Event;
import com.focuszone.utils.PreferenceManager;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TimerViewModel extends AndroidViewModel {

    private final SessionRepository repository;
    private final PreferenceManager preferences;
    private final MediatorLiveData<Long> remainingSeconds = new MediatorLiveData<>();
    private final MediatorLiveData<Long> totalSeconds = new MediatorLiveData<>();
    private final MediatorLiveData<TimerState> state = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> currentSession = new MediatorLiveData<>();
    private final MediatorLiveData<String> currentMode = new MediatorLiveData<>();
    private final MediatorLiveData<SessionSummary> todaySummary = new MediatorLiveData<>();
    private final MutableLiveData<String> quote = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showQuotes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showCounter = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> completionEvent = new MutableLiveData<>();
    private final Random random = new Random();

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener;

    private TimerService timerService;
    private boolean bound;
    private boolean serviceSourcesAttached;
    private TimerState previousState = TimerState.IDLE;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            bound = true;
            attachServiceLiveData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            timerService = null;
        }
    };

    private final List<String> quotes = Arrays.asList(
            "Small steps compound into serious progress.",
            "Your attention is your superpower.",
            "One focused session can change the whole day.",
            "Begin again. That is the craft.",
            "Study now, thank yourself later.",
            "Deep work beats busy work.",
            "Protect this block like it matters.",
            "The next twenty-five minutes are yours.",
            "Consistency turns effort into identity.",
            "Focus is built one return at a time.",
            "Make the next session clean and honest.",
            "You only need to win this interval.",
            "Quiet effort is still powerful effort.",
            "Progress likes a timer.",
            "Show up, settle in, keep going.",
            "Distraction can wait.",
            "A clear mind starts with a clear minute.",
            "Stack one good session on another.",
            "Let the work become simple.",
            "Finish the block. Future you is watching."
    );

    public TimerViewModel(@NonNull Application application) {
        super(application);
        repository = SessionRepository.getInstance(application);
        preferences = new PreferenceManager(application);

        preferenceListener = (prefs, key) -> {
            if (Constants.PREF_QUOTES.equals(key)) {
                boolean enabled = preferences.isQuotesEnabled();
                showQuotes.postValue(enabled);
                if (!enabled) quote.postValue("");
                else if (quote.getValue() == null || quote.getValue().isEmpty()) refreshQuote();
            } else if (Constants.PREF_SESSION_COUNTER.equals(key)) {
                showCounter.postValue(preferences.isSessionCounterEnabled());
            } else if (Constants.PREF_FOCUS_MINUTES.equals(key) || Constants.PREF_SHORT_BREAK_MINUTES.equals(key) || Constants.PREF_LONG_BREAK_MINUTES.equals(key)) {
                if (state.getValue() == TimerState.IDLE) {
                    totalSeconds.postValue(getDurationFromPrefs());
                    remainingSeconds.postValue(getDurationFromPrefs());
                }
            }
        };

        remainingSeconds.setValue(preferences.getLong(
                Constants.PREF_REMAINING_SECONDS,
                preferences.getFocusMinutes() * 60L
        ));
        totalSeconds.setValue(preferences.getLong(
                Constants.PREF_TOTAL_SECONDS,
                preferences.getFocusMinutes() * 60L
        ));
        state.setValue(TimerState.IDLE);
        currentSession.setValue(preferences.getInt(Constants.PREF_CURRENT_SESSION_NUMBER, 1));
        currentMode.setValue(SessionType.FOCUS.getDisplayLabel());
        showQuotes.setValue(preferences.isQuotesEnabled());
        showCounter.setValue(preferences.isSessionCounterEnabled());
        preferences.registerListener(preferenceListener);
        setupSummaryObserver();
        refreshQuote();
    }

    private void setupSummaryObserver() {
        long startOfDay = com.focuszone.utils.TimeUtils.getStartOfDay(System.currentTimeMillis());
        LiveData<Integer> countToday = repository.getFocusCountTodayLiveData(startOfDay);
        LiveData<Integer> secondsToday = repository.getFocusSecondsTodayLiveData(startOfDay);
        LiveData<List<Long>> allTimestamps = repository.getAllFocusTimestampsLiveData();

        todaySummary.addSource(countToday, count -> updateSummary(count, secondsToday.getValue(), allTimestamps.getValue()));
        todaySummary.addSource(secondsToday, seconds -> updateSummary(countToday.getValue(), seconds, allTimestamps.getValue()));
        todaySummary.addSource(allTimestamps, timestamps -> updateSummary(countToday.getValue(), secondsToday.getValue(), timestamps));
    }

    private void updateSummary(Integer count, Integer seconds, List<Long> timestamps) {
        int sessions = count != null ? count : 0;
        int minutes = seconds != null ? seconds / 60 : 0;
        int streak = calculateStreak(timestamps);
        todaySummary.setValue(new SessionSummary(sessions, minutes, streak));
    }

    private int calculateStreak(List<Long> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) return 0;
        java.util.TreeSet<Long> activeDays = new java.util.TreeSet<>();
        for (Long ts : timestamps) activeDays.add(com.focuszone.utils.TimeUtils.getStartOfDay(ts));
        long cursor = com.focuszone.utils.TimeUtils.getStartOfDay(System.currentTimeMillis());
        if (!activeDays.contains(cursor)) cursor = com.focuszone.utils.TimeUtils.addDays(cursor, -1);
        int streak = 0;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = com.focuszone.utils.TimeUtils.addDays(cursor, -1);
        }
        return streak;
    }

    private long getDurationFromPrefs() {
        if (timerService != null && timerService.getCurrentType().getValue() != null) {
            SessionType type = timerService.getCurrentType().getValue();
            if (type == SessionType.SHORT_BREAK) return preferences.getShortBreakMinutes() * 60L;
            if (type == SessionType.LONG_BREAK) return preferences.getLongBreakMinutes() * 60L;
        }
        return preferences.getFocusMinutes() * 60L;
    }

    public void bindTimerService(Context context) {
        Context appContext = context.getApplicationContext();
        Intent intent = new Intent(appContext, TimerService.class);
        intent.setAction(Constants.ACTION_RESTORE_TIMER);
        appContext.startService(intent);
        appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindTimerService(Context context) {
        if (!bound) {
            return;
        }
        context.getApplicationContext().unbindService(serviceConnection);
        bound = false;
    }

    public LiveData<Long> getRemainingSeconds() {
        return remainingSeconds;
    }

    public LiveData<Long> getTotalSeconds() {
        return totalSeconds;
    }

    public LiveData<TimerState> getState() {
        return state;
    }

    public LiveData<Integer> getCurrentSession() {
        return currentSession;
    }

    public LiveData<String> getCurrentMode() {
        return currentMode;
    }

    public LiveData<SessionSummary> getTodaySummary() {
        return todaySummary;
    }

    public LiveData<String> getQuote() {
        return quote;
    }

    public LiveData<Boolean> getShowQuotes() {
        return showQuotes;
    }

    public LiveData<Boolean> getShowCounter() {
        return showCounter;
    }

    public LiveData<Event<String>> getCompletionEvent() {
        return completionEvent;
    }

    public int getLongBreakInterval() {
        return preferences.getLongBreakInterval();
    }

    public void startTimer() {
        if (timerService != null) {
            if (timerService.getCurrentType().getValue() == SessionType.FOCUS) {
                refreshQuote();
            }
            timerService.startTimer();
        } else {
            sendServiceAction(Constants.ACTION_START_TIMER);
        }
    }

    public void pauseTimer() {
        if (timerService != null) {
            timerService.pauseTimer();
        } else {
            sendServiceAction(Constants.ACTION_PAUSE_TIMER);
        }
    }

    public void resumeTimer() {
        if (timerService != null) {
            timerService.resumeTimer();
        } else {
            sendServiceAction(Constants.ACTION_RESUME_TIMER);
        }
    }

    public void resetTimer() {
        if (timerService != null) {
            timerService.resetTimer();
        } else {
            sendServiceAction(Constants.ACTION_RESET_TIMER);
        }
    }

    public void skipSession() {
        if (timerService != null) {
            timerService.skipSession();
        } else {
            sendServiceAction(Constants.ACTION_SKIP_TIMER);
        }
    }

    @Override
    protected void onCleared() {
        preferences.unregisterListener(preferenceListener);
        timerService = null;
        super.onCleared();
    }

    private void attachServiceLiveData() {
        if (timerService == null) {
            return;
        }
        if (serviceSourcesAttached) {
            totalSeconds.setValue(timerService.getTotalSeconds());
            return;
        }
        serviceSourcesAttached = true;

        remainingSeconds.addSource(timerService.getRemainingSeconds(), remainingSeconds::setValue);
        state.addSource(timerService.getTimerState(), newState -> {
            state.setValue(newState);
            if (previousState == TimerState.RUNNING && newState == TimerState.COMPLETED) {
                completionEvent.setValue(new Event<>("Session complete. Great work!"));
            }
            previousState = newState;
        });
        currentSession.addSource(timerService.getCurrentSessionNumber(), currentSession::setValue);
        currentMode.addSource(timerService.getCurrentType(), type -> {
            currentMode.setValue(type.getDisplayLabel());
            totalSeconds.setValue(timerService.getTotalSeconds());
            if (type == SessionType.FOCUS && previousState == TimerState.IDLE) {
                refreshQuote();
            }
        });
        totalSeconds.setValue(timerService.getTotalSeconds());
    }

    private void refreshQuote() {
        if (!preferences.isQuotesEnabled()) {
            quote.postValue("");
            return;
        }
        quote.postValue(quotes.get(random.nextInt(quotes.size())));
    }

    private void sendServiceAction(String action) {
        Intent intent = new Intent(getApplication(), TimerService.class);
        intent.setAction(action);
        getApplication().startService(intent);
    }
}
