package com.example.smarttrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NewDb.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USERNAME ="username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_TASK_ID = "taskId";
    private static final String COLUMN_USER_EMAIL = "userEmail";
    private static final String COLUMN_TASK_DESC = "description";
    private static final String COLUMN_TASK_COLOR = "color";
    private static final String COLUMN_TASK_DATE = "task_date";
    private static final String COLUMN_TASK_TIME = "task_time";
    private static final String COLUMN_TASK_END_TIME = "task_end_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table with user_id as AUTOINCREMENT
        String createTableStatement = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Add this line
                COLUMN_EMAIL + " TEXT UNIQUE," + // Ensure email remains unique
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createTableStatement);

        // Tasks table
        String createTasksTableStatement = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USER_EMAIL + " TEXT," +
                COLUMN_TASK_DESC + " TEXT," +
                COLUMN_TASK_COLOR + " TEXT," +
                COLUMN_TASK_DATE + " TEXT," + // New column for date
                COLUMN_TASK_TIME + " TEXT," + // New column for time
                COLUMN_TASK_END_TIME + " TEXT," + // New column for end time
                "FOREIGN KEY (" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createTasksTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ...

        // Add new columns to the tasks table
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_DATE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_TIME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_END_TIME + " TEXT");
        }
    }

    public boolean addTask(String userEmail, String description, String color, String date, String time, String endTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_EMAIL, userEmail);
        contentValues.put(COLUMN_TASK_DESC, description);
        contentValues.put(COLUMN_TASK_COLOR, color);
        contentValues.put(COLUMN_TASK_DATE, date);
        contentValues.put(COLUMN_TASK_TIME, time);
        contentValues.put(COLUMN_TASK_END_TIME, endTime);
        try {
            long result = db.insertOrThrow(TABLE_TASKS, null, contentValues);
            db.close();
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to insert task", e);
            db.close();
            return false;
        }
    }
    public void deleteTask(String userEmail, String taskDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "userEmail = ? AND description = ?", new String[] {userEmail, taskDescription});
        db.close();
    }
    public boolean addUser(String email, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_USERNAME, username);
        contentValues.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        db.close();
        return result != -1; // returns true if insert is successful
    }

    public boolean checkUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_EMAIL + "=?", new String[]{email});
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }
    public String getUsername(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME}, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        String username = "User"; // Default username
        if (cursor != null && cursor.moveToFirst()) {
            username = cursor.getString(0);
        }
        Log.d("DatabaseHelper", "getUsername: email = " + email + ", username = " + username);
        cursor.close();
        db.close();
        return username;
    }
    public String getUserEmail(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        String email = null;
        if (cursor != null && cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return email;
    }

    public String getPassword(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        String password = null;
        if (cursor != null && cursor.moveToFirst()) {
            password = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return password;
    }

    public int getTaskCount(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " +
                COLUMN_USER_EMAIL + "=?", new String[]{userEmail});
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            db.close();
            return count;
        } else {
            db.close();
            if (cursor != null) {
                cursor.close();
            }
            return 0; // Return 0 if there are no tasks
        }
    }
    public int getTaskCountForDate(String userEmail, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " +
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_TASK_DATE + "=?", new String[]{userEmail, date});
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            db.close();
            return count;
        } else {
            db.close();
            if (cursor != null) {
                cursor.close();
            }
            return 0; // Return 0 if there are no tasks
        }
    }
    public List<Task> getTasksForDate(String userEmail, String date) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " +
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_TASK_DATE + "=? ORDER BY " + COLUMN_TASK_TIME, new String[]{userEmail, date});
        if (cursor.moveToFirst()) {
            do {
                int descIndex = cursor.getColumnIndex(COLUMN_TASK_DESC);
                int colorIndex = cursor.getColumnIndex(COLUMN_TASK_COLOR);
                int timeIndex = cursor.getColumnIndex(COLUMN_TASK_TIME);
                int endTimeIndex = cursor.getColumnIndex(COLUMN_TASK_END_TIME);

                if (descIndex == -1 || colorIndex == -1 || timeIndex == -1 || endTimeIndex == -1) {
                    throw new IllegalArgumentException("Column not found in the table");
                }

                String description = cursor.getString(descIndex);
                String color = cursor.getString(colorIndex);
                String time = cursor.getString(timeIndex);
                String endTime = cursor.getString(endTimeIndex);
                tasks.add(new Task(description, color, date, time, endTime));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }
    public void updateUserInformation(String currentEmail, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (newUsername != null && !newUsername.isEmpty()) {
            contentValues.put(COLUMN_USERNAME, newUsername);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            contentValues.put(COLUMN_PASSWORD, newPassword);
        }
        db.update(TABLE_USERS, contentValues, COLUMN_EMAIL + "=?", new String[]{currentEmail});
        db.close();
    }
    public boolean updateTask(Task task, String originalDescription, String originalDate, String originalStartTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TASK_DESC, task.getDescription());
        contentValues.put(COLUMN_TASK_COLOR, task.getColor());
        contentValues.put(COLUMN_TASK_DATE, task.getDate());
        contentValues.put(COLUMN_TASK_TIME, task.getStartTime());
        contentValues.put(COLUMN_TASK_END_TIME, task.getEndTime());
        int result = db.update(TABLE_TASKS, contentValues, COLUMN_TASK_DESC + "=? AND " + COLUMN_TASK_DATE + "=? AND " + COLUMN_TASK_TIME + "=?", new String[]{originalDescription, originalDate, originalStartTime});
        db.close();
        return result > 0;
    }
}
