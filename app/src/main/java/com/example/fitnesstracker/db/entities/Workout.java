package com.example.fitnesstracker.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

@Entity(tableName = "workouts")
public class Workout implements java.io.Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String type; // trčanje, hodanje, biciklizam
    private long timestamp; // datum i vrijeme
    private long durationMillis;
    private float distanceMetres;
    private float averageSpeed;
    private String description;
    private List<LatLng> route; // lista koordinata rute

    public Workout(String type, long timestamp, long durationMillis, float distanceMetres, float averageSpeed, String description, List<LatLng> route) {
        this.type = type;
        this.timestamp = timestamp;
        this.durationMillis = durationMillis;
        this.distanceMetres = distanceMetres;
        this.averageSpeed = averageSpeed;
        this.description = description;
        this.route = route;
    }

    // Geteri i seteri (neophodni za Room)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public long getDurationMillis() { return durationMillis; }
    public float getDistanceMetres() { return distanceMetres; }
    public float getAverageSpeed() { return averageSpeed; }
    public String getDescription() { return description; }
    public List<LatLng> getRoute() { return route; }
}