package com.studenthub.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.studenthub.R;
import com.studenthub.StudentHubApp;
import com.studenthub.ui.MainActivity;
import com.studenthub.util.PreferencesManager;

/**
 * Foreground service for running Pomodoro focus timer.
 * Handles focus sessions and breaks with notifications.
 */
public class FocusService extends Service {

    private static final int NOTIFICATION_ID = 1001;

    private final IBinder binder = new FocusBinder();
    private FocusServiceListener listener;

    private CountDownTimer timer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean isBreakTime = false;
    private long timeRemaining = 0;
    private long totalTime = 0;
    private int completedSessions = 0;

    public class FocusBinder extends Binder {
        public FocusService getService() {
            return FocusService.this;
        }
    }

    public interface FocusServiceListener {
        void onTimerTick(long millisRemaining, int progress);
        void onSessionComplete(boolean wasBreak);
        void onTimerStopped();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void setListener(FocusServiceListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (isRunning) return;

        PreferencesManager prefs = PreferencesManager.getInstance();
        int minutes = isBreakTime ? prefs.getBreakDuration() : prefs.getFocusDuration();
        totalTime = minutes * 60 * 1000L;
        timeRemaining = totalTime;

        startTimer();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    public void pause() {
        if (!isRunning || isPaused) return;
        
        isPaused = true;
        if (timer != null) {
            timer.cancel();
        }
        updateNotification();
    }

    public void resume() {
        if (!isRunning || !isPaused) return;
        
        isPaused = false;
        startTimer();
        updateNotification();
    }

    public void stop() {
        isRunning = false;
        isPaused = false;
        isBreakTime = false;
        completedSessions = 0;
        
        if (timer != null) {
            timer.cancel();
        }
        
        if (listener != null) {
            listener.onTimerStopped();
        }
        
        stopForeground(true);
        stopSelf();
    }

    public void skip() {
        if (!isRunning) return;
        
        if (timer != null) {
            timer.cancel();
        }
        
        onSessionFinished();
    }

    private void startTimer() {
        isRunning = true;
        isPaused = false;

        timer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                int progress = (int) ((millisUntilFinished * 100) / totalTime);
                
                if (listener != null) {
                    listener.onTimerTick(millisUntilFinished, progress);
                }
                
                updateNotification();
            }

            @Override
            public void onFinish() {
                onSessionFinished();
            }
        }.start();
    }

    private void onSessionFinished() {
        boolean wasBreak = isBreakTime;
        
        if (!isBreakTime) {
            completedSessions++;
        }
        
        isBreakTime = !isBreakTime;
        
        if (listener != null) {
            listener.onSessionComplete(wasBreak);
        }

        // Show completion notification
        showCompletionNotification(wasBreak);
        
        // Auto-start next session
        start();
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String title = isBreakTime ? getString(R.string.break_session) : getString(R.string.focus_session);
        String content = formatTime(timeRemaining);
        if (isPaused) {
            content += " (Paused)";
        }

        return new NotificationCompat.Builder(this, StudentHubApp.CHANNEL_FOCUS)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_focus)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }

    private void updateNotification() {
        if (isRunning) {
            Notification notification = buildNotification();
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void showCompletionNotification(boolean wasBreak) {
        String title = wasBreak ? getString(R.string.break_complete) : getString(R.string.focus_complete);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, StudentHubApp.CHANNEL_FOCUS)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_focus)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        androidx.core.app.NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID + 1, notification);
    }

    private String formatTime(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isBreakTime() {
        return isBreakTime;
    }

    public int getCompletedSessions() {
        return completedSessions;
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }
}
