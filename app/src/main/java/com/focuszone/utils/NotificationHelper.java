package com.focuszone.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.focuszone.MainActivity;
import com.focuszone.R;
import com.focuszone.data.model.SessionType;
import com.focuszone.data.model.TimerState;
import com.focuszone.receiver.NotificationActionReceiver;

public class NotificationHelper {

    private final Context context;
    private final PreferenceManager preferences;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = new PreferenceManager(this.context);
    }

    public NotificationCompat.Builder buildTimerNotificationBuilder(SessionType type,
                                                                    long remainingSeconds,
                                                                    TimerState state) {
        boolean paused = state == TimerState.PAUSED;
        String pauseResumeAction = paused ? Constants.ACTION_RESUME_TIMER : Constants.ACTION_PAUSE_TIMER;
        String pauseResumeLabel = paused ? context.getString(R.string.resume) : context.getString(R.string.pause);
        int pauseResumeIcon = paused ? R.drawable.ic_play : R.drawable.ic_pause;

        return new NotificationCompat.Builder(context, Constants.TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(context.getString(R.string.timer_notification_title, type.getDisplayLabel()))
                .setContentText(context.getString(R.string.remaining_format, TimeUtils.formatSeconds(remainingSeconds)))
                .setContentIntent(openTimerPendingIntent())
                .setOngoing(state == TimerState.RUNNING)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(pauseResumeIcon, pauseResumeLabel, actionPendingIntent(pauseResumeAction, 3101))
                .addAction(R.drawable.ic_skip, context.getString(R.string.skip), actionPendingIntent(Constants.ACTION_SKIP_TIMER, 3102));
    }

    public android.app.Notification buildTimerNotification(SessionType type,
                                                           long remainingSeconds,
                                                           TimerState state) {
        return buildTimerNotificationBuilder(type, remainingSeconds, state).build();
    }

    public void showSessionCompleteNotification(SessionType completedType, SessionType nextType, int sessionsToday) {
        if (!preferences.isSessionEndNotificationEnabled() || !canPostNotifications()) {
            return;
        }

        String title = completedType == SessionType.FOCUS
                ? context.getString(R.string.focus_complete_title)
                : context.getString(R.string.break_complete_title);
        
        int nextDuration = nextType == SessionType.SHORT_BREAK 
                ? preferences.getShortBreakMinutes() 
                : preferences.getLongBreakMinutes();

        String body = completedType == SessionType.FOCUS
                ? context.getString(R.string.focus_complete_body, nextDuration, sessionsToday)
                : context.getString(R.string.break_complete_body);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(openTimerPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (nextType.isBreak()) {
            builder.addAction(R.drawable.ic_play, context.getString(R.string.start_break), actionPendingIntent(Constants.ACTION_START_BREAK, 3103));
        }

        NotificationManagerCompat.from(context).notify(Constants.SESSION_COMPLETE_NOTIFICATION_ID, builder.build());
    }

    public void showBreakEndingWarning() {
        if (!canPostNotifications()) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(context.getString(R.string.break_ending_title))
                .setContentText(context.getString(R.string.break_ending_body))
                .setContentIntent(openTimerPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(Constants.BREAK_WARNING_NOTIFICATION_ID, builder.build());
    }

    public void showDailyReminderNotification() {
        if (!canPostNotifications()) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(context.getString(R.string.daily_reminder_title))
                .setContentText(context.getString(R.string.daily_reminder_body))
                .setContentIntent(openTimerPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(Constants.DAILY_REMINDER_NOTIFICATION_ID, builder.build());
    }

    public void showExportCompleteNotification(String fileName) {
        if (!canPostNotifications()) {
            return;
        }

        Intent intent = new Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 4001, intent, pendingIntentFlags());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("History exported successfully")
                .setContentText("File saved in Downloads: " + fileName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_link, "Open Downloads", pendingIntent);

        NotificationManagerCompat.from(context).notify(Constants.EXPORT_NOTIFICATION_ID, builder.build());
    }

    private PendingIntent openTimerPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.ACTION_OPEN_TIMER);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 3001, intent, pendingIntentFlags());
    }

    private PendingIntent actionPendingIntent(String action, int requestCode) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, requestCode, intent, pendingIntentFlags());
    }

    private int pendingIntentFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }

    private boolean canPostNotifications() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
