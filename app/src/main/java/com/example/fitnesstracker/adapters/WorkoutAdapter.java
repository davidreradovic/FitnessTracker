package com.example.fitnesstracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.db.entities.Workout;
import com.example.fitnesstracker.other.TrackingUtility;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onDeleteClick(Workout workout);
    }

    private OnWorkoutClickListener listener;

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    private final DiffUtil.ItemCallback<Workout> diffCallback = new DiffUtil.ItemCallback<Workout>() {
        @Override
        public boolean areItemsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
            return oldItem.hashCode() == newItem.hashCode();
        }
    };

    private final AsyncListDiffer<Workout> differ = new AsyncListDiffer<>(this, diffCallback);

    public void submitList(List<Workout> list) {
        differ.submitList(list);
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkoutViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_workout,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = differ.getCurrentList().get(position);
        holder.bind(workout);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWorkoutClick(workout);
        });
        
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(workout);
        });
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvDistance, tvDuration, tvAvgSpeed;
        android.widget.ImageView ivDelete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvAvgSpeed = itemView.findViewById(R.id.tvAvgSpeed);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(Workout workout) {
            String type = workout.getType();
            if ("Trčanje".equals(type)) type = com.example.fitnesstracker.other.Constants.TYPE_RUNNING;
            if ("Hodanje".equals(type)) type = com.example.fitnesstracker.other.Constants.TYPE_WALKING;
            if ("Biciklizam".equals(type)) type = com.example.fitnesstracker.other.Constants.TYPE_CYCLING;
            
            tvType.setText(TrackingUtility.getTranslatedType(type, itemView.getContext()));
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(workout.getTimestamp());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            tvDate.setText(dateFormat.format(calendar.getTime()));

            String unit = androidx.preference.PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getString("units", "km");
            float displayDistance = TrackingUtility.getDistanceByUnit(workout.getDistanceMetres(), unit);
            String distance = String.format(Locale.getDefault(), "%.2f %s", displayDistance, unit);
            tvDistance.setText(distance);

            tvDuration.setText(TrackingUtility.getFormattedStopWatchTime(workout.getDurationMillis()));

            float displaySpeed = "mi".equals(unit) ? workout.getAverageSpeed() * 0.621371f : workout.getAverageSpeed();
            String speedUnit = "mi".equals(unit) ? "mph" : "km/h";
            String speed = String.format(Locale.getDefault(), "%.1f %s", displaySpeed, speedUnit);
            tvAvgSpeed.setText(speed);
        }
    }
}