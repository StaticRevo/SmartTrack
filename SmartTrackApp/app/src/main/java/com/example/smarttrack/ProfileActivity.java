package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {

    private TextView currentUsernameTextView;
    private TextView currentEmailTextView;
    private TextView currentPasswordTextView;
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        currentUsernameTextView = findViewById(R.id.currentUsername);
        currentEmailTextView = findViewById(R.id.currentEmail);
        currentPasswordTextView = findViewById(R.id.currentPassword);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Fetch the current user information from the database
        Intent intent = getIntent();
        String currentEmail = intent.getStringExtra("userEmail");
        String currentUsername = dbHelper.getUsername(currentEmail);
        String currentPassword = dbHelper.getPassword(currentUsername);

        // Display the current user information in the TextViews
        currentUsernameTextView.setText("Username: " + currentUsername);
        currentEmailTextView.setText("Email: " + currentEmail);
        currentPasswordTextView.setText("Password: " + currentPassword);

        findViewById(R.id.SaveChanges).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from the EditTexts
                String newUsername = usernameEditText.getText().toString();
                String newPassword = passwordEditText.getText().toString();

                // Create final copies of the variables
                final String finalCurrentUsername;
                final String finalCurrentPassword;

                if(newUsername != null && !newUsername.isEmpty()) {
                    finalCurrentUsername = newUsername;
                } else {
                    finalCurrentUsername = currentUsername;
                }
                if(newPassword != null && !newPassword.isEmpty()) {
                    finalCurrentPassword = newPassword;
                } else {
                    finalCurrentPassword = currentPassword;
                }
                dbHelper.updateUserInformation(currentEmail, finalCurrentUsername, finalCurrentPassword);

                Toast.makeText(ProfileActivity.this, "User information updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        BottomNavigationView bottomNav = findViewById(R.id.navigation_bar);
        bottomNav.setSelectedItemId(R.id.navigation_user);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.navigation_home) {
                    intent = new Intent(ProfileActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.navigation_tasks) {
                    intent = new Intent(ProfileActivity.this, TasksActivity.class);
                } else if (item.getItemId() == R.id.navigation_settings) {
                    intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);;
                    //startActivity(intent);
                    // overridePendingTransition(0, 0);
                    // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                }
                return true;
            }
        });
    }
}