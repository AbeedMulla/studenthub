package com.studenthub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an assignment.
 * Stores assignment details including title, course, due date, and priority.
 */
@Entity(tableName = "assignments")
public class AssignmentEntity {
    
    // Priority constants
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String userId;
    private String title;
    private String course;
    private long dueDate;
    private int priority;
    private String notes;
    private boolean completed;
    
    // Sync fields
    private long createdAt;
    private long updatedAt;
    private boolean deleted;
    private boolean synced;
    
    // Reminder tracking (to prevent spam)
    private long lastReminderSent;
    
    // Constructors
    public AssignmentEntity() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.priority = PRIORITY_MEDIUM;
        this.completed = false;
        this.deleted = false;
        this.synced = false;
        this.lastReminderSent = 0;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    
    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
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
    
    public long getLastReminderSent() { return lastReminderSent; }
    public void setLastReminderSent(long lastReminderSent) { this.lastReminderSent = lastReminderSent; }
    
    // Helper methods
    
    /**
     * Get priority as human-readable string
     */
    public String getPriorityString() {
        switch (priority) {
            case PRIORITY_HIGH: return "High";
            case PRIORITY_MEDIUM: return "Medium";
            case PRIORITY_LOW: return "Low";
            default: return "Medium";
        }
    }
    
    /**
     * Check if assignment is overdue
     */
    public boolean isOverdue() {
        return !completed && dueDate < System.currentTimeMillis();
    }
    
    /**
     * Check if assignment is due today
     */
    public boolean isDueToday() {
        return com.studenthub.util.DateTimeUtils.isToday(dueDate);
    }
    
    /**
     * Check if assignment is due tomorrow
     */
    public boolean isDueTomorrow() {
        return com.studenthub.util.DateTimeUtils.isTomorrow(dueDate);
    }
    
    /**
     * Check if assignment is due this week
     */
    public boolean isDueThisWeek() {
        return com.studenthub.util.DateTimeUtils.isThisWeek(dueDate);
    }
    
    /**
     * Get formatted due date
     */
    public String getFormattedDueDate() {
        return com.studenthub.util.DateTimeUtils.formatDateTime(dueDate);
    }
    
    /**
     * Check if we can send a reminder (anti-spam - max once per day per item)
     */
    public boolean canSendReminder() {
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        return lastReminderSent < oneDayAgo;
    }
    
    /**
     * Mark entity as updated (for sync tracking)
     */
    public void markUpdated() {
        this.updatedAt = System.currentTimeMillis();
        this.synced = false;
    }
}
