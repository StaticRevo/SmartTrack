package com.example.smarttrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.HashMap;
import java.util.Map;
import com.example.smarttrack.MainActivity;
import com.example.smarttrack.TasksActivity;
import com.example.smarttrack.ProfileActivity;
import com.example.smarttrack.SettingsActivity;

public class NavigationUtils {

    public static void setupBottomNavigation(BottomNavigationView bottomNav, Context context, Class<?> currentActivityClass) {
        Map<Integer, Class<?>> activityMap = new HashMap<>();
        activityMap.put(R.id.navigation_home, MainActivity.class);
        activityMap.put(R.id.navigation_tasks, TasksActivity.class);
        activityMap.put(R.id.navigation_user, ProfileActivity.class);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Class<?> activityClass = activityMap.get(item.getItemId());

                if (activityClass != null && !activityClass.equals(currentActivityClass)) {
                    Intent intent = new Intent(context, activityClass);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    return true;
                }

                return false;
            }
        });
    }
}