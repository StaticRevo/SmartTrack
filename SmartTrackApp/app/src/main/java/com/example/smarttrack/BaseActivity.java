package com.example.smarttrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smarttrack.LoginActivity;
import com.example.smarttrack.R;

public class BaseActivity extends AppCompatActivity {
    
    protected void setupProfileIcon() {
        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(BaseActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_sign_out) {
                        signOutUser();
                        return true;
                    }
                    return false;
                });
                popup.show();
            }
        });
    }

    protected void signOutUser() {
        Log.d("BaseActivity", "signOutUser: attempting to sign out");

        // Clear user session data in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // This clears all the data. Use editor.remove("key") if you want to remove specific data.
        editor.apply(); // Commit changes asynchronously. Use editor.commit() for synchronous.

        try {
            // Redirect to Login Activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Ensure this activity is closed so the user can't navigate back to it.
            Log.d("BaseActivity", "signOutUser: sign out successful, redirecting...");
        } catch (Exception e) {
            Log.e("BaseActivity", "signOutUser: failed to redirect to LoginActivity", e);
        }
    }

}
