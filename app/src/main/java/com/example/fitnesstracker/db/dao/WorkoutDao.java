package com.example.fitnesstracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.fitnesstracker.db.entities.Workout;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    void insertWorkout(Workout workout);

    @Query("SELECT * FROM workouts WHERE timestamp >= :timestamp")
    List<Workout> getWorkoutsAfter(long timestamp);

    @Query("SELECT * FROM workouts WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    LiveData<List<Workout>> getWorkoutsInDateRange(long startTime, long endTime);

    @Query("SELECT * FROM workouts WHERE (type IN (:types) OR :allTypes = 1) AND (description LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    LiveData<List<Workout>> searchWorkoutsCombined(String query, List<String> types, int allTypes);

    @Query("SELECT * FROM workouts WHERE (type IN (:types) OR :allTypes = 1) AND (description LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%') ORDER BY durationMillis DESC")
    LiveData<List<Workout>> searchWorkoutsCombinedSortedByDuration(String query, List<String> types, int allTypes);

    @Query("SELECT * FROM workouts WHERE (type IN (:types) OR :allTypes = 1) AND (description LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%') ORDER BY distanceMetres DESC")
    LiveData<List<Workout>> searchWorkoutsCombinedSortedByDistance(String query, List<String> types, int allTypes);

    @Query("SELECT * FROM workouts WHERE (type IN (:types) OR :allTypes = 1) AND (description LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%') ORDER BY averageSpeed DESC")
    LiveData<List<Workout>> searchWorkoutsCombinedSortedByAvgSpeed(String query, List<String> types, int allTypes);

    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    LiveData<List<Workout>> getAllWorkouts();

    @Query("SELECT * FROM workouts ORDER BY durationMillis DESC")
    LiveData<List<Workout>> getAllWorkoutsSortedByDuration();

    @Query("SELECT * FROM workouts ORDER BY distanceMetres DESC")
    LiveData<List<Workout>> getAllWorkoutsSortedByDistance();

    @Query("SELECT * FROM workouts ORDER BY averageSpeed DESC")
    LiveData<List<Workout>> getAllWorkoutsSortedByAvgSpeed();

    @Query("SELECT * FROM workouts WHERE description LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<Workout>> searchWorkouts(String query);

    @Query("SELECT * FROM workouts WHERE type IN (:types) ORDER BY timestamp DESC")
    LiveData<List<Workout>> getWorkoutsByTypes(List<String> types);

    @Query("SELECT * FROM workouts WHERE type IN (:types) ORDER BY durationMillis DESC")
    LiveData<List<Workout>> getWorkoutsByTypesSortedByDuration(List<String> types);

    @Query("SELECT * FROM workouts WHERE type IN (:types) ORDER BY distanceMetres DESC")
    LiveData<List<Workout>> getWorkoutsByTypesSortedByDistance(List<String> types);

    @Query("SELECT * FROM workouts WHERE type IN (:types) ORDER BY averageSpeed DESC")
    LiveData<List<Workout>> getWorkoutsByTypesSortedByAvgSpeed(List<String> types);
    
    @Query("DELETE FROM workouts")
    void deleteAll();

    @androidx.room.Delete
    void deleteWorkout(Workout workout);
}