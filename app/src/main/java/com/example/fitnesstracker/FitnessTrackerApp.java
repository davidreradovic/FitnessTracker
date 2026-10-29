package com.example.fitnesstracker;

import android.app.Application;
import org.osmdroid.config.IConfigurationProvider;
import java.io.File;

public class FitnessTrackerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 1. OSMDroid Setup
        IConfigurationProvider config = org.osmdroid.config.Configuration.getInstance();
        config.setUserAgentValue(getPackageName());
        
        File newCache = new File(getFilesDir(), "osmdroid_tiles_final");
        if (!newCache.exists()) newCache.mkdirs();
        config.setOsmdroidTileCache(newCache);
        
        // Android i AppCompat sada automatski upravljaju jezikom 
        // zahvaljujući servisu dodatnom u Manifest.
    }
}
