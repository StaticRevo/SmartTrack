package com.example.smarttrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class TasksActivity extends AppCompatActivity {
    private RecyclerView daysOfWeekRecyclerView;
    private DaysAdapter daysAdapter;
    private List<DayInfo> days;
    private DatabaseHelper dbHelper = new DatabaseHelper(this);

    private String userEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Get the user email from the intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");

        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        // Find the TextView and set the current date
        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(currentDate);

        MaterialButton addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });
        daysOfWeekRecyclerView = findViewById(R.id.daysOfWeekRecyclerView);
        daysOfWeekRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        days = generateDays();
        daysAdapter = new DaysAdapter(this, days); // Ensure you have adjusted the constructor of DaysAdapter for Java
        daysOfWeekRecyclerView.setAdapter(daysAdapter);

        daysAdapter.setOnDayClickListener(new DaysAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(int position) {
                daysAdapter.setSelectedPosition(position);
                // Fetch tasks for the selected day
                String selectedDate = days.get(position).getFullDate();
                if (userEmail != null && selectedDate != null) {
                    List<Task> tasks = dbHelper.getTasksForDate(userEmail, selectedDate);
                    // Clear the tasksLinearLayout
                    LinearLayout tasksLinearLayout = findViewById(R.id.tasksLinearLayout);
                    tasksLinearLayout.removeAllViews();
                    // Add tasks to the tasksLinearLayout
                    for (Task task : tasks) {
                        // Inflate the task_item layout
                        View taskView = LayoutInflater.from(TasksActivity.this).inflate(R.layout.task_item, tasksLinearLayout, false);
                        // Set the task details
                        TextView taskTitle = taskView.findViewById(R.id.task_title);
                        taskTitle.setText(task.getDescription());
                        TextView taskStartTime = taskView.findViewById(R.id.task_start_time);
                        taskStartTime.setText(task.getStartTime());
                        TextView taskEndTime = taskView.findViewById(R.id.task_end_time);
                        taskEndTime.setText(task.getEndTime());

                        // Set the color of the CardView based on the color of the task
                        androidx.cardview.widget.CardView cardView = taskView.findViewById(R.id.task_card_view); // Replace with the actual id of your CardView
                        cardView.setCardBackgroundColor(Color.parseColor(task.getColor())); // Parse the color string to an actual color

                        // Add the task view to the tasksLinearLayout
                        tasksLinearLayout.addView(taskView);

                        ImageView deleteButton = taskView.findViewById(R.id.delete_button);
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Delete the task from the database
                                dbHelper.deleteTask(userEmail, task.getDescription());
                                // Remove the task view from the tasksLinearLayout
                                tasksLinearLayout.removeView(taskView);
                                // Display a toast message
                                Toast.makeText(TasksActivity.this, "Task removed successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

                        ImageView editButton = taskView.findViewById(R.id.edit_button);
                        editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Start the EditTaskActivity
                                Intent intent = new Intent(TasksActivity.this, EditTaskActivity.class);
                                intent.putExtra("task", task);
                                startActivity(intent);
                            }
                        });
                    }
                } else {
                    // Log which one is null
                    if (userEmail == null) {
                        Log.d("TasksActivity", "userEmail is null");
                    }
                    if (selectedDate == null) {
                        Log.d("TasksActivity", "selectedDate is null");
                    }
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
                    intent = new Intent(TasksActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.navigation_user) {
                    intent = new Intent(TasksActivity.this, ProfileActivity.class);
                } else if (item.getItemId() == R.id.navigation_settings) {
                    intent = new Intent(TasksActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    //startActivity(intent);
                   // overridePendingTransition(0, 0);
                   // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                }
                return true;
            }
        });
    }
    private List<DayInfo> generateDays() {
        List<DayInfo> daysList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 28; i++) {
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
            String date = new SimpleDateFormat("d", Locale.getDefault()).format(calendar.getTime());
            String fullDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(calendar.getTime());

            daysList.add(new DayInfo(dayOfWeek, date, fullDate));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return daysList;
    }
}
