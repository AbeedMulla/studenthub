package com.studenthub.ui.focus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.studenthub.R;
import com.studenthub.service.FocusService;
import com.studenthub.util.DateTimeUtils;
import com.studenthub.util.PreferencesManager;

/**
 * Fragment for Pomodoro focus timer.
 */
public class FocusFragment extends Fragment implements FocusService.FocusServiceListener {

    private TextView sessionTypeBadge, timerText, timerLabel, sessionsCount;
    private CircularProgressIndicator timerProgress;
    private MaterialButton playPauseButton, stopButton, skipButton;
    private ImageButton settingsButton;

    private FocusService focusService;
    private boolean serviceBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            FocusService.FocusBinder focusBinder = (FocusService.FocusBinder) binder;
            focusService = focusBinder.getService();
            focusService.setListener(FocusFragment.this);
            serviceBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            focusService = null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_focus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
        resetTimerDisplay();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), FocusService.class);
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (serviceBound) {
            if (focusService != null) {
                focusService.setListener(null);
            }
            requireContext().unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    private void initViews(View view) {
        sessionTypeBadge = view.findViewById(R.id.session_type_badge);
        timerText = view.findViewById(R.id.timer_text);
        timerLabel = view.findViewById(R.id.timer_label);
        timerProgress = view.findViewById(R.id.timer_progress);
        playPauseButton = view.findViewById(R.id.play_pause_button);
        stopButton = view.findViewById(R.id.stop_button);
        skipButton = view.findViewById(R.id.skip_button);
        sessionsCount = view.findViewById(R.id.sessions_count);
        settingsButton = view.findViewById(R.id.focus_settings_button);
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> {
            if (!serviceBound) return;

            if (focusService.isRunning()) {
                if (focusService.isPaused()) {
                    focusService.resume();
                } else {
                    focusService.pause();
                }
            } else {
                focusService.start();
            }
            updateUI();
        });

        stopButton.setOnClickListener(v -> {
            if (serviceBound && focusService.isRunning()) {
                focusService.stop();
                updateUI();
            }
        });

        skipButton.setOnClickListener(v -> {
            if (serviceBound && focusService.isRunning()) {
                focusService.skip();
                updateUI();
            }
        });

        settingsButton.setOnClickListener(v -> showSettingsDialog());
    }

    private void resetTimerDisplay() {
        int focusDuration = PreferencesManager.getInstance().getFocusDuration();
        timerText.setText(String.format("%02d:00", focusDuration));
        timerProgress.setProgress(100);
        sessionTypeBadge.setText(R.string.focus_session);
        sessionTypeBadge.setBackgroundTintList(
                getResources().getColorStateList(R.color.focus_active, null));
        timerLabel.setText(R.string.focus_time);
        sessionsCount.setText("0");
        
        stopButton.setVisibility(View.GONE);
        skipButton.setVisibility(View.GONE);
        playPauseButton.setIconResource(R.drawable.ic_play);
    }

    private void updateUI() {
        if (!serviceBound || focusService == null) {
            resetTimerDisplay();
            return;
        }

        boolean isRunning = focusService.isRunning();
        boolean isPaused = focusService.isPaused();
        boolean isBreak = focusService.isBreakTime();

        // Update controls visibility
        stopButton.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        skipButton.setVisibility(isRunning ? View.VISIBLE : View.GONE);

        // Update play/pause icon
        if (isRunning && !isPaused) {
            playPauseButton.setIconResource(R.drawable.ic_pause);
        } else {
            playPauseButton.setIconResource(R.drawable.ic_play);
        }

        // Update session type badge
        if (isBreak) {
            sessionTypeBadge.setText(R.string.break_session);
            sessionTypeBadge.setBackgroundTintList(
                    getResources().getColorStateList(R.color.break_active, null));
            timerLabel.setText(R.string.break_time);
            timerProgress.setIndicatorColor(getResources().getColor(R.color.break_active, null));
        } else {
            sessionTypeBadge.setText(R.string.focus_session);
            sessionTypeBadge.setBackgroundTintList(
                    getResources().getColorStateList(R.color.focus_active, null));
            timerLabel.setText(R.string.focus_time);
            timerProgress.setIndicatorColor(getResources().getColor(R.color.focus_active, null));
        }

        // Update sessions count
        sessionsCount.setText(String.valueOf(focusService.getCompletedSessions()));
    }

    @Override
    public void onTimerTick(long millisRemaining, int progress) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            timerText.setText(DateTimeUtils.formatCountdown(millisRemaining));
            timerProgress.setProgress(progress);
        });
    }

    @Override
    public void onSessionComplete(boolean wasBreak) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(this::updateUI);
    }

    @Override
    public void onTimerStopped() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(this::resetTimerDisplay);
    }

    private void showSettingsDialog() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        
        String[] focusOptions = getResources().getStringArray(R.array.focus_duration_options);
        int[] focusValues = getResources().getIntArray(R.array.focus_duration_values);
        int currentFocus = prefs.getFocusDuration();
        int focusIndex = 2; // Default to 25 min
        for (int i = 0; i < focusValues.length; i++) {
            if (focusValues[i] == currentFocus) {
                focusIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.focus_duration)
                .setSingleChoiceItems(focusOptions, focusIndex, (dialog, which) -> {
                    prefs.setFocusDuration(focusValues[which]);
                    if (!focusService.isRunning()) {
                        resetTimerDisplay();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
