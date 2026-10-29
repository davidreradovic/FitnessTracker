package com.example.fitnesstracker.db.converters;

import androidx.room.TypeConverter;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromRouteList(List<LatLng> route) {
        if (route == null) return null;
        Gson gson = new Gson();
        return gson.toJson(route);
    }

    @TypeConverter
    public static List<LatLng> toRouteList(String routeString) {
        if (routeString == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<LatLng>>() {}.getType();
        return gson.fromJson(routeString, type);
    }
}