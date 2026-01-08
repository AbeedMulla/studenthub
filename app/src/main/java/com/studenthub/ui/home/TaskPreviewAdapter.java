package com.studenthub.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studenthub.R;
import com.studenthub.data.local.entity.TaskEntity;

import java.util.List;

/**
 * Simple adapter for showing task previews on home dashboard.
 */
public class TaskPreviewAdapter extends RecyclerView.Adapter<TaskPreviewAdapter.ViewHolder> {

    private final List<TaskEntity> tasks;
    private final OnTaskCheckedListener listener;

    public interface OnTaskCheckedListener {
        void onTaskChecked(TaskEntity task, boolean completed);
    }

    public TaskPreviewAdapter(List<TaskEntity> tasks, OnTaskCheckedListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_task_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskEntity task = tasks.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final TextView title;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            title = itemView.findViewById(R.id.title);
        }

        void bind(TaskEntity task, OnTaskCheckedListener listener) {
            title.setText(task.getTitle());
            checkbox.setChecked(task.isCompleted());
            
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskChecked(task, isChecked);
                }
            });
        }
    }
}
