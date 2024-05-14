package com.example.smarttrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize the EditTexts
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        TextView alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);

        // Initialize the Button
        Button registerButton = findViewById(R.id.registerButton);

        // Set the OnClickListener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the register method when the button is clicked
                register();
            }
        });
        // Set an OnClickListener on the create new account TextView
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the RegisterActivity when the create account TextView is clicked
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void register() {
        // Extract the text from the EditTexts
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Check if the inputs are valid
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialising the DatabaseHelper
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Check if the user already exists
        if (dbHelper.checkUser(email)) {
            Toast.makeText(this, "User already exists. Please try with a different email.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean insertSuccess = dbHelper.addUser(email, username, password);
            if (insertSuccess) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                // Optionally start LoginActivity after registration or handle as needed
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close the RegisterActivity once done
            } else {
                Toast.makeText(this, "Registration failed for an unknown reason.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Log the exception to console or log files
            e.printStackTrace();
        }
    }
}