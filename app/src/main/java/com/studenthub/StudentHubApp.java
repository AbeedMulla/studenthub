package com.studenthub;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.studenthub.util.PreferencesManager;

/**
 * Main Application class for StudentHub.
 * Handles app-wide initialization including notification channels and theme setup.
 */
public class StudentHubApp extends Application {

    // Notification Channel IDs
    public static final String CHANNEL_CLASSES = "channel_classes";
    public static final String CHANNEL_ASSIGNMENTS = "channel_assignments";
    public static final String CHANNEL_FOCUS = "channel_focus";
    public static final String CHANNEL_SYNC = "channel_sync";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize preferences
        PreferencesManager.init(this);
        
        // Apply saved theme preference
        applyTheme();
        
        // Create notification channels
        createNotificationChannels();
    }

    /**
     * Apply the saved theme preference (light/dark mode).
     */
    private void applyTheme() {
        boolean isDarkMode = PreferencesManager.getInstance().isDarkMode();
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Create all notification channels required by the app.
     * This is required for Android 8.0 (API 26) and above.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            
            // Class reminders channel
            NotificationChannel classChannel = new NotificationChannel(
                CHANNEL_CLASSES,
                getString(R.string.notification_channel_classes),
                NotificationManager.IMPORTANCE_HIGH
            );
            classChannel.setDescription(getString(R.string.notification_channel_classes_desc));
            classChannel.enableVibration(true);
            manager.createNotificationChannel(classChannel);

            // Assignment reminders channel
            NotificationChannel assignmentChannel = new NotificationChannel(
                CHANNEL_ASSIGNMENTS,
                getString(R.string.notification_channel_assignments),
                NotificationManager.IMPORTANCE_HIGH
            );
            assignmentChannel.setDescription(getString(R.string.notification_channel_assignments_desc));
            assignmentChannel.enableVibration(true);
            manager.createNotificationChannel(assignmentChannel);

            // Focus mode channel
            NotificationChannel focusChannel = new NotificationChannel(
                CHANNEL_FOCUS,
                getString(R.string.notification_channel_focus),
                NotificationManager.IMPORTANCE_LOW
            );
            focusChannel.setDescription(getString(R.string.notification_channel_focus_desc));
            manager.createNotificationChannel(focusChannel);

            // Sync status channel
            NotificationChannel syncChannel = new NotificationChannel(
                CHANNEL_SYNC,
                getString(R.string.notification_channel_sync),
                NotificationManager.IMPORTANCE_LOW
            );
            syncChannel.setDescription(getString(R.string.notification_channel_sync_desc));
            manager.createNotificationChannel(syncChannel);
        }
    }
}
