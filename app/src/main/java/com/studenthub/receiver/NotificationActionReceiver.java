package com.studenthub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver for notification action buttons.
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_MARK_COMPLETE = "com.studenthub.ACTION_MARK_COMPLETE";
    public static final String EXTRA_ASSIGNMENT_ID = "extra_assignment_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_MARK_COMPLETE.equals(action)) {
            String assignmentId = intent.getStringExtra(EXTRA_ASSIGNMENT_ID);
            if (assignmentId != null) {
                // Mark assignment as complete
                // DataRepository.getInstance(context).setAssignmentCompleted(assignmentId, true, null);
            }
        }
    }
}
