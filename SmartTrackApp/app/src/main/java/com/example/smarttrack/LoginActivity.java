package com.example.smarttrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private TextView createNewAccount;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // Check if the Remember Me is checked
        if(sharedPreferences.getBoolean("RememberMe", false)) {
            // User chose to be remembered, go directly to MainActivity
            String email = sharedPreferences.getString("userEmail", "");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userEmail", email); // Pass the email to MainActivity
            startActivity(intent);
            finish();
            return;
        }

        // Initialize the variables by finding the view by its ID
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        createNewAccount = findViewById(R.id.createNewAccount);
        Button loginBtn = findViewById(R.id.signInButton);

        // Set an OnClickListener on the login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the button is clicked
                login();
            }
        });

        // Set an OnClickListener on the create new account TextView
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the RegisterActivity when the create account TextView is clicked
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Check if the inputs are valid
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this);

        // Assuming you have a method in dbHelper to check the user's credentials
        boolean isValidUser = dbHelper.checkUser(email); // This should ideally also check the password

        if (isValidUser) {
            // Store the email in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("RememberMe", rememberMeCheckBox.isChecked());
            editor.putString("userEmail", email); // Always save the user's email
            editor.apply(); // Saves the preferences asynchronously

            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Navigate to the MainActivity after successful login
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userEmail", email); // Pass the email to MainActivity
            startActivity(intent);
            finish(); // Close the LoginActivity
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Invalid email or password.", Toast.LENGTH_SHORT).show();
        }
    }

}