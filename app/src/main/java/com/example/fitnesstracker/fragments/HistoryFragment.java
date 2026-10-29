package com.example.fitnesstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.appcompat.widget.SearchView;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.adapters.WorkoutAdapter;
import com.example.fitnesstracker.db.AppDatabase;
import com.example.fitnesstracker.db.entities.Workout;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private WorkoutAdapter workoutAdapter;
    private RecyclerView rvWorkouts;
    private SearchView searchView;
    private Spinner spFilter, spTypeFilter;
    
    private String currentQuery = "";
    private int currentFilterPosition = 0;
    private String currentTypeFilter = "ALL";
    private long selectedDate = -1L;

    private androidx.lifecycle.LiveData<List<Workout>> currentWorkoutsLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        if (getArguments() != null) {
            selectedDate = getArguments().getLong("selectedDate", -1L);
        }

        rvWorkouts = view.findViewById(R.id.rvWorkouts);
        searchView = view.findViewById(R.id.searchView);
        spFilter = view.findViewById(R.id.spFilter);
        spTypeFilter = view.findViewById(R.id.spTypeFilter);
        
        setupRecyclerView();
        setupFilters();
        
        subscribeToObservers();

        return view;
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter();
        rvWorkouts.setAdapter(workoutAdapter);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        workoutAdapter.setOnWorkoutClickListener(new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("workout", workout);
                NavHostFragment.findNavController(HistoryFragment.this)
                        .navigate(R.id.action_nav_history_to_workoutDetailFragment, bundle);
            }

            @Override
            public void onDeleteClick(Workout workout) {
                showDeleteConfirmationDialog(workout);
            }
        });
    }

    private void setupFilters() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                subscribeToObservers();
                return true;
            }
        });

        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterPosition = position;
                subscribeToObservers();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: currentTypeFilter = com.example.fitnesstracker.other.Constants.TYPE_RUNNING; break;
                    case 2: currentTypeFilter = com.example.fitnesstracker.other.Constants.TYPE_WALKING; break;
                    case 3: currentTypeFilter = com.example.fitnesstracker.other.Constants.TYPE_CYCLING; break;
                    default: currentTypeFilter = "ALL"; break;
                }
                subscribeToObservers();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDeleteConfirmationDialog(Workout workout) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_workout_title)
                .setMessage(R.string.delete_workout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase.getDatabase(requireContext()).workoutDao().deleteWorkout(workout);
                    }).start();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void subscribeToObservers() {
        if (currentWorkoutsLiveData != null) {
            currentWorkoutsLiveData.removeObservers(getViewLifecycleOwner());
        }

        com.example.fitnesstracker.db.dao.WorkoutDao dao = AppDatabase.getDatabase(requireContext()).workoutDao();
        
        if (selectedDate != -1L) {
            long dayEnd = selectedDate + (24 * 60 * 60 * 1000);
            currentWorkoutsLiveData = dao.getWorkoutsInDateRange(selectedDate, dayEnd);
        } else {
            List<String> queryTypes = new ArrayList<>();
            int allTypes = currentTypeFilter.equals("ALL") ? 1 : 0;
            if (allTypes == 0) {
                queryTypes.add(currentTypeFilter);
                if (com.example.fitnesstracker.other.Constants.TYPE_RUNNING.equals(currentTypeFilter)) queryTypes.add("Trčanje");
                if (com.example.fitnesstracker.other.Constants.TYPE_WALKING.equals(currentTypeFilter)) queryTypes.add("Hodanje");
                if (com.example.fitnesstracker.other.Constants.TYPE_CYCLING.equals(currentTypeFilter)) queryTypes.add("Biciklizam");
            }

            String query = currentQuery != null ? currentQuery.trim() : "";

            switch (currentFilterPosition) {
                case 1: // Trajanje
                    currentWorkoutsLiveData = dao.searchWorkoutsCombinedSortedByDuration(query, queryTypes, allTypes);
                    break;
                case 2: // Udaljenost
                    currentWorkoutsLiveData = dao.searchWorkoutsCombinedSortedByDistance(query, queryTypes, allTypes);
                    break;
                case 3: // Brzina
                    currentWorkoutsLiveData = dao.searchWorkoutsCombinedSortedByAvgSpeed(query, queryTypes, allTypes);
                    break;
                default: // Datum
                    currentWorkoutsLiveData = dao.searchWorkoutsCombined(query, queryTypes, allTypes);
                    break;
            }
        }

        currentWorkoutsLiveData.observe(getViewLifecycleOwner(), workouts -> {
            workoutAdapter.submitList(workouts);
        });
    }

    @Override
    public void onDestroyView() {
        // Poništavamo filter čim se pogled uništi (prelazak na drugu karticu)
        if (getArguments() != null) {
            getArguments().putLong("selectedDate", -1L);
        }
        selectedDate = -1L;
        super.onDestroyView();
    }
}
