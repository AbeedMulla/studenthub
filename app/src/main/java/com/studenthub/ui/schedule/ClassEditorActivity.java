package com.studenthub.ui.schedule;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.studenthub.R;
import com.studenthub.data.local.entity.ClassEntity;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activity for creating and editing classes.
 */
public class ClassEditorActivity extends AppCompatActivity {

    public static final String EXTRA_CLASS_ID = "extra_class_id";

    private MaterialToolbar toolbar;
    private TextInputLayout nameLayout;
    private TextInputEditText nameInput, startTimeInput, endTimeInput, buildingInput, roomInput, notesInput;
    private ChipGroup daysChipGroup;
    private MaterialButton saveButton, deleteButton;

    private DataRepository repository;
    private ClassEntity existingClass;
    private boolean isEditMode = false;

    private int startHour = 9, startMinute = 0;
    private int endHour = 10, endMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_editor);

        repository = DataRepository.getInstance(this);
        
        initViews();
        setupToolbar();
        setupListeners();

        String classId = getIntent().getStringExtra(EXTRA_CLASS_ID);
        if (classId != null) {
            isEditMode = true;
            loadClass(classId);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nameLayout = findViewById(R.id.name_layout);
        nameInput = findViewById(R.id.name_input);
        startTimeInput = findViewById(R.id.start_time_input);
        endTimeInput = findViewById(R.id.end_time_input);
        buildingInput = findViewById(R.id.building_input);
        roomInput = findViewById(R.id.room_input);
        notesInput = findViewById(R.id.notes_input);
        daysChipGroup = findViewById(R.id.days_chip_group);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        // Set default times
        startTimeInput.setText(DateTimeUtils.formatTime(startHour, startMinute));
        endTimeInput.setText(DateTimeUtils.formatTime(endHour, endMinute));
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(isEditMode ? R.string.edit_class : R.string.add_class);
    }

    private void setupListeners() {
        startTimeInput.setOnClickListener(v -> showTimePicker(true));
        endTimeInput.setOnClickListener(v -> showTimePicker(false));

        saveButton.setOnClickListener(v -> saveClass());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void showTimePicker(boolean isStartTime) {
        int hour = isStartTime ? startHour : endHour;
        int minute = isStartTime ? startMinute : endMinute;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(isStartTime ? R.string.start_time : R.string.end_time)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            if (isStartTime) {
                startHour = picker.getHour();
                startMinute = picker.getMinute();
                startTimeInput.setText(DateTimeUtils.formatTime(startHour, startMinute));
            } else {
                endHour = picker.getHour();
                endMinute = picker.getMinute();
                endTimeInput.setText(DateTimeUtils.formatTime(endHour, endMinute));
            }
        });

        picker.show(getSupportFragmentManager(), "time_picker");
    }

    private void loadClass(String classId) {
        repository.getClassById(classId).observe(this, classEntity -> {
            if (classEntity != null) {
                existingClass = classEntity;
                populateForm(classEntity);
                deleteButton.setVisibility(View.VISIBLE);
                toolbar.setTitle(R.string.edit_class);
            }
        });
    }

    private void populateForm(ClassEntity classEntity) {
        nameInput.setText(classEntity.getName());
        buildingInput.setText(classEntity.getBuilding());
        roomInput.setText(classEntity.getRoom());
        notesInput.setText(classEntity.getNotes());

        // Set times
        startHour = classEntity.getStartTime() / 60;
        startMinute = classEntity.getStartTime() % 60;
        endHour = classEntity.getEndTime() / 60;
        endMinute = classEntity.getEndTime() % 60;
        startTimeInput.setText(DateTimeUtils.formatTime(startHour, startMinute));
        endTimeInput.setText(DateTimeUtils.formatTime(endHour, endMinute));

        // Set days
        String days = classEntity.getDays();
        if (days != null) {
            if (days.contains("1")) ((Chip) findViewById(R.id.chip_sun)).setChecked(true);
            if (days.contains("2")) ((Chip) findViewById(R.id.chip_mon)).setChecked(true);
            if (days.contains("3")) ((Chip) findViewById(R.id.chip_tue)).setChecked(true);
            if (days.contains("4")) ((Chip) findViewById(R.id.chip_wed)).setChecked(true);
            if (days.contains("5")) ((Chip) findViewById(R.id.chip_thu)).setChecked(true);
            if (days.contains("6")) ((Chip) findViewById(R.id.chip_fri)).setChecked(true);
            if (days.contains("7")) ((Chip) findViewById(R.id.chip_sat)).setChecked(true);
        }
    }

    private void saveClass() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.required_field));
            return;
        }
        nameLayout.setError(null);

        // Get selected days
        List<String> selectedDays = new ArrayList<>();
        if (((Chip) findViewById(R.id.chip_sun)).isChecked()) selectedDays.add("1");
        if (((Chip) findViewById(R.id.chip_mon)).isChecked()) selectedDays.add("2");
        if (((Chip) findViewById(R.id.chip_tue)).isChecked()) selectedDays.add("3");
        if (((Chip) findViewById(R.id.chip_wed)).isChecked()) selectedDays.add("4");
        if (((Chip) findViewById(R.id.chip_thu)).isChecked()) selectedDays.add("5");
        if (((Chip) findViewById(R.id.chip_fri)).isChecked()) selectedDays.add("6");
        if (((Chip) findViewById(R.id.chip_sat)).isChecked()) selectedDays.add("7");

        ClassEntity classEntity = existingClass != null ? existingClass : new ClassEntity();
        classEntity.setName(name);
        classEntity.setDays(TextUtils.join(",", selectedDays));
        classEntity.setStartTime(startHour * 60 + startMinute);
        classEntity.setEndTime(endHour * 60 + endMinute);
        classEntity.setBuilding(buildingInput.getText() != null ? buildingInput.getText().toString().trim() : "");
        classEntity.setRoom(roomInput.getText() != null ? roomInput.getText().toString().trim() : "");
        classEntity.setNotes(notesInput.getText() != null ? notesInput.getText().toString().trim() : "");

        repository.saveClass(classEntity, new DataRepository.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ClassEditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ClassEditorActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_class)
                .setMessage(R.string.delete_class_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteClass())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteClass() {
        if (existingClass == null) return;

        repository.deleteClass(existingClass.getId(), new DataRepository.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ClassEditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ClassEditorActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
