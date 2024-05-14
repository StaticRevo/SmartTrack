package com.example.smarttrack;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationBarView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTaskActivity extends BaseActivity {
    private ChipGroup chipGroupCategory;
    private EditText subjectInput, chapterName;
    private Task task; // The task to be edited

    private String getCategoryColor(Chip selectedChip) {
        int chipId = selectedChip.getId();
        if (chipId == R.id.chipClass) {
            return "#67b387"; // light_green
        } else if (chipId == R.id.chipExam) {
            return "#eb5273"; // red
        } else if (chipId == R.id.chipLab) {
            return "#d867bc"; // pink
        } else if (chipId == R.id.chipAssignment) {
            return "#f5c177"; // orange
        } else if (chipId == R.id.chipOther) {
            return "#8187f7"; // purple
        } else {
            return "#FFFFFF"; // white
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Intent intent = getIntent();
        Task task = (Task) intent.getSerializableExtra("task");

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to TasksActivity
                Intent intent = new Intent(EditTaskActivity.this, TasksActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        // Store the original description
        assert task != null;
        String originalDescription = task.getDescription();
        String originalDate = task.getDate();
        String originalStartTime = task.getStartTime();

        // Initialize the NumberPicker widgets
        NumberPicker hourPickerStart = findViewById(R.id.hourPickerStart);
        NumberPicker minutePickerStart = findViewById(R.id.minutePickerStart);
        NumberPicker hourPickerEnd = findViewById(R.id.hourPickerEnd);
        NumberPicker minutePickerEnd = findViewById(R.id.minutePickerEnd);

        // Set the minimum and maximum hours for start and end
        hourPickerStart.setMinValue(0);
        hourPickerStart.setMaxValue(23);
        hourPickerEnd.setMinValue(0);
        hourPickerEnd.setMaxValue(23);

        // Set the minimum and maximum minutes for start and end
        minutePickerStart.setMinValue(0);
        minutePickerStart.setMaxValue(59);
        minutePickerEnd.setMinValue(0);
        minutePickerEnd.setMaxValue(59);

        // Initialising the necessary views
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        subjectInput = findViewById(R.id.subjectInput);
        chapterName = findViewById(R.id.chapterName);
        Button editTaskButton = findViewById(R.id.editTaskButton);

        final TextView dateText = findViewById(R.id.dateText);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current date
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Set the TextView to display the current date by default
                dateText.setText(day + "/" + (month + 1) + "/" + year);

                // Create a new instance of DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Display the selected date in the TextView
                                dateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
        // Setting an OnClickListener for the Edit Task button
        editTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if any category is selected
                int chipId = chipGroupCategory.getCheckedChipId();
                if (chipId == -1) {
                    // No category selected
                    Toast.makeText(EditTaskActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Get selected category chip
                Chip selectedChip = findViewById(chipId);
                String category = selectedChip.getText().toString();

                // Check if subject and chapter names are filled
                String subject = subjectInput.getText().toString();
                String chapter = chapterName.getText().toString();
                if (subject.isEmpty() || chapter.isEmpty()) {
                    Toast.makeText(EditTaskActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if a date has been selected
                String selectedDate = dateText.getText().toString();
                if (selectedDate.equals("fri_25_september_2024")) { // Replace with your default date string
                    Toast.makeText(EditTaskActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedStartHour = hourPickerStart.getValue();
                int selectedStartMinute = minutePickerStart.getValue();
                int selectedEndHour = hourPickerEnd.getValue();
                int selectedEndMinute = minutePickerEnd.getValue();

                String selectedStartTime = String.format("%02d:%02d", selectedStartHour, selectedStartMinute);
                String selectedEndTime = String.format("%02d:%02d", selectedEndHour, selectedEndMinute);

                // Get the task description
                String taskDescription = subject + " - " + chapter;

                // Get the task color
                String taskColor = getCategoryColor(selectedChip);

                // Update the task object with the new values
                assert task != null;
                task.setDescription(taskDescription);
                task.setColor(taskColor);
                task.setDate(selectedDate);
                task.setStartTime(selectedStartTime);
                task.setEndTime(selectedEndTime);

                // Log the updated task details
                Log.d("EditTaskActivity", "Updated task details: " + task.getDescription() + ", " + task.getColor() + ", " + task.getDate() + ", " + task.getStartTime() + ", " + task.getEndTime());

                // Update the task in the database
                DatabaseHelper db = new DatabaseHelper(EditTaskActivity.this);
                boolean isUpdated = db.updateTask(task, originalDescription, originalDate, originalStartTime);
                if (isUpdated) {
                    Toast.makeText(EditTaskActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to TasksActivity
                    Intent intent = new Intent(EditTaskActivity.this, TasksActivity.class);
                    startActivity(intent);
                    finish(); // Finish the current activity
                } else {
                    Toast.makeText(EditTaskActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
