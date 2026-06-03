package com.focuszone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.focuszone.service.TimerService;
import com.focuszone.ui.settings.SettingsViewModel;
import com.focuszone.utils.Constants;
import com.focuszone.utils.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        boolean shouldRestore = Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action);

        if (!shouldRestore) {
            return;
        }

        PreferenceManager preferences = new PreferenceManager(context);
        if (preferences.isDailyReminderEnabled()) {
            SettingsViewModel.scheduleDailyReminder(context, preferences);
        }

        String state = preferences.getString(Constants.PREF_TIMER_STATE, "IDLE");
        if ("RUNNING".equals(state) || "PAUSED".equals(state)) {
            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.setAction(Constants.ACTION_RESTORE_TIMER);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
