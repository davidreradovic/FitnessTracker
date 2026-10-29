package com.example.fitnesstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.db.entities.Workout;
import com.example.fitnesstracker.other.Constants;
import com.example.fitnesstracker.other.TrackingUtility;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkoutDetailFragment extends Fragment {

    private MapView mapView;
    private Workout workout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_detail, container, false);
        
        workout = (Workout) getArguments().getSerializable("workout");
        
        mapView = view.findViewById(R.id.mapViewDetail);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
            
        drawPath();
        setupUI(view);

        return view;
    }

    private void setupUI(View view) {
        TextView tvType = view.findViewById(R.id.tvDetailType);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvDistance = view.findViewById(R.id.tvDetailDistance);
        TextView tvDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvSpeed = view.findViewById(R.id.tvDetailSpeed);
        TextView tvDescription = view.findViewById(R.id.tvDetailDescription);

        String type = workout.getType();
        if ("Trčanje".equals(type)) type = com.example.fitnesstracker.other.Constants.TYPE_RUNNING;
        if ("Hodanje".equals(type)) type = com.example.fitnesstracker.other.Constants.TYPE_WALKING;
        if ("Biciklizam".equals(type)) type = com.example.fitnesstracker.other.Constants.TYPE_CYCLING;

        tvType.setText(TrackingUtility.getTranslatedType(type, requireContext()));
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(workout.getTimestamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        tvDate.setText(dateFormat.format(calendar.getTime()));

        String unit = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("units", "km");
        float displayDistance = TrackingUtility.getDistanceByUnit(workout.getDistanceMetres(), unit);
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f", displayDistance));
        
        tvDuration.setText(TrackingUtility.getFormattedStopWatchTime(workout.getDurationMillis()));
        
        float displaySpeed = "mi".equals(unit) ? workout.getAverageSpeed() * 0.621371f : workout.getAverageSpeed();
        tvSpeed.setText(String.format(Locale.getDefault(), "%.1f", displaySpeed));
        
        String desc = workout.getDescription();
        tvDescription.setText(desc == null || desc.isEmpty() ? "Nema unesenog opisa." : desc);
    }

    private void drawPath() {
        org.osmdroid.views.overlay.CopyrightOverlay copyrightOverlay = new org.osmdroid.views.overlay.CopyrightOverlay(requireContext());
        mapView.getOverlays().add(copyrightOverlay);

        if (workout.getRoute() == null || workout.getRoute().isEmpty()) return;
        
        List<com.google.android.gms.maps.model.LatLng> route = workout.getRoute();
        Polyline line = new Polyline();
        line.getOutlinePaint().setColor(Constants.POLYLINE_COLOR);
        line.getOutlinePaint().setStrokeWidth(Constants.POLYLINE_WIDTH);

        List<GeoPoint> geoPoints = new ArrayList<>();
        for (com.google.android.gms.maps.model.LatLng latLng : route) {
            geoPoints.add(new GeoPoint(latLng.latitude, latLng.longitude));
        }
        line.setPoints(geoPoints);
        mapView.getOverlays().add(line);
        
        // Zoom to route
        GeoPoint startPoint = geoPoints.get(0);
        mapView.getController().setZoom(Constants.MAP_ZOOM);
        mapView.getController().setCenter(startPoint);
        mapView.invalidate();
    }

    @Override public void onResume() { super.onResume(); if(mapView != null) mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if(mapView != null) mapView.onPause(); }
    @Override public void onDestroyView() { super.onDestroyView(); }
}