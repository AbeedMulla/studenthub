package com.studenthub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast receiver for device boot.
 * Re-schedules reminders after device restart.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, re-scheduling reminders");
            // TODO: Re-schedule WorkManager tasks for reminders
            // This would reschedule any pending class/assignment reminders
        }
    }
}
