package com.studenthub.ui.assignments;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studenthub.R;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.util.DateTimeUtils;

import java.util.List;

/**
 * Adapter for assignment list with section headers.
 */
public class AssignmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ASSIGNMENT = 1;

    private final List<Object> items;
    private final OnAssignmentListener listener;

    public interface OnAssignmentListener {
        void onAssignmentClick(AssignmentEntity assignment);
        void onCompletedChanged(AssignmentEntity assignment, boolean completed);
    }

    public AssignmentAdapter(List<Object> items, OnAssignmentListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ASSIGNMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_assignment, parent, false);
            return new AssignmentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else {
            ((AssignmentViewHolder) holder).bind((AssignmentEntity) items.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView sectionTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.section_title);
        }

        void bind(String title) {
            sectionTitle.setText(title);
        }
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final View priorityIndicator;
        private final TextView title, course, dueDate;
        private final CheckBox checkbox;

        AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            title = itemView.findViewById(R.id.title);
            course = itemView.findViewById(R.id.course);
            dueDate = itemView.findViewById(R.id.due_date);
            checkbox = itemView.findViewById(R.id.checkbox);
        }

        void bind(AssignmentEntity assignment, OnAssignmentListener listener) {
            title.setText(assignment.getTitle());
            course.setText(assignment.getCourse());
            dueDate.setText(DateTimeUtils.getRelativeTimeString(assignment.getDueDate()));
            checkbox.setChecked(assignment.isCompleted());

            // Strikethrough if completed
            if (assignment.isCompleted()) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                title.setAlpha(0.6f);
            } else {
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                title.setAlpha(1f);
            }

            // Priority color
            int colorRes;
            switch (assignment.getPriority()) {
                case AssignmentEntity.PRIORITY_HIGH:
                    colorRes = R.color.priority_high;
                    break;
                case AssignmentEntity.PRIORITY_LOW:
                    colorRes = R.color.priority_low;
                    break;
                default:
                    colorRes = R.color.priority_medium;
            }
            priorityIndicator.setBackgroundTintList(
                    itemView.getContext().getResources().getColorStateList(colorRes, null));

            // Due date color (red if overdue)
            if (assignment.isOverdue()) {
                dueDate.setTextColor(itemView.getContext().getResources().getColor(R.color.error, null));
            } else {
                dueDate.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary, null));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAssignmentClick(assignment);
            });

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onCompletedChanged(assignment, isChecked);
            });
        }
    }
}
