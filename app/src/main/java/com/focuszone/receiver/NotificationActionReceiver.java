package com.focuszone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.focuszone.data.repository.SessionRepository;
import com.focuszone.service.TimerService;
import com.focuszone.ui.settings.SettingsViewModel;
import com.focuszone.utils.Constants;
import com.focuszone.utils.NotificationHelper;
import com.focuszone.utils.PreferenceManager;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        if (Constants.ACTION_DAILY_REMINDER.equals(action)) {
            handleDailyReminder(context);
            return;
        }

        if (!Constants.ACTION_PAUSE_TIMER.equals(action)
                && !Constants.ACTION_RESUME_TIMER.equals(action)
                && !Constants.ACTION_SKIP_TIMER.equals(action)
                && !Constants.ACTION_START_BREAK.equals(action)) {
            return;
        }

        Intent serviceIntent = new Intent(context, TimerService.class);
        serviceIntent.setAction(action);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    private void handleDailyReminder(Context context) {
        BroadcastReceiver.PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();
        SessionRepository repository = SessionRepository.getInstance(appContext);
        PreferenceManager preferences = new PreferenceManager(appContext);
        repository.getTodaySummary(summary -> {
            if (summary.getTodaySessions() == 0) {
                new NotificationHelper(appContext).showDailyReminderNotification();
            }
            if (preferences.isDailyReminderEnabled()) {
                SettingsViewModel.scheduleDailyReminder(appContext, preferences);
            }
            pendingResult.finish();
        });
    }
}
