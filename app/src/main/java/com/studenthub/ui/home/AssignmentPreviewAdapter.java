package com.studenthub.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studenthub.R;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.util.DateTimeUtils;

import java.util.List;

/**
 * Simple adapter for showing assignment previews on home dashboard.
 */
public class AssignmentPreviewAdapter extends RecyclerView.Adapter<AssignmentPreviewAdapter.ViewHolder> {

    private final List<AssignmentEntity> assignments;

    public AssignmentPreviewAdapter(List<AssignmentEntity> assignments) {
        this.assignments = assignments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_assignment_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentEntity assignment = assignments.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View priorityIndicator;
        private final TextView title;
        private final TextView dueDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            title = itemView.findViewById(R.id.title);
            dueDate = itemView.findViewById(R.id.due_date);
        }

        void bind(AssignmentEntity assignment) {
            title.setText(assignment.getTitle());
            dueDate.setText(DateTimeUtils.getRelativeTimeString(assignment.getDueDate()));
            
            // Set priority color
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
        }
    }
}
