package com.example.fitnesstracker.other;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.content.Context;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import com.example.fitnesstracker.R;

public class TrackingUtility {

    public static boolean hasLocationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    public static String getFormattedStopWatchTime(long ms) {
        long milliseconds = ms;
        long hours = (milliseconds / (1000 * 60 * 60));
        milliseconds -= hours * (1000 * 60 * 60);
        long minutes = (milliseconds / (1000 * 60));
        milliseconds -= minutes * (1000 * 60);
        long seconds = (milliseconds / 1000);
        
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    public static float calculateDistance(List<LatLng> path) {
        float distance = 0f;
        for (int i = 0; i < path.size() - 1; i++) {
            LatLng pos1 = path.get(i);
            LatLng pos2 = path.get(i + 1);
            
            float[] results = new float[1];
            Location.distanceBetween(
                    pos1.latitude, pos1.longitude,
                    pos2.latitude, pos2.longitude,
                    results
            );
            distance += results[0];
        }
        return distance; // Vraća metre
    }

    public static float getDistanceByUnit(float distanceMetres, String unit) {
        if ("mi".equals(unit)) {
            return distanceMetres * 0.000621371f;
        }
        return distanceMetres / 1000f;
    }

    public static String getTranslatedType(String type, Context context) {
        if (Constants.TYPE_RUNNING.equals(type)) return context.getString(R.string.running);
        if (Constants.TYPE_WALKING.equals(type)) return context.getString(R.string.walking);
        if (Constants.TYPE_CYCLING.equals(type)) return context.getString(R.string.cycling);
        return type;
    }
}