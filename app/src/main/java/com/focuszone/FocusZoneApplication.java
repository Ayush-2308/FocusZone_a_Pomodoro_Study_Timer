package com.focuszone;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.focuszone.utils.Constants;

public class FocusZoneApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel timerChannel = new NotificationChannel(
                Constants.TIMER_CHANNEL_ID,
                "Active timer",
                NotificationManager.IMPORTANCE_LOW
        );
        timerChannel.setDescription("Persistent FocusZone timer controls and remaining time.");
        timerChannel.setShowBadge(false);
        timerChannel.enableVibration(false);
        timerChannel.setSound(null, null);
        timerChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

        NotificationChannel reminderChannel = new NotificationChannel(
                Constants.REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        reminderChannel.setDescription("Session completion alerts, break warnings, and daily focus reminders.");
        reminderChannel.enableVibration(true);
        reminderChannel.setVibrationPattern(new long[]{0, 200, 120, 200});

        Uri defaultSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound_bell);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        reminderChannel.setSound(defaultSound, audioAttributes);
        reminderChannel.setLightColor(ContextCompat.getColor(this, R.color.focus_orange));
        reminderChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

        manager.createNotificationChannel(timerChannel);
        manager.createNotificationChannel(reminderChannel);
    }
}
