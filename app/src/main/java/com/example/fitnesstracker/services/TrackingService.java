package com.example.fitnesstracker.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnesstracker.MainActivity;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.other.Constants;
import com.example.fitnesstracker.other.TrackingUtility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TrackingService extends LifecycleService {

    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);
    public static MutableLiveData<List<List<LatLng>>> pathPoints = new MutableLiveData<>(new ArrayList<>());
    public static MutableLiveData<Long> timeRunInMillis = new MutableLiveData<>(0L);
    public static MutableLiveData<Long> timeRunInSeconds = new MutableLiveData<>(0L);
    public static MutableLiveData<Float> curSpeed = new MutableLiveData<>(0f);

    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean isFirstRun = true;
    
    private long timeStarted = 0L;
    private long lapTime = 0L;
    private long totalTime = 0L;
    private long lastSecondTimestamp = 0L;
    private boolean isTimerEnabled = false;

    private NotificationCompat.Builder notificationBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        postInitialValues();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        
        isTracking.observe(this, tracking -> {
            updateLocationTracking(tracking);
        });

        timeRunInSeconds.observe(this, seconds -> {
            boolean showUpdates = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications", true);
            if (showUpdates && !isFirstRun && notificationBuilder != null) {
                notificationBuilder.setContentText(TrackingUtility.getFormattedStopWatchTime(seconds * 1000L));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(Constants.NOTIFICATION_ID, notificationBuilder.build());
            }
        });
    }

    private void postInitialValues() {
        isTracking.postValue(false);
        pathPoints.postValue(new ArrayList<>());
        timeRunInMillis.postValue(0L);
        timeRunInSeconds.postValue(0L);
        curSpeed.postValue(0f);
    }

    private void addEmptyPolyline() {
        List<List<LatLng>> currentPoints = pathPoints.getValue();
        if (currentPoints != null) {
            currentPoints.add(new ArrayList<>());
            pathPoints.postValue(currentPoints);
        } else {
            List<List<LatLng>> newList = new ArrayList<>();
            newList.add(new ArrayList<>());
            pathPoints.postValue(newList);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Constants.ACTION_START_OR_RESUME_SERVICE:
                        if (isFirstRun) {
                            startForegroundService();
                            isFirstRun = false;
                        } else {
                            startTimer();
                        }
                        break;
                    case Constants.ACTION_PAUSE_SERVICE:
                        pauseService();
                        break;
                    case Constants.ACTION_STOP_SERVICE:
                        stopService();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startTimer() {
        addEmptyPolyline();
        isTracking.postValue(true);
        timeStarted = System.currentTimeMillis();
        isTimerEnabled = true;
        
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTimerEnabled) {
                    lapTime = System.currentTimeMillis() - timeStarted;
                    timeRunInMillis.postValue(totalTime + lapTime);
                    
                    Long currentMillis = timeRunInMillis.getValue();
                    if (currentMillis != null && currentMillis >= lastSecondTimestamp + 1000L) {
                        timeRunInSeconds.postValue(timeRunInSeconds.getValue() + 1);
                        lastSecondTimestamp += 1000L;
                    }
                    handler.postDelayed(this, 50L);
                }
            }
        });
    }
    
    private void pauseService() {
        isTracking.postValue(false);
        isTimerEnabled = false;
        totalTime += lapTime;
    }

    private void stopService() {
        isTracking.postValue(false);
        isTimerEnabled = false;
        isFirstRun = true;
        totalTime = 0L;
        lastSecondTimestamp = 0L;
        postInitialValues();
        stopForeground(true);
        stopSelf();
    }

    @SuppressLint("MissingPermission")
    private void updateLocationTracking(boolean isTracking) {
        if (isTracking) {
            LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, Constants.LOCATION_UPDATE_INTERVAL)
                    .setMinUpdateIntervalMillis(Constants.FASTEST_LOCATION_INTERVAL)
                    .build();
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (isTracking.getValue() != null && isTracking.getValue()) {
                for (Location location : locationResult.getLocations()) {
                    addPathPoint(location);
                    float speedKmH = location.getSpeed() * 3.6f;
                    curSpeed.postValue(speedKmH);
                }
            }
        }
    };

    private void addPathPoint(Location location) {
        if (location != null) {
            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            List<List<LatLng>> currentPoints = pathPoints.getValue();
            if (currentPoints != null && !currentPoints.isEmpty()) {
                currentPoints.get(currentPoints.size() - 1).add(pos);
                pathPoints.postValue(currentPoints);
            }
        }
    }

    private void startForegroundService() {
        startTimer();
        isTracking.postValue(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager);
        }

        notificationBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Fitness Tracker")
                .setContentText("00:00:00")
                .setContentIntent(getMainActivityPendingIntent());

        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent getMainActivityPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        );
        notificationManager.createNotificationChannel(channel);
    }
}