package com.studenthub.ui.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studenthub.R;
import com.studenthub.data.local.entity.ClassEntity;

import java.util.Calendar;
import java.util.List;

/**
 * Adapter for displaying class items in schedule lists.
 */
public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    private final List<ClassEntity> classes;
    private final OnClassClickListener listener;

    public interface OnClassClickListener {
        void onClassClick(ClassEntity classEntity);
    }

    public ClassAdapter(List<ClassEntity> classes, OnClassClickListener listener) {
        this.classes = classes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassEntity classEntity = classes.get(position);
        holder.bind(classEntity, listener);
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView startTime;
        private final TextView endTime;
        private final TextView className;
        private final TextView location;
        private final TextView statusBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.start_time);
            endTime = itemView.findViewById(R.id.end_time);
            className = itemView.findViewById(R.id.class_name);
            location = itemView.findViewById(R.id.location);
            statusBadge = itemView.findViewById(R.id.status_badge);
        }

        void bind(ClassEntity classEntity, OnClassClickListener listener) {
            startTime.setText(classEntity.getFormattedStartTime());
            endTime.setText(classEntity.getFormattedEndTime());
            className.setText(classEntity.getName());
            
            String loc = classEntity.getLocation();
            if (loc != null && !loc.isEmpty()) {
                location.setText(loc);
                location.setVisibility(View.VISIBLE);
            } else {
                location.setVisibility(View.GONE);
            }

            // Check if class is happening now or soon
            Calendar now = Calendar.getInstance();
            int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            int today = now.get(Calendar.DAY_OF_WEEK);

            if (classEntity.occursOnDay(today)) {
                if (currentMinutes >= classEntity.getStartTime() && 
                    currentMinutes < classEntity.getEndTime()) {
                    statusBadge.setText(R.string.in_progress);
                    statusBadge.setBackgroundTintList(
                        itemView.getContext().getResources().getColorStateList(R.color.success, null));
                    statusBadge.setVisibility(View.VISIBLE);
                } else if (classEntity.getStartTime() - currentMinutes > 0 && 
                           classEntity.getStartTime() - currentMinutes <= 30) {
                    int mins = classEntity.getStartTime() - currentMinutes;
                    statusBadge.setText("In " + mins + " min");
                    statusBadge.setBackgroundTintList(
                        itemView.getContext().getResources().getColorStateList(R.color.warning, null));
                    statusBadge.setVisibility(View.VISIBLE);
                } else {
                    statusBadge.setVisibility(View.GONE);
                }
            } else {
                statusBadge.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClassClick(classEntity);
                }
            });
        }
    }
}
