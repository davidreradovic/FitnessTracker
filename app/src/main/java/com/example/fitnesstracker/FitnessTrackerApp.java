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

        // VAŽNO: OSM traži specifičan format (NazivAplikacije/Verzija (Paket))
        String userAgent = "FitTrackApp/1.0 (" + getPackageName() + ")";
        config.setUserAgentValue(userAgent);

        File newCache = new File(getFilesDir(), "osmdroid_tiles_final");
        if (!newCache.exists()) {
            newCache.mkdirs();
        }
        config.setOsmdroidTileCache(newCache);
    }
}