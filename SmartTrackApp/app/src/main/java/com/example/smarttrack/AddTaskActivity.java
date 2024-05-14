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

public class AddTaskActivity extends BaseActivity {
    private ChipGroup chipGroupCategory;
    private EditText subjectInput, chapterName;

    private String getCurrentUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userEmail", "");
    }
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
        setContentView(R.layout.activity_add_task);

        // Common profile icon setup
        setupProfileIcon();

        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        // Find the TextView and set the current date
        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(currentDate);

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
        Button addTaskButton = findViewById(R.id.addTaskButton);

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
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this,
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

        // Setting an OnClickListener for the Add Task button
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if any category is selected
                int chipId = chipGroupCategory.getCheckedChipId();
                if (chipId == -1) {
                    // No category selected
                    Toast.makeText(AddTaskActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get selected category chip
                Chip selectedChip = findViewById(chipId);
                String category = selectedChip.getText().toString();

                // Check if subject and chapter names are filled
                String subject = subjectInput.getText().toString();
                String chapter = chapterName.getText().toString();
                if (subject.isEmpty() || chapter.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if a date has been selected
                String selectedDate = dateText.getText().toString();
                if (selectedDate.equals("fri_25_september_2024")) { // Replace with your default date string
                    Toast.makeText(AddTaskActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedStartHour = hourPickerStart.getValue();
                int selectedStartMinute = minutePickerStart.getValue();

                // Check if an end time has been selected
                int selectedEndHour = hourPickerEnd.getValue();
                int selectedEndMinute = minutePickerEnd.getValue();
                // Add any necessary checks for the time

                String selectedStartTime = String.format("%02d:%02d", selectedStartHour, selectedStartMinute);
                String selectedEndTime = String.format("%02d:%02d", selectedEndHour, selectedEndMinute);

                // Get the user email
                String userEmail = getCurrentUserEmail();

                // Get the task description
                String taskDescription = subject + " - " + chapter;

                // Get the task color
                String taskColor = getCategoryColor(selectedChip);

                // Add the task to the database
                DatabaseHelper db = new DatabaseHelper(AddTaskActivity.this);

                Log.d("AddTaskActivity", "Colour : " + taskColor);
                boolean isInserted = db.addTask(userEmail, taskDescription, taskColor, selectedDate, selectedStartTime, selectedEndTime);

                if (isInserted) {
                    Toast.makeText(AddTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to TasksActivity
                    Intent intent = new Intent(AddTaskActivity.this, TasksActivity.class);
                    intent.putExtra("userEmail", userEmail); // Pass the user email to the TasksActivity
                    startActivity(intent);
                    finish(); // Finish the current activity
                } else {
                    Toast.makeText(AddTaskActivity.this, "Failed to add task", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        BottomNavigationView bottomNav = findViewById(R.id.navigation_bar);
        bottomNav.setSelectedItemId(R.id.navigation_tasks);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.navigation_home) {
                    intent = new Intent(AddTaskActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.navigation_user) {
                    intent = new Intent(AddTaskActivity.this, ProfileActivity.class);
                } else if (item.getItemId() == R.id.navigation_settings) {
                    intent = new Intent(AddTaskActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    //startActivity(intent);
                    // overridePendingTransition(0, 0);
                    // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                }
                return true; // indicate that we handled the event
            }
        });
    }
}
