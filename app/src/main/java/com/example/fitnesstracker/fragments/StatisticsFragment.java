package com.example.fitnesstracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.db.AppDatabase;
import com.example.fitnesstracker.db.entities.Workout;
import com.example.fitnesstracker.other.TrackingUtility;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import androidx.navigation.NavOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private BarChart barChart;
    private TextView tvCurrentWeek;
    private ImageButton btnPrevWeek, btnNextWeek;
    private Spinner spMetricFilter;
    
    private TextView tvDistanceProgress, tvTimeProgress;
    private android.widget.ProgressBar pbDistance, pbTime;

    private Calendar currentWeekCalendar;
    private List<Workout> allWorkouts = new ArrayList<>();
    private int currentMetricIndex = 0; // 0: Distance, 1: Duration, 2: Speed
    
    private final List<Long> dayTimestamps = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        
        barChart = view.findViewById(R.id.barChart);
        tvCurrentWeek = view.findViewById(R.id.tvCurrentWeek);
        btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);
        spMetricFilter = view.findViewById(R.id.spMetricFilter);
        
        tvDistanceProgress = view.findViewById(R.id.tvDistanceProgress);
        tvTimeProgress = view.findViewById(R.id.tvTimeProgress);
        pbDistance = view.findViewById(R.id.pbDistance);
        pbTime = view.findViewById(R.id.pbTime);
        
        currentWeekCalendar = Calendar.getInstance();
        currentWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        
        setupBarChart();
        setupListeners();
        subscribeToObservers();
        
        return view;
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setExtraOffsets(0, 0, 0, 10);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#33FFFFFF"));
        barChart.getAxisRight().setEnabled(false);

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                int index = (int) e.getX();
                if (index >= 0 && index < dayTimestamps.size()) {
                    long timestamp = dayTimestamps.get(index);
                    Bundle bundle = new Bundle();
                    bundle.putLong("selectedDate", timestamp);
                    
                    // Koristimo NavOptions da simuliramo klik na tab i izbegnemo slaganje backstack-a
                    NavOptions navOptions = new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.nav_graph, false)
                            .build();

                    androidx.navigation.fragment.NavHostFragment.findNavController(StatisticsFragment.this)
                            .navigate(R.id.nav_history, bundle, navOptions);
                }
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void setupListeners() {
        btnPrevWeek.setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            updateUI();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            updateUI();
        });

        spMetricFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMetricIndex = position;
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void subscribeToObservers() {
        AppDatabase.getDatabase(requireContext()).workoutDao().getAllWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            if (workouts != null) {
                allWorkouts = workouts;
                updateUI();
            }
        });
    }

    private void updateUI() {
        // Find start and end of current week
        Calendar cal = (Calendar) currentWeekCalendar.clone();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 7);
        long weekEnd = cal.getTimeInMillis();

        // Update week text
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());
        String weekRange = sdf.format(new Date(weekStart)) + " - " + sdf.format(new Date(weekEnd - 1000));
        tvCurrentWeek.setText(weekRange);

        // Filter workouts for this week
        List<Workout> weekWorkouts = new ArrayList<>();
        dayTimestamps.clear();
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.add(Calendar.DAY_OF_YEAR, -7); // Vratimo na početak sedmice
        for (int i = 0; i < 7; i++) {
            dayTimestamps.add(tempCal.getTimeInMillis());
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (Workout w : allWorkouts) {
            if (w.getTimestamp() >= weekStart && w.getTimestamp() < weekEnd) {
                weekWorkouts.add(w);
            }
        }

        updateGoalProgress(allWorkouts);
        setupChartData(weekWorkouts);
    }

    private void updateGoalProgress(List<Workout> workouts) {
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        float goalDistance = Float.parseFloat(prefs.getString("goal_distance", "5.0"));
        int goalTimeMinutes = Integer.parseInt(prefs.getString("goal_time", "30"));
        String unit = prefs.getString("units", "km");

        // Calculate today's totals
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        float todayDistanceMetres = 0f;
        long todayTimeMillis = 0L;

        for (Workout w : workouts) {
            if (w.getTimestamp() >= todayStart) {
                todayDistanceMetres += w.getDistanceMetres();
                todayTimeMillis += w.getDurationMillis();
            }
        }

        float todayDistConverted = TrackingUtility.getDistanceByUnit(todayDistanceMetres, unit);
        float todayTimeMinutes = todayTimeMillis / 1000f / 60f;

        // Update UI
        String distLabel = getString(R.string.distance_km_label).split("\\(")[0].trim();
        if ("mi".equals(unit)) distLabel = getString(R.string.distance_mi_label).split("\\(")[0].trim();
        
        String distText = String.format(Locale.getDefault(), "%s: %.1f / %.1f %s", distLabel, todayDistConverted, goalDistance, unit);
        tvDistanceProgress.setText(distText);
        pbDistance.setProgress((int) Math.min(100, (todayDistConverted / goalDistance) * 100));

        String timeLabel = getString(R.string.time_label).toLowerCase();
        timeLabel = timeLabel.substring(0, 1).toUpperCase() + timeLabel.substring(1);
        
        String timeText = String.format(Locale.getDefault(), "%s: %d / %d min", timeLabel, (int) todayTimeMinutes, goalTimeMinutes);
        tvTimeProgress.setText(timeText);
        pbTime.setProgress((int) Math.min(100, (todayTimeMinutes / goalTimeMinutes) * 100));
    }

    private void setupChartData(List<Workout> weekWorkouts) {
        // Group by day of week (0-6 for Mon-Sun)
        Map<Integer, Float> dayData = new HashMap<>();
        for (int i = 0; i < 7; i++) dayData.put(i, 0f);

        for (Workout w : weekWorkouts) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(w.getTimestamp());
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            // Convert Calendar DAY_OF_WEEK (Sun=1, Mon=2...) to 0-6 (Mon=0...Sun=6)
            int index = (dayOfWeek + 5) % 7;
            
            String unit = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("units", "km");
            
            float value = 0f;
            switch (currentMetricIndex) {
                case 0: // Distance
                    value = TrackingUtility.getDistanceByUnit(w.getDistanceMetres(), unit);
                    break;
                case 1: // Duration
                    value = w.getDurationMillis() / 1000f / 60f;
                    break;
                case 2: // Avg Speed
                    value = "mi".equals(unit) ? w.getAverageSpeed() * 0.621371f : w.getAverageSpeed();
                    break;
            }
            Float current = dayData.get(index);
            dayData.put(index, (current != null ? current : 0f) + value);
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Float val = dayData.get(i);
            entries.add(new BarEntry(i, val != null ? val : 0f));
        }

        String label = "";
        String unit = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("units", "km");
        switch (currentMetricIndex) {
            case 0: label = "Udaljenost (" + unit + ")"; break;
            case 1: label = "Trajanje (min)"; break;
            case 2: label = "Brzina (" + ("mi".equals(unit) ? "mph" : "km/h") + ")"; break;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        final String[] days = {"Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned"};
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < days.length) return days[index];
                return "";
            }
        });

        barChart.invalidate();
        barChart.animateY(500);
    }
}
