package com.example.fitnesstracker.fragments;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.other.Constants;
import com.example.fitnesstracker.other.TrackingUtility;
import com.example.fitnesstracker.services.TrackingService;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.NotificationManager;

public class TrackingFragment extends Fragment {

    private Button btnToggleRun, btnFinishRun;
    private TextView tvTimer, tvDistance, tvSpeed, tvDistanceLabel, tvSpeedLabel;
    private android.widget.EditText etDescription;
    private android.view.View inputLayout;
    
    private MapView mapView;
    
    private boolean isTracking = false;
    private String activityType = com.example.fitnesstracker.other.Constants.TYPE_RUNNING;
    private List<List<com.google.android.gms.maps.model.LatLng>> pathPoints = new ArrayList<>();
    private long curTimeInMillis = 0L;
    private List<Polyline> polylines = new ArrayList<>();
    private MyLocationNewOverlay locationOverlay;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                
                if (allGranted) {
                    sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE);
                } else {
                    android.widget.Toast.makeText(requireContext(), 
                        getString(R.string.permissions_required), android.widget.Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);

        btnToggleRun = view.findViewById(R.id.btnToggleRun);
        btnFinishRun = view.findViewById(R.id.btnFinishRun);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvSpeed = view.findViewById(R.id.tvSpeed);
        tvDistanceLabel = view.findViewById(R.id.tvDistanceLabel);
        tvSpeedLabel = view.findViewById(R.id.tvSpeedLabel);
        etDescription = view.findViewById(R.id.etDescription);
        inputLayout = view.findViewById(R.id.inputLayout);
        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        
        // Postavljamo podrazumevani centar (Banja Luka)
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(new org.osmdroid.util.GeoPoint(44.7722, 17.1910));
        
        setupLocationOverlay();
        addAllPolylines();
        
        if (getArguments() != null) {
            activityType = getArguments().getString("activityType", com.example.fitnesstracker.other.Constants.TYPE_RUNNING);
        }

        btnToggleRun.setOnClickListener(v -> toggleRun());
        btnFinishRun.setOnClickListener(v -> finishRun());

        subscribeToObservers();

        return view;
    }



    private void subscribeToObservers() {
        TrackingService.isTracking.observe(getViewLifecycleOwner(), tracking -> {
            updateTracking(tracking);
        });

        TrackingService.pathPoints.observe(getViewLifecycleOwner(), pathPoints -> {
            this.pathPoints = pathPoints;
            if (pathPoints != null) {
                if (pathPoints.isEmpty()) {
                    mapView.getOverlays().clear();
                    addCopyrightOverlay();
                    if (locationOverlay != null) mapView.getOverlays().add(locationOverlay);
                    mapView.invalidate();
                } else {
                    addLatestPolyline();
                    moveCameraToUser();
                }
                
                float distanceMetres = 0f;
                for (List<com.google.android.gms.maps.model.LatLng> polyline : pathPoints) {
                    distanceMetres += TrackingUtility.calculateDistance(polyline);
                }
                
                String unit = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("units", "km");
                float displayDistance = TrackingUtility.getDistanceByUnit(distanceMetres, unit);
                
                String label = "mi".equals(unit) ? getString(R.string.distance_mi_label) : getString(R.string.distance_km_label);
                tvDistanceLabel.setText(label);
                
                String speedLabel = "mi".equals(unit) ? getString(R.string.speed_mph_label) : getString(R.string.speed_kmh_label);
                tvSpeedLabel.setText(speedLabel);
                
                String distanceStr = String.format(Locale.getDefault(), "%.2f", displayDistance);
                tvDistance.setText(distanceStr);
            }
        });

        TrackingService.curSpeed.observe(getViewLifecycleOwner(), speed -> {
            if (isTracking) {
                String speedStr = String.format(Locale.getDefault(), "%.1f", speed);
                tvSpeed.setText(speedStr);
            }
        });

        TrackingService.timeRunInMillis.observe(getViewLifecycleOwner(), ms -> {
            curTimeInMillis = ms;
            String formattedTime = TrackingUtility.getFormattedStopWatchTime(ms);
            tvTimer.setText(formattedTime);
            updateTracking(isTracking);
        });
    }

    private void addCopyrightOverlay() {
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(requireContext());
        mapView.getOverlays().add(copyrightOverlay);
    }

    private void setupLocationOverlay() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        locationOverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(locationOverlay);
    }

    private void moveCameraToUser() {
        if (!pathPoints.isEmpty() && !pathPoints.get(pathPoints.size() - 1).isEmpty()) {
            com.google.android.gms.maps.model.LatLng lastLatLng = pathPoints.get(pathPoints.size() - 1).get(pathPoints.get(pathPoints.size() - 1).size() - 1);
            GeoPoint geoPoint = new GeoPoint(lastLatLng.latitude, lastLatLng.longitude);
            mapView.getController().animateTo(geoPoint);
            mapView.getController().setZoom(Constants.MAP_ZOOM);
        }
    }

    private void addAllPolylines() {
        mapView.getOverlays().clear();
        addCopyrightOverlay();
        if (locationOverlay != null) mapView.getOverlays().add(locationOverlay);
        for (List<com.google.android.gms.maps.model.LatLng> polylinePoints : pathPoints) {
            Polyline line = new Polyline();
            line.getOutlinePaint().setColor(Constants.POLYLINE_COLOR);
            line.getOutlinePaint().setStrokeWidth(Constants.POLYLINE_WIDTH);
            
            List<GeoPoint> geoPoints = new ArrayList<>();
            for (com.google.android.gms.maps.model.LatLng latLng : polylinePoints) {
                geoPoints.add(new GeoPoint(latLng.latitude, latLng.longitude));
            }
            line.setPoints(geoPoints);
            mapView.getOverlays().add(line);
        }
        mapView.invalidate();
    }

    private void addLatestPolyline() {
        if (!pathPoints.isEmpty() && pathPoints.get(pathPoints.size() - 1).size() > 1) {
            List<com.google.android.gms.maps.model.LatLng> lastPolylinePoints = pathPoints.get(pathPoints.size() - 1);
            com.google.android.gms.maps.model.LatLng preLastLatLng = lastPolylinePoints.get(lastPolylinePoints.size() - 2);
            com.google.android.gms.maps.model.LatLng lastLatLng = lastPolylinePoints.get(lastPolylinePoints.size() - 1);

            Polyline line = new Polyline();
            line.getOutlinePaint().setColor(Constants.POLYLINE_COLOR);
            line.getOutlinePaint().setStrokeWidth(Constants.POLYLINE_WIDTH);
            
            line.addPoint(new GeoPoint(preLastLatLng.latitude, preLastLatLng.longitude));
            line.addPoint(new GeoPoint(lastLatLng.latitude, lastLatLng.longitude));
            
            mapView.getOverlays().add(line);
            mapView.invalidate();
        }
    }

    private void updateTracking(boolean isTracking) {
        this.isTracking = isTracking;
        if (!isTracking) {
            if (curTimeInMillis > 0L) {
                btnToggleRun.setText(getString(R.string.resume));
                btnFinishRun.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.VISIBLE);
            } else {
                btnToggleRun.setText(getString(R.string.start));
                btnFinishRun.setVisibility(View.GONE);
                inputLayout.setVisibility(View.GONE);
            }
            btnToggleRun.setBackgroundColor(getResources().getColor(R.color.blue_primary));
            tvSpeed.setText("0.0");
        } else {
            btnToggleRun.setText(getString(R.string.stop));
            btnToggleRun.setBackgroundColor(getResources().getColor(R.color.error_red));
            btnFinishRun.setVisibility(View.GONE);
            inputLayout.setVisibility(View.GONE);
        }
    }

    private void toggleRun() {
        if (isTracking) {
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE);
        } else {
            if (TrackingUtility.hasLocationPermissions(requireContext())) {
                sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE);
            } else {
                requestPermissions();
            }
        }
    }

    private void finishRun() {
        showSaveConfirmationDialog();
    }

    private void showSaveConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.save_workout_title)
                .setMessage(R.string.save_workout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    saveWorkoutToDb();
                    etDescription.setText("");
                    sendCommandToService(Constants.ACTION_STOP_SERVICE);
                    androidx.navigation.fragment.NavHostFragment.findNavController(this).navigateUp();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    sendCommandToService(Constants.ACTION_STOP_SERVICE);
                    dialog.dismiss();
                })
                .show();
    }

    private void saveWorkoutToDb() {
        java.util.List<java.util.List<com.google.android.gms.maps.model.LatLng>> allPoints = TrackingService.pathPoints.getValue();
        if (allPoints == null) return;
        
        java.util.List<com.google.android.gms.maps.model.LatLng> flattenedList = new java.util.ArrayList<>();
        float totalDistanceMetres = 0f;
        for (java.util.List<com.google.android.gms.maps.model.LatLng> polyline : allPoints) {
            flattenedList.addAll(polyline);
            totalDistanceMetres += TrackingUtility.calculateDistance(polyline);
        }
        
        float avgSpeed = 0f;
        if (curTimeInMillis > 0) {
            avgSpeed = (totalDistanceMetres / 1000f) / (curTimeInMillis / 1000f / 3600f);
        }
        
        long timestamp = System.currentTimeMillis();
        String description = etDescription.getText().toString();

        com.example.fitnesstracker.db.entities.Workout workout = new com.example.fitnesstracker.db.entities.Workout(
                activityType,
                timestamp,
                curTimeInMillis,
                totalDistanceMetres,
                avgSpeed,
                description,
                flattenedList
        );
        
        // Uzimamo Application Context pre ulaska u drugu nit (Thread)
        android.content.Context appContext = requireContext().getApplicationContext();

        // Asinhrono čuvanje u bazu
        new Thread(() -> {
            com.example.fitnesstracker.db.AppDatabase db = com.example.fitnesstracker.db.AppDatabase.getDatabase(appContext);
            db.workoutDao().insertWorkout(workout);
            
            // Provera ciljeva nakon čuvanja koristeći stabilni context
            checkGoalAchievement(db, appContext);
        }).start();
    }

    private void checkGoalAchievement(com.example.fitnesstracker.db.AppDatabase db, android.content.Context context) {
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("notifications", true)) return;

        float goalDistance = Float.parseFloat(prefs.getString("goal_distance", "5.0"));
        int goalTimeMinutes = Integer.parseInt(prefs.getString("goal_time", "30"));
        String unit = prefs.getString("units", "km");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        List<com.example.fitnesstracker.db.entities.Workout> todayWorkouts = db.workoutDao().getWorkoutsAfter(todayStart);
        
        float totalDistanceMetres = 0f;
        long totalTimeMillis = 0L;
        for (com.example.fitnesstracker.db.entities.Workout w : todayWorkouts) {
            totalDistanceMetres += w.getDistanceMetres();
            totalTimeMillis += w.getDurationMillis();
        }

        float totalDistConverted = TrackingUtility.getDistanceByUnit(totalDistanceMetres, unit);
        float totalTimeMinutes = totalTimeMillis / 1000f / 60f;

        if (totalDistConverted >= goalDistance || totalTimeMinutes >= goalTimeMinutes) {
            showGoalCelebrationNotification(context);
        }
    }

    private void showGoalCelebrationNotification(android.content.Context context) {
        android.app.NotificationManager notificationManager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        String channelId = "goal_channel";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId, "Ciljevi", android.app.NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_foreground_padded)
                .setContentTitle(context.getString(R.string.goal_congrats_title))
                .setContentText(context.getString(R.string.goal_congrats_msg))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(200, builder.build());
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            });
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void sendCommandToService(String action) {
        Intent intent = new Intent(requireContext(), TrackingService.class);
        intent.setAction(action);
        requireContext().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (locationOverlay != null) locationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
        if (locationOverlay != null) locationOverlay.disableMyLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}