package com.studenthub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a class/course in the student's schedule.
 * Stores class details including name, schedule, and location.
 */
@Entity(tableName = "classes")
public class ClassEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String userId;
    private String name;
    
    // Days stored as comma-separated integers (1=Sunday, 7=Saturday)
    private String days;
    
    // Time stored as minutes from midnight
    private int startTime;
    private int endTime;
    
    private String building;
    private String room;
    private String notes;
    
    // Sync fields
    private long createdAt;
    private long updatedAt;
    private boolean deleted;
    private boolean synced;
    
    // Constructors
    public ClassEntity() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deleted = false;
        this.synced = false;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }
    
    public int getStartTime() { return startTime; }
    public void setStartTime(int startTime) { this.startTime = startTime; }
    
    public int getEndTime() { return endTime; }
    public void setEndTime(int endTime) { this.endTime = endTime; }
    
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
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
     * Get formatted location string (Building + Room)
     */
    public String getLocation() {
        StringBuilder sb = new StringBuilder();
        if (building != null && !building.isEmpty()) {
            sb.append(building);
        }
        if (room != null && !room.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Room ").append(room);
        }
        return sb.toString();
    }
    
    /**
     * Check if class occurs on a specific day
     * @param dayOfWeek Calendar day constant (1=Sunday, 7=Saturday)
     */
    public boolean occursOnDay(int dayOfWeek) {
        if (days == null || days.isEmpty()) return false;
        return days.contains(String.valueOf(dayOfWeek));
    }
    
    /**
     * Get start time as formatted string
     */
    public String getFormattedStartTime() {
        int hour = startTime / 60;
        int minute = startTime % 60;
        return com.studenthub.util.DateTimeUtils.formatTime(hour, minute);
    }
    
    /**
     * Get end time as formatted string
     */
    public String getFormattedEndTime() {
        int hour = endTime / 60;
        int minute = endTime % 60;
        return com.studenthub.util.DateTimeUtils.formatTime(hour, minute);
    }
    
    /**
     * Mark entity as updated (for sync tracking)
     */
    public void markUpdated() {
        this.updatedAt = System.currentTimeMillis();
        this.synced = false;
    }
}
