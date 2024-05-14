package com.example.smarttrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Switch enableNotificationsSwitch;
    private Switch taskReminderNotificationSwitch;
    private Switch dailySummaryNotificationSwitch;
    private Switch notificationQuietHoursSwitch;
    private Switch themeSettingSwitch;

    private static final int ALARM_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize the switches
        enableNotificationsSwitch = findViewById(R.id.enableNotificationsSwitch);
        taskReminderNotificationSwitch = findViewById(R.id.taskReminderNotificationSwitch);
        dailySummaryNotificationSwitch = findViewById(R.id.dailySummaryNotificationSwitch);
        notificationQuietHoursSwitch = findViewById(R.id.notificationQuietHoursSwitch);
        themeSettingSwitch = findViewById(R.id.themeSettingSwitch);

        // Load the saved theme choice from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppSettingsPrefs", 0);
        boolean isEnableNotifications = prefs.getBoolean("EnableNotifications", false);
        boolean isTaskReminderNotification = prefs.getBoolean("TaskReminderNotification", false);
        boolean isDailySummaryNotification = prefs.getBoolean("DailySummaryNotification", false);
        boolean isNotificationQuietHours = prefs.getBoolean("NotificationQuietHours", false);
        boolean isDarkThemeEnabled = prefs.getBoolean("DarkTheme", false);
        themeSettingSwitch.setChecked(isDarkThemeEnabled);

        // Set the saved state to each switch
        themeSettingSwitch.setChecked(isDarkThemeEnabled);
        enableNotificationsSwitch.setChecked(isEnableNotifications);
        taskReminderNotificationSwitch.setChecked(isTaskReminderNotification);
        dailySummaryNotificationSwitch.setChecked(isDailySummaryNotification);
        notificationQuietHoursSwitch.setChecked(isNotificationQuietHours);


        enableNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the state of Enable Notifications setting
            prefs.edit().putBoolean("EnableNotifications", isChecked).apply();

            if (isChecked) {
                if (taskReminderNotificationSwitch.isChecked()) {
                    // Schedule the task reminder alarms
                    scheduleTaskReminderAlarms();
                }
                if (dailySummaryNotificationSwitch.isChecked()) {
                    // Schedule the daily summary alarms
                    scheduleDailySummaryNotifications();
                }
            } else {
                // Cancel all alarms
                cancelAlarms();
            }
        });

        taskReminderNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the state of Task Reminder Notification setting
            prefs.edit().putBoolean("TaskReminderNotification", isChecked).apply();
        });

        dailySummaryNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the state of Daily Summary Notification setting
            prefs.edit().putBoolean("DailySummaryNotification", isChecked).apply();
        });

        notificationQuietHoursSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the state of Notification Quiet Hours setting
            prefs.edit().putBoolean("NotificationQuietHours", isChecked).apply();
        });

        themeSettingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the theme preference
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("DarkTheme", isChecked);
            editor.apply();

            // Apply the theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getApplication().setTheme(R.style.AppTheme_Dark);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getApplication().setTheme(R.style.AppTheme);
            }

            // Show a message to indicate a restart is needed
            Toast.makeText(SettingsActivity.this, "Theme changed. Please restart the app to apply the changes.", Toast.LENGTH_LONG).show();
        });

        BottomNavigationView bottomNav = findViewById(R.id.navigation_bar);
        bottomNav.setSelectedItemId(R.id.navigation_settings);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.navigation_home) {
                    intent = new Intent(SettingsActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.navigation_tasks) {
                    intent = new Intent(SettingsActivity.this, TasksActivity.class);
                } else if (item.getItemId() == R.id.navigation_user) {
                    intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            }
        });
    }
    private void scheduleTaskReminderAlarms() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Fetch tasks for the current day
        String userEmail = getIntent().getStringExtra("userEmail"); // Get the email from the intent
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String currentDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        List<Task> tasks = dbHelper.getTasksForDate(userEmail, currentDate);

        // Iterate over the tasks
        for (Task task : tasks) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("title", "Task Reminder");
            intent.putExtra("content", "You have a task coming up in 10 minutes.");

            // Create a unique ID for the PendingIntent
            int requestCode = (task.getDescription() + task.getDate() + task.getStartTime()).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Parse the start time to a Date object
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            try {
                Date startTime = timeFormat.parse(task.getStartTime());

                // Subtract 10 minutes from the start time to get the alarm time
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime);
                calendar.add(Calendar.MINUTE, -10);

                // Convert the alarm time to milliseconds since epoch
                long triggerAtMillis = calendar.getTimeInMillis();

                // Schedule the alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    private void scheduleDailySummaryNotifications() {
        // Fetch the saved state of Daily Summary Notification setting
        SharedPreferences prefs = getSharedPreferences("AppSettingsPrefs", 0);
        boolean isDailySummaryNotification = prefs.getBoolean("DailySummaryNotification", false);

        // Only schedule the alarms if the Daily Summary Notification setting is enabled
        if (isDailySummaryNotification) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Fetch tasks for the current day
            String userEmail = getIntent().getStringExtra("userEmail"); // Get the email from the intent
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            String currentDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
            List<Task> tasks = dbHelper.getTasksForDate(userEmail, currentDate);

            // Create two Intents for the start and end of day notifications
            Intent startOfDayIntent = new Intent(this, AlarmReceiver.class);
            startOfDayIntent.putExtra("title", "Daily Summary");
            startOfDayIntent.putExtra("content", "You have " + tasks.size() + " tasks today.");

            Intent endOfDayIntent = new Intent(this, AlarmReceiver.class);
            endOfDayIntent.putExtra("title", "Daily Summary");
            // Assuming each task takes 1 hour, you can modify this as per your requirements
            endOfDayIntent.putExtra("content", "You spent " + tasks.size() + " hours on tasks today.");

            // Create unique IDs for the PendingIntents
            int startOfDayRequestCode = ("startOfDay" + currentDate).hashCode();
            int endOfDayRequestCode = ("endOfDay" + currentDate).hashCode();

            PendingIntent startOfDayPendingIntent = PendingIntent.getBroadcast(this, startOfDayRequestCode, startOfDayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent endOfDayPendingIntent = PendingIntent.getBroadcast(this, endOfDayRequestCode, endOfDayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set the start of day alarm to 8:00 AM and end of day alarm to 8:00 PM
            Calendar startOfDayCalendar = Calendar.getInstance();
            startOfDayCalendar.set(Calendar.HOUR_OF_DAY, 8);
            startOfDayCalendar.set(Calendar.MINUTE, 0);
            startOfDayCalendar.set(Calendar.SECOND, 0);

            Calendar endOfDayCalendar = Calendar.getInstance();
            endOfDayCalendar.set(Calendar.HOUR_OF_DAY, 20);
            endOfDayCalendar.set(Calendar.MINUTE, 0);
            endOfDayCalendar.set(Calendar.SECOND, 0);

            // Schedule the alarms
            alarmManager.set(AlarmManager.RTC_WAKEUP, startOfDayCalendar.getTimeInMillis(), startOfDayPendingIntent);
            alarmManager.set(AlarmManager.RTC_WAKEUP, endOfDayCalendar.getTimeInMillis(), endOfDayPendingIntent);
        }
    }
    private void cancelAlarms() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cancel the alarm
        alarmManager.cancel(pendingIntent);
    }
}