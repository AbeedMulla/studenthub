package com.studenthub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.studenthub.util.PreferencesManager;

/**
 * Broadcast receiver for geofence transitions.
 * Automatically switches between Home and Campus modes based on location.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";
    
    public static final String GEOFENCE_ID_HOME = "geofence_home";
    public static final String GEOFENCE_ID_CAMPUS = "geofence_campus";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: " + geofencingEvent.getErrorCode());
            return;
        }

        int transition = geofencingEvent.getGeofenceTransition();
        PreferencesManager prefs = PreferencesManager.getInstance();

        // Only process if manual mode is set to Auto
        if (prefs.getManualMode() != PreferencesManager.MODE_AUTO) {
            return;
        }

        for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
            String geofenceId = geofence.getRequestId();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (GEOFENCE_ID_HOME.equals(geofenceId)) {
                    Log.d(TAG, "Entered home geofence");
                    prefs.setCurrentMode(PreferencesManager.MODE_HOME);
                } else if (GEOFENCE_ID_CAMPUS.equals(geofenceId)) {
                    Log.d(TAG, "Entered campus geofence");
                    prefs.setCurrentMode(PreferencesManager.MODE_CAMPUS);
                }
            } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "Exited geofence: " + geofenceId);
            }
        }
    }
}
