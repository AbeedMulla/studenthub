package com.studenthub.ui.tasks;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.studenthub.R;
import com.studenthub.data.local.entity.TaskEntity;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.util.DateTimeUtils;

/**
 * Activity for creating and editing tasks.
 */
public class TaskEditorActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private MaterialToolbar toolbar;
    private TextInputLayout titleLayout;
    private TextInputEditText titleInput, dueDateInput, tagsInput;
    private MaterialButton saveButton, deleteButton;

    private DataRepository repository;
    private TaskEntity existingTask;
    private boolean isEditMode = false;
    private Long selectedDueDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        repository = DataRepository.getInstance(this);

        initViews();
        setupToolbar();
        setupListeners();

        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (taskId != null) {
            isEditMode = true;
            loadTask(taskId);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        titleLayout = findViewById(R.id.title_layout);
        titleInput = findViewById(R.id.title_input);
        dueDateInput = findViewById(R.id.due_date_input);
        tagsInput = findViewById(R.id.tags_input);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(isEditMode ? R.string.edit_task : R.string.add_task);
    }

    private void setupListeners() {
        dueDateInput.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> saveTask());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.task_due_date)
                .setSelection(selectedDueDate != null ? selectedDueDate : System.currentTimeMillis())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDueDate = selection;
            dueDateInput.setText(DateTimeUtils.formatDate(selection));
        });

        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void loadTask(String taskId) {
        repository.getTaskById(taskId).observe(this, task -> {
            if (task != null) {
                existingTask = task;
                populateForm(task);
                deleteButton.setVisibility(View.VISIBLE);
                toolbar.setTitle(R.string.edit_task);
            }
        });
    }

    private void populateForm(TaskEntity task) {
        titleInput.setText(task.getTitle());
        tagsInput.setText(task.getTags());

        if (task.hasDueDate()) {
            selectedDueDate = task.getDueDate();
            dueDateInput.setText(DateTimeUtils.formatDate(selectedDueDate));
        }
    }

    private void saveTask() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError(getString(R.string.required_field));
            return;
        }
        titleLayout.setError(null);

        TaskEntity task = existingTask != null ? existingTask : new TaskEntity();
        task.setTitle(title);
        task.setDueDate(selectedDueDate);
        task.setTags(tagsInput.getText() != null ? tagsInput.getText().toString().trim() : "");

        repository.saveTask(task, new DataRepository.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(TaskEditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(TaskEditorActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task)
                .setMessage(R.string.delete_task_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteTask())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteTask() {
        if (existingTask == null) return;

        repository.deleteTask(existingTask.getId(), new DataRepository.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(TaskEditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(TaskEditorActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
