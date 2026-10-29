package com.example.fitnesstracker;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupNavigation();
        checkDailyReminder();
    }

    private void checkDailyReminder() {
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("daily_reminder", true)) return;

        // Check if there are any workouts today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        new Thread(() -> {
            java.util.List<com.example.fitnesstracker.db.entities.Workout> workouts = 
                com.example.fitnesstracker.db.AppDatabase.getDatabase(this).workoutDao().getWorkoutsAfter(todayStart);
            
            if (workouts.isEmpty()) {
                showReminderNotification();
            }
        }).start();
    }

    private void showReminderNotification() {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        String channelId = "daily_reminder_channel";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId, "Podsetnici", android.app.NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_foreground_padded)
                .setContentTitle(getString(R.string.reminder_title))
                .setContentText(getString(R.string.reminder_msg))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(100, builder.build());
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
}
