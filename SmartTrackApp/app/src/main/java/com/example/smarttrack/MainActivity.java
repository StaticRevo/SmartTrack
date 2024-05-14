package com.example.smarttrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the saved theme choice from SharedPreferences before setting the content view
        SharedPreferences prefs = getSharedPreferences("AppSettingsPrefs", 0);
        boolean isDarkThemeEnabled = prefs.getBoolean("DarkTheme", false);
        AppCompatDelegate.setDefaultNightMode(isDarkThemeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);


        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("userEmail"); // Get the email from the intent

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Fetch the username using the email
        String username = "User"; // Default username
        if (userEmail != null) {
            username = dbHelper.getUsername(userEmail);
        }

        // Common profile icon setup
        setupProfileIcon();

        // Fetch tasks for the current day
        String currentDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        List<Task> tasks = dbHelper.getTasksForDate(userEmail, currentDate);

        // Fetch the task count for the user
        int taskCount = dbHelper.getTaskCountForDate(userEmail, currentDate);
        // Update the greeting text view with the fetched username
        TextView greetingText = findViewById(R.id.greeting_text);
        greetingText.setText("Hello " + username);

        // Update the task count text
        TextView taskCountText = findViewById(R.id.task_count_text);
        taskCountText.setText("You've got " + taskCount + " tasks today");


        // Get the LinearLayout
        LinearLayout tasksLinearLayout = findViewById(R.id.tasksLinearLayoutMain);

        // Iterate over the tasks
        for (Task task : tasks) {
            // Inflate the task_item layout
            View taskView = LayoutInflater.from(MainActivity.this).inflate(R.layout.task_item_main, tasksLinearLayout, false);

            // Set the task details
            TextView taskTitle = taskView.findViewById(R.id.task_title);
            taskTitle.setText(task.getDescription());
            TextView taskStartTime = taskView.findViewById(R.id.task_start_time);
            taskStartTime.setText(task.getStartTime());
            TextView taskEndTime = taskView.findViewById(R.id.task_end_time);
            taskEndTime.setText(task.getEndTime());

            // Calculate the task duration
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startTime = null;
            Date endTime = null;
            try {
                startTime = timeFormat.parse(task.getStartTime());
                endTime = timeFormat.parse(task.getEndTime());
            } catch (ParseException e) {
                throw new RuntimeException("Error parsing time", e);
            }

            long durationMillis = endTime.getTime() - startTime.getTime();
            long durationHours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) - TimeUnit.HOURS.toMinutes(durationHours);

            // Format the duration string based on hours and minutes
            String durationText;
            if (durationHours > 0 && durationMinutes > 0) {
                durationText = durationHours + " hour" + (durationHours == 1 ? "" : "s") + " and " + durationMinutes + " minute" + (durationMinutes == 1 ? "" : "s");
            } else if (durationHours > 0) {
                durationText = durationHours + " hour" + (durationHours == 1 ? "" : "s");
            } else if (durationMinutes > 0) {
                durationText = durationMinutes + " minute" + (durationMinutes == 1 ? "" : "s");
            } else {
                durationText = "Less than a minute";
            }

            // Display the task duration
            TextView taskDuration = taskView.findViewById(R.id.task_duration);
            taskDuration.setText(durationText);

            long totalDurationMillis = endTime.getTime() - startTime.getTime();

            // Set up the progress bar
            final ProgressBar taskProgressBar = taskView.findViewById(R.id.task_progress_bar);

            // Calculate the final start time based on the current time and the task's start time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, startTime.getHours());
            calendar.set(Calendar.MINUTE, startTime.getMinutes());
            Date finalStartTime = calendar.getTime();

            final long finalTotalDurationMillis = totalDurationMillis;

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    long elapsedMillis = currentTimeMillis - finalStartTime.getTime();

                    // Guard against negative elapsed time
                    if (elapsedMillis < 0) {
                        elapsedMillis = 0;
                    } else if (elapsedMillis > finalTotalDurationMillis) {
                        elapsedMillis = finalTotalDurationMillis;
                    }

                    // Improved precision for progress calculation
                    int progress = (int) ((elapsedMillis * 100.0) / finalTotalDurationMillis);

                    // Update the progress bar
                    taskProgressBar.setProgress(progress);
                    
                    // Schedule the next update only if we're within the task duration
                    if (elapsedMillis < finalTotalDurationMillis) {
                        handler.postDelayed(this, 1000); // Update every second
                    } else {
                        // If the task is complete, no need to update the progress bar anymore
                        taskProgressBar.setProgress(100);
                    }
                }
            };


            // Start the updates immediately
            handler.post(runnable);

            // Set the color of the CardView based on the color of the task
            androidx.cardview.widget.CardView cardView = taskView.findViewById(R.id.task_card_view); // Replace with the actual id of your CardView
            cardView.setCardBackgroundColor(Color.parseColor(task.getColor())); // Parse the color string to an actual color

            // Add the task view to the LinearLayout
            tasksLinearLayout.addView(taskView);

        }
        // Set up the RecyclerView
        BottomNavigationView bottomNav = findViewById(R.id.navigation_bar);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.navigation_tasks) {
                    intent = new Intent(MainActivity.this, TasksActivity.class);
                    intent.putExtra("userEmail", userEmail);
                } else if (item.getItemId() == R.id.navigation_user) {
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("userEmail", userEmail);
                } else if (item.getItemId() == R.id.navigation_settings) {
                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                }
                if (intent != null) {
                    startActivity(intent);

                }
                return true; // indicate that we handled the event
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
