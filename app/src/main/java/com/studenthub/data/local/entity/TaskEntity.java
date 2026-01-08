package com.studenthub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a simple task.
 * Stores task details including title, optional due date, and tags.
 */
@Entity(tableName = "tasks")
public class TaskEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String userId;
    private String title;
    private Long dueDate; // Nullable - optional due date
    private String tags; // Comma-separated tags
    private boolean completed;
    
    // Sync fields
    private long createdAt;
    private long updatedAt;
    private boolean deleted;
    private boolean synced;
    
    // Constructors
    public TaskEntity() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.completed = false;
        this.deleted = false;
        this.synced = false;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Helper methods
    
    /**
     * Check if task has a due date set
     */
    public boolean hasDueDate() {
        return dueDate != null && dueDate > 0;
    }
    
    /**
     * Check if task is due today
     */
    public boolean isDueToday() {
        return hasDueDate() && com.studenthub.util.DateTimeUtils.isToday(dueDate);
    }
    
    /**
     * Check if task is overdue
     */
    public boolean isOverdue() {
        return hasDueDate() && !completed && dueDate < System.currentTimeMillis();
    }
    
    /**
     * Get formatted due date or empty string
     */
    public String getFormattedDueDate() {
        if (!hasDueDate()) return "";
        
        if (com.studenthub.util.DateTimeUtils.isToday(dueDate)) {
            return "Due today";
        } else if (com.studenthub.util.DateTimeUtils.isTomorrow(dueDate)) {
            return "Due tomorrow";
        } else {
            return "Due " + com.studenthub.util.DateTimeUtils.formatDateShort(dueDate);
        }
    }
    
    /**
     * Get tags as formatted string (with # prefix)
     */
    public String getFormattedTags() {
        if (tags == null || tags.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        String[] tagArray = tags.split(",");
        for (String tag : tagArray) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append("#").append(trimmed);
            }
        }
        return sb.toString();
    }
    
    /**
     * Mark entity as updated (for sync tracking)
     */
    public void markUpdated() {
        this.updatedAt = System.currentTimeMillis();
        this.synced = false;
    }
}
