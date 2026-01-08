package com.studenthub.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.studenthub.R;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.data.local.entity.ClassEntity;
import com.studenthub.data.local.entity.TaskEntity;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.ui.MainActivity;
import com.studenthub.util.DateTimeUtils;
import com.studenthub.util.PreferencesManager;

import java.util.Calendar;
import java.util.List;

/**
 * Home dashboard fragment showing context-aware cards.
 * Displays next class, upcoming assignments, focus shortcut, and today's tasks.
 */
public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private TextView greetingText, modeBadge;
    private MaterialCardView nextClassCard, assignmentsCard, focusCard, tasksCard;
    private TextView nextClassName, nextClassLocation, nextClassCountdown, noClassText;
    private TextView noAssignmentsText, noTasksText, tasksCount;
    private RecyclerView assignmentsPreviewList, tasksPreviewList;

    private DataRepository repository;
    private Handler handler;
    private Runnable countdownRunnable;

    private ClassEntity nextClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        repository = DataRepository.getInstance(requireContext());
        handler = new Handler(Looper.getMainLooper());
        
        initViews(view);
        setupListeners();
        loadData();
        updateModeBadge();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        greetingText = view.findViewById(R.id.greeting_text);
        modeBadge = view.findViewById(R.id.mode_badge);
        
        // Next Class Card
        nextClassCard = view.findViewById(R.id.next_class_card);
        nextClassName = view.findViewById(R.id.next_class_name);
        nextClassLocation = view.findViewById(R.id.next_class_location);
        nextClassCountdown = view.findViewById(R.id.next_class_countdown);
        noClassText = view.findViewById(R.id.no_class_text);
        
        // Assignments Card
        assignmentsCard = view.findViewById(R.id.assignments_card);
        assignmentsPreviewList = view.findViewById(R.id.assignments_preview_list);
        noAssignmentsText = view.findViewById(R.id.no_assignments_text);
        assignmentsPreviewList.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Focus Card
        focusCard = view.findViewById(R.id.focus_card);
        
        // Tasks Card
        tasksCard = view.findViewById(R.id.tasks_card);
        tasksPreviewList = view.findViewById(R.id.tasks_preview_list);
        noTasksText = view.findViewById(R.id.no_tasks_text);
        tasksCount = view.findViewById(R.id.tasks_count);
        tasksPreviewList.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Set greeting
        greetingText.setText(DateTimeUtils.getGreeting());
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            repository.sync(new DataRepository.OnSyncCallback() {
                @Override
                public void onSuccess() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            swipeRefresh.setRefreshing(false);
                            loadData();
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            swipeRefresh.setRefreshing(false);
                        });
                    }
                }
            });
        });

        nextClassCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_schedule);
            }
        });

        assignmentsCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_assignments);
            }
        });

        focusCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_focus);
            }
        });

        tasksCard.setOnClickListener(v -> {
            // Could navigate to a tasks tab if we add one
        });
    }

    private void loadData() {
        loadNextClass();
        loadUpcomingAssignments();
        loadTodaysTasks();
    }

    private void loadNextClass() {
        int today = DateTimeUtils.getCurrentDayOfWeek();
        
        repository.getClassesForDaySync(today, new DataRepository.OnDataCallback<List<ClassEntity>>() {
            @Override
            public void onSuccess(List<ClassEntity> classes) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    nextClass = findNextClass(classes);
                    updateNextClassUI();
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private ClassEntity findNextClass(List<ClassEntity> classes) {
        if (classes == null || classes.isEmpty()) return null;
        
        Calendar now = Calendar.getInstance();
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        
        for (ClassEntity c : classes) {
            if (c.getEndTime() > currentMinutes) {
                return c;
            }
        }
        return null;
    }

    private void updateNextClassUI() {
        if (nextClass != null) {
            nextClassName.setVisibility(View.VISIBLE);
            nextClassLocation.setVisibility(View.VISIBLE);
            nextClassCountdown.setVisibility(View.VISIBLE);
            noClassText.setVisibility(View.GONE);
            
            nextClassName.setText(nextClass.getName());
            nextClassLocation.setText(nextClass.getLocation());
            
            startCountdown();
        } else {
            nextClassName.setVisibility(View.GONE);
            nextClassLocation.setVisibility(View.GONE);
            nextClassCountdown.setVisibility(View.GONE);
            noClassText.setVisibility(View.VISIBLE);
        }
    }

    private void startCountdown() {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
        
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (nextClass == null || getActivity() == null) return;
                
                Calendar now = Calendar.getInstance();
                int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
                
                if (currentMinutes >= nextClass.getStartTime() && currentMinutes < nextClass.getEndTime()) {
                    nextClassCountdown.setText(R.string.in_progress);
                    nextClassCountdown.setTextColor(getResources().getColor(R.color.success, null));
                } else if (currentMinutes < nextClass.getStartTime()) {
                    int minutesUntil = nextClass.getStartTime() - currentMinutes;
                    if (minutesUntil < 60) {
                        nextClassCountdown.setText(getString(R.string.starts_in, minutesUntil + " min"));
                    } else {
                        int hours = minutesUntil / 60;
                        int mins = minutesUntil % 60;
                        nextClassCountdown.setText(getString(R.string.starts_in, hours + "h " + mins + "m"));
                    }
                    nextClassCountdown.setTextColor(getResources().getColor(R.color.primary, null));
                }
                
                handler.postDelayed(this, 60000); // Update every minute
            }
        };
        
        handler.post(countdownRunnable);
    }

    private void loadUpcomingAssignments() {
        repository.getUpcomingAssignments(3, new DataRepository.OnDataCallback<List<AssignmentEntity>>() {
            @Override
            public void onSuccess(List<AssignmentEntity> assignments) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    if (assignments.isEmpty()) {
                        assignmentsPreviewList.setVisibility(View.GONE);
                        noAssignmentsText.setVisibility(View.VISIBLE);
                    } else {
                        assignmentsPreviewList.setVisibility(View.VISIBLE);
                        noAssignmentsText.setVisibility(View.GONE);
                        
                        AssignmentPreviewAdapter adapter = new AssignmentPreviewAdapter(assignments);
                        assignmentsPreviewList.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void loadTodaysTasks() {
        repository.getRecentTasks(5, new DataRepository.OnDataCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> tasks) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    if (tasks.isEmpty()) {
                        tasksPreviewList.setVisibility(View.GONE);
                        noTasksText.setVisibility(View.VISIBLE);
                        tasksCount.setText("0 remaining");
                    } else {
                        tasksPreviewList.setVisibility(View.VISIBLE);
                        noTasksText.setVisibility(View.GONE);
                        tasksCount.setText(tasks.size() + " remaining");
                        
                        TaskPreviewAdapter adapter = new TaskPreviewAdapter(tasks, (task, completed) -> {
                            repository.setTaskCompleted(task.getId(), completed, null);
                        });
                        tasksPreviewList.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void updateModeBadge() {
        int mode = PreferencesManager.getInstance().getCurrentMode();
        
        switch (mode) {
            case PreferencesManager.MODE_HOME:
                modeBadge.setText(R.string.mode_home);
                modeBadge.setBackgroundTintList(
                    getResources().getColorStateList(R.color.mode_home, null));
                break;
            case PreferencesManager.MODE_CAMPUS:
                modeBadge.setText(R.string.mode_campus);
                modeBadge.setBackgroundTintList(
                    getResources().getColorStateList(R.color.mode_campus, null));
                break;
            default:
                modeBadge.setText(R.string.mode_home);
                modeBadge.setBackgroundTintList(
                    getResources().getColorStateList(R.color.mode_home, null));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }
}
