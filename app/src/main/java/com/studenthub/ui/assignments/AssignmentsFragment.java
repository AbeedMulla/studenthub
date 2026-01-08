package com.studenthub.ui.assignments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studenthub.R;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.data.repository.DataRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying and managing assignments.
 */
public class AssignmentsFragment extends Fragment {

    private RecyclerView assignmentsList;
    private View loadingContainer, emptyState;
    private MaterialButton toggleCompleted, addFirstAssignment;
    private FloatingActionButton fabAddAssignment;

    private DataRepository repository;
    private AssignmentAdapter adapter;
    private boolean showCompleted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assignments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = DataRepository.getInstance(requireContext());
        initViews(view);
        setupListeners();
        loadAssignments();
    }

    private void initViews(View view) {
        assignmentsList = view.findViewById(R.id.assignments_list);
        loadingContainer = view.findViewById(R.id.loading_container);
        emptyState = view.findViewById(R.id.empty_state);
        toggleCompleted = view.findViewById(R.id.toggle_completed);
        addFirstAssignment = view.findViewById(R.id.add_first_assignment);
        fabAddAssignment = view.findViewById(R.id.fab_add_assignment);

        assignmentsList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupListeners() {
        fabAddAssignment.setOnClickListener(v -> openAssignmentEditor(null));
        addFirstAssignment.setOnClickListener(v -> openAssignmentEditor(null));

        toggleCompleted.setOnClickListener(v -> {
            showCompleted = !showCompleted;
            toggleCompleted.setText(showCompleted ? R.string.hide_completed : R.string.show_completed);
            loadAssignments();
        });
    }

    private void loadAssignments() {
        if (showCompleted) {
            repository.getAllAssignments().observe(getViewLifecycleOwner(), this::displayAssignments);
        } else {
            repository.getIncompleteAssignments().observe(getViewLifecycleOwner(), this::displayAssignments);
        }
    }

    private void displayAssignments(List<AssignmentEntity> assignments) {
        loadingContainer.setVisibility(View.GONE);

        if (assignments == null || assignments.isEmpty()) {
            assignmentsList.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            assignmentsList.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            // Group assignments
            List<Object> groupedItems = groupAssignments(assignments);
            
            adapter = new AssignmentAdapter(groupedItems, new AssignmentAdapter.OnAssignmentListener() {
                @Override
                public void onAssignmentClick(AssignmentEntity assignment) {
                    openAssignmentEditor(assignment.getId());
                }

                @Override
                public void onCompletedChanged(AssignmentEntity assignment, boolean completed) {
                    repository.setAssignmentCompleted(assignment.getId(), completed, null);
                }
            });
            assignmentsList.setAdapter(adapter);
        }
    }

    private List<Object> groupAssignments(List<AssignmentEntity> assignments) {
        List<Object> items = new ArrayList<>();
        
        List<AssignmentEntity> overdue = new ArrayList<>();
        List<AssignmentEntity> today = new ArrayList<>();
        List<AssignmentEntity> tomorrow = new ArrayList<>();
        List<AssignmentEntity> thisWeek = new ArrayList<>();
        List<AssignmentEntity> later = new ArrayList<>();
        List<AssignmentEntity> completed = new ArrayList<>();

        for (AssignmentEntity a : assignments) {
            if (a.isCompleted()) {
                completed.add(a);
            } else if (a.isOverdue()) {
                overdue.add(a);
            } else if (a.isDueToday()) {
                today.add(a);
            } else if (a.isDueTomorrow()) {
                tomorrow.add(a);
            } else if (a.isDueThisWeek()) {
                thisWeek.add(a);
            } else {
                later.add(a);
            }
        }

        if (!overdue.isEmpty()) {
            items.add(getString(R.string.overdue));
            items.addAll(overdue);
        }
        if (!today.isEmpty()) {
            items.add(getString(R.string.due_today));
            items.addAll(today);
        }
        if (!tomorrow.isEmpty()) {
            items.add(getString(R.string.due_tomorrow));
            items.addAll(tomorrow);
        }
        if (!thisWeek.isEmpty()) {
            items.add(getString(R.string.due_this_week));
            items.addAll(thisWeek);
        }
        if (!later.isEmpty()) {
            items.add(getString(R.string.due_later));
            items.addAll(later);
        }
        if (showCompleted && !completed.isEmpty()) {
            items.add(getString(R.string.completed));
            items.addAll(completed);
        }

        return items;
    }

    private void openAssignmentEditor(@Nullable String assignmentId) {
        Intent intent = new Intent(requireContext(), AssignmentEditorActivity.class);
        if (assignmentId != null) {
            intent.putExtra(AssignmentEditorActivity.EXTRA_ASSIGNMENT_ID, assignmentId);
        }
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAssignments();
    }
}
