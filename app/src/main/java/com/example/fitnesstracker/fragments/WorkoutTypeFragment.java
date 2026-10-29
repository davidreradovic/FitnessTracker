package com.example.fitnesstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.fitnesstracker.R;

public class WorkoutTypeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_type, container, false);

        CardView cvRunning = view.findViewById(R.id.cvRunning);
        CardView cvWalking = view.findViewById(R.id.cvWalking);
        CardView cvCycling = view.findViewById(R.id.cvCycling);

        cvRunning.setOnClickListener(v -> navigateToTracking(com.example.fitnesstracker.other.Constants.TYPE_RUNNING));
        cvWalking.setOnClickListener(v -> navigateToTracking(com.example.fitnesstracker.other.Constants.TYPE_WALKING));
        cvCycling.setOnClickListener(v -> navigateToTracking(com.example.fitnesstracker.other.Constants.TYPE_CYCLING));

        return view;
    }

    private void navigateToTracking(String type) {
        Bundle bundle = new Bundle();
        bundle.putString("activityType", type);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_nav_workout_type_to_nav_tracking, bundle);
    }
}