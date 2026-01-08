package com.studenthub.ui.assignments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.studenthub.R;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.util.DateTimeUtils;

import java.util.Calendar;

/**
 * Activity for creating and editing assignments.
 */
public class AssignmentEditorActivity extends AppCompatActivity {

    public static final String EXTRA_ASSIGNMENT_ID = "extra_assignment_id";

    private MaterialToolbar toolbar;
    private TextInputLayout titleLayout, courseLayout;
    private TextInputEditText titleInput, courseInput, dueDateInput, dueTimeInput, notesInput;
    private ChipGroup priorityChipGroup;
    private MaterialButton saveButton, deleteButton;

    private DataRepository repository;
    private AssignmentEntity existingAssignment;
    private boolean isEditMode = false;

    private long selectedDueDate = 0;
    private int dueHour = 23, dueMinute = 59;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_editor);

        repository = DataRepository.getInstance(this);

        initViews();
        setupToolbar();
        setupListeners();

        // Set default due date to tomorrow
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 23);
        tomorrow.set(Calendar.MINUTE, 59);
        selectedDueDate = tomorrow.getTimeInMillis();
        updateDueDateDisplay();

        String assignmentId = getIntent().getStringExtra(EXTRA_ASSIGNMENT_ID);
        if (assignmentId != null) {
            isEditMode = true;
            loadAssignment(assignmentId);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        titleLayout = findViewById(R.id.title_layout);
        courseLayout = findViewById(R.id.course_layout);
        titleInput = findViewById(R.id.title_input);
        courseInput = findViewById(R.id.course_input);
        dueDateInput = findViewById(R.id.due_date_input);
        dueTimeInput = findViewById(R.id.due_time_input);
        notesInput = findViewById(R.id.notes_input);
        priorityChipGroup = findViewById(R.id.priority_chip_group);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(isEditMode ? R.string.edit_assignment : R.string.add_assignment);
    }

    private void setupListeners() {
        dueDateInput.setOnClickListener(v -> showDatePicker());
        dueTimeInput.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> saveAssignment());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.due_date)
                .setSelection(selectedDueDate)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selection);
            cal.set(Calendar.HOUR_OF_DAY, dueHour);
            cal.set(Calendar.MINUTE, dueMinute);
            selectedDueDate = cal.getTimeInMillis();
            updateDueDateDisplay();
        });

        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(dueHour)
                .setMinute(dueMinute)
                .setTitleText(R.string.due_time)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            dueHour = picker.getHour();
            dueMinute = picker.getMinute();
            
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDueDate);
            cal.set(Calendar.HOUR_OF_DAY, dueHour);
            cal.set(Calendar.MINUTE, dueMinute);
            selectedDueDate = cal.getTimeInMillis();
            
            updateDueDateDisplay();
        });

        picker.show(getSupportFragmentManager(), "time_picker");
    }

    private void updateDueDateDisplay() {
        dueDateInput.setText(DateTimeUtils.formatDate(selectedDueDate));
        dueTimeInput.setText(DateTimeUtils.formatTime(selectedDueDate));
    }

    private void loadAssignment(String assignmentId) {
        repository.getAssignmentById(assignmentId).observe(this, assignment -> {
            if (assignment != null) {
                existingAssignment = assignment;
                populateForm(assignment);
                deleteButton.setVisibility(View.VISIBLE);
                toolbar.setTitle(R.string.edit_assignment);
            }
        });
    }

    private void populateForm(AssignmentEntity assignment) {
        titleInput.setText(assignment.getTitle());
        courseInput.setText(assignment.getCourse());
        notesInput.setText(assignment.getNotes());

        selectedDueDate = assignment.getDueDate();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDueDate);
        dueHour = cal.get(Calendar.HOUR_OF_DAY);
        dueMinute = cal.get(Calendar.MINUTE);
        updateDueDateDisplay();

        // Set priority
        switch (assignment.getPriority()) {
            case AssignmentEntity.PRIORITY_LOW:
                ((Chip) findViewById(R.id.chip_low)).setChecked(true);
                break;
            case AssignmentEntity.PRIORITY_HIGH:
                ((Chip) findViewById(R.id.chip_high)).setChecked(true);
                break;
            default:
                ((Chip) findViewById(R.id.chip_medium)).setChecked(true);
        }
    }

    private void saveAssignment() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String course = courseInput.getText() != null ? courseInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError(getString(R.string.required_field));
            return;
        }
        titleLayout.setError(null);

        // Get priority
        int priority = AssignmentEntity.PRIORITY_MEDIUM;
        if (((Chip) findViewById(R.id.chip_low)).isChecked()) {
            priority = AssignmentEntity.PRIORITY_LOW;
        } else if (((Chip) findViewById(R.id.chip_high)).isChecked()) {
            priority = AssignmentEntity.PRIORITY_HIGH;
        }

        AssignmentEntity assignment = existingAssignment != null ? existingAssignment : new AssignmentEntity();
        assignment.setTitle(title);
        assignment.setCourse(course);
        assignment.setDueDate(selectedDueDate);
        assignment.setPriority(priority);
        assignment.setNotes(notesInput.getText() != null ? notesInput.getText().toString().trim() : "");

        repository.saveAssignment(assignment, new DataRepository.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AssignmentEditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AssignmentEditorActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_assignment)
                .setMessage(R.string.delete_assignment_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteAssignment())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAssignment() {
        if (existingAssignment == null) return;

        repository.deleteAssignment(existingAssignment.getId(), new DataRepository.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AssignmentEditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AssignmentEditorActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
