package com.example.fitnesstracker.other;

public class Constants {
    public static final String ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE";
    public static final String ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    
    public static final String NOTIFICATION_CHANNEL_ID = "tracking_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Tracking";
    public static final int NOTIFICATION_ID = 1;

    public static final long LOCATION_UPDATE_INTERVAL = 5000L;
    public static final long FASTEST_LOCATION_INTERVAL = 2000L;

    public static final int POLYLINE_COLOR = 0xFFFF0000; // Crvena
    public static final float POLYLINE_WIDTH = 8f;
    public static final float MAP_ZOOM = 15f;

    public static final String TYPE_RUNNING = "Running";
    public static final String TYPE_WALKING = "Walking";
    public static final String TYPE_CYCLING = "Cycling";
}