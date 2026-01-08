package com.studenthub.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.studenthub.R;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.ui.auth.LoginActivity;
import com.studenthub.util.DateTimeUtils;
import com.studenthub.util.PreferencesManager;

/**
 * Settings fragment for app preferences.
 */
public class SettingsFragment extends Fragment {

    private TextView userEmail, classReminderValue, quietHoursValue;
    private TextView homeLocationValue, campusLocationValue, currentModeValue, lastSyncValue;
    private MaterialSwitch darkModeSwitch, dueTomorrowSwitch, dueHourSwitch, quietHoursSwitch;
    private CircularProgressIndicator syncProgress;
    private View logoutButton, classReminderSetting, quietHoursSetting;
    private View homeLocationSetting, campusLocationSetting, modeToggleButton, syncNowButton;

    private PreferencesManager prefs;
    private DataRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefs = PreferencesManager.getInstance();
        repository = DataRepository.getInstance(requireContext());
        
        initViews(view);
        loadSettings();
        setupListeners();
    }

    private void initViews(View view) {
        userEmail = view.findViewById(R.id.user_email);
        logoutButton = view.findViewById(R.id.logout_button);
        
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        
        classReminderSetting = view.findViewById(R.id.class_reminder_setting);
        classReminderValue = view.findViewById(R.id.class_reminder_value);
        dueTomorrowSwitch = view.findViewById(R.id.due_tomorrow_switch);
        dueHourSwitch = view.findViewById(R.id.due_hour_switch);
        quietHoursSetting = view.findViewById(R.id.quiet_hours_setting);
        quietHoursValue = view.findViewById(R.id.quiet_hours_value);
        quietHoursSwitch = view.findViewById(R.id.quiet_hours_switch);
        
        homeLocationSetting = view.findViewById(R.id.home_location_setting);
        homeLocationValue = view.findViewById(R.id.home_location_value);
        campusLocationSetting = view.findViewById(R.id.campus_location_setting);
        campusLocationValue = view.findViewById(R.id.campus_location_value);
        modeToggleButton = view.findViewById(R.id.mode_toggle_button);
        currentModeValue = view.findViewById(R.id.current_mode_value);
        
        syncNowButton = view.findViewById(R.id.sync_now_button);
        lastSyncValue = view.findViewById(R.id.last_sync_value);
        syncProgress = view.findViewById(R.id.sync_progress);
    }

    private void loadSettings() {
        // User email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail.setText(user.getEmail());
        }

        // Dark mode
        darkModeSwitch.setChecked(prefs.isDarkMode());

        // Class reminders
        int reminderMinutes = prefs.getClassReminderMinutes();
        classReminderValue.setText(getString(R.string.minutes_before, reminderMinutes));

        // Assignment reminders
        dueTomorrowSwitch.setChecked(prefs.isDueTomorrowReminderEnabled());
        dueHourSwitch.setChecked(prefs.isDueHourReminderEnabled());

        // Quiet hours
        quietHoursSwitch.setChecked(prefs.isQuietHoursEnabled());
        updateQuietHoursDisplay();

        // Location
        updateLocationDisplay();

        // Mode
        updateModeDisplay();

        // Last sync
        updateLastSyncDisplay();
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> confirmLogout());

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setDarkMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        classReminderSetting.setOnClickListener(v -> showReminderTimeDialog());

        dueTomorrowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.setDueTomorrowReminderEnabled(isChecked));

        dueHourSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.setDueHourReminderEnabled(isChecked));

        quietHoursSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.setQuietHoursEnabled(isChecked));

        homeLocationSetting.setOnClickListener(v -> openLocationPicker("home"));
        campusLocationSetting.setOnClickListener(v -> openLocationPicker("campus"));

        modeToggleButton.setOnClickListener(v -> showModeDialog());

        syncNowButton.setOnClickListener(v -> performSync());
    }

    private void showReminderTimeDialog() {
        String[] options = getResources().getStringArray(R.array.reminder_times);
        int[] values = getResources().getIntArray(R.array.reminder_times_values);
        int currentValue = prefs.getClassReminderMinutes();
        
        int selectedIndex = 2; // Default
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reminder_time)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    prefs.setClassReminderMinutes(values[which]);
                    classReminderValue.setText(getString(R.string.minutes_before, values[which]));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateQuietHoursDisplay() {
        int start = prefs.getQuietHoursStart();
        int end = prefs.getQuietHoursEnd();
        String startStr = DateTimeUtils.formatTime(start / 60, start % 60);
        String endStr = DateTimeUtils.formatTime(end / 60, end % 60);
        quietHoursValue.setText(startStr + " - " + endStr);
    }

    private void updateLocationDisplay() {
        if (prefs.hasHomeLocation()) {
            homeLocationValue.setText(String.format("%.4f, %.4f", 
                    prefs.getHomeLatitude(), prefs.getHomeLongitude()));
        } else {
            homeLocationValue.setText(R.string.set_location);
        }

        if (prefs.hasCampusLocation()) {
            campusLocationValue.setText(String.format("%.4f, %.4f", 
                    prefs.getCampusLatitude(), prefs.getCampusLongitude()));
        } else {
            campusLocationValue.setText(R.string.set_location);
        }
    }

    private void updateModeDisplay() {
        int mode = prefs.getManualMode();
        String[] modeOptions = getResources().getStringArray(R.array.mode_options);
        currentModeValue.setText(modeOptions[mode]);
    }

    private void updateLastSyncDisplay() {
        long lastSync = prefs.getLastSyncTime();
        if (lastSync == 0) {
            lastSyncValue.setText(R.string.never_synced);
        } else {
            lastSyncValue.setText(getString(R.string.last_synced, DateTimeUtils.formatDateTime(lastSync)));
        }
    }

    private void openLocationPicker(String type) {
        Intent intent = new Intent(requireContext(), LocationPickerActivity.class);
        intent.putExtra(LocationPickerActivity.EXTRA_LOCATION_TYPE, type);
        startActivity(intent);
    }

    private void showModeDialog() {
        String[] options = getResources().getStringArray(R.array.mode_options);
        int currentMode = prefs.getManualMode();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.current_mode)
                .setSingleChoiceItems(options, currentMode, (dialog, which) -> {
                    prefs.setManualMode(which);
                    if (which != PreferencesManager.MODE_AUTO) {
                        prefs.setCurrentMode(which);
                    }
                    updateModeDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performSync() {
        syncProgress.setVisibility(View.VISIBLE);
        
        repository.sync(new DataRepository.OnSyncCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    syncProgress.setVisibility(View.GONE);
                    updateLastSyncDisplay();
                    Toast.makeText(requireContext(), R.string.sync_success, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    syncProgress.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.sync_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.logout, (dialog, which) -> logout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLocationDisplay();
    }
}
