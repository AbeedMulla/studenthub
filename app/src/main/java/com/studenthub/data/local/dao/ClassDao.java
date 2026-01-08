package com.studenthub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.studenthub.data.local.entity.ClassEntity;

import java.util.List;

/**
 * Data Access Object for ClassEntity.
 * Provides database operations for class/course management.
 */
@Dao
public interface ClassDao {
    
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClassEntity classEntity);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ClassEntity> classes);
    
    // Update operations
    @Update
    void update(ClassEntity classEntity);
    
    // Delete operations
    @Delete
    void delete(ClassEntity classEntity);
    
    @Query("DELETE FROM classes WHERE id = :id")
    void deleteById(String id);
    
    @Query("DELETE FROM classes WHERE userId = :userId")
    void deleteAllForUser(String userId);
    
    // Query operations
    
    /**
     * Get all non-deleted classes for a user (LiveData for observation)
     */
    @Query("SELECT * FROM classes WHERE userId = :userId AND deleted = 0 ORDER BY startTime ASC")
    LiveData<List<ClassEntity>> getAllForUser(String userId);
    
    /**
     * Get all non-deleted classes for a user (blocking call)
     */
    @Query("SELECT * FROM classes WHERE userId = :userId AND deleted = 0 ORDER BY startTime ASC")
    List<ClassEntity> getAllForUserSync(String userId);
    
    /**
     * Get a specific class by ID
     */
    @Query("SELECT * FROM classes WHERE id = :id")
    ClassEntity getById(String id);
    
    /**
     * Get a specific class by ID (LiveData)
     */
    @Query("SELECT * FROM classes WHERE id = :id")
    LiveData<ClassEntity> getByIdLiveData(String id);
    
    /**
     * Get classes for a specific day of week
     * @param dayOfWeek day number as string to search in days field
     */
    @Query("SELECT * FROM classes WHERE userId = :userId AND deleted = 0 AND days LIKE '%' || :dayOfWeek || '%' ORDER BY startTime ASC")
    List<ClassEntity> getClassesForDay(String userId, String dayOfWeek);
    
    /**
     * Get classes for a specific day (LiveData)
     */
    @Query("SELECT * FROM classes WHERE userId = :userId AND deleted = 0 AND days LIKE '%' || :dayOfWeek || '%' ORDER BY startTime ASC")
    LiveData<List<ClassEntity>> getClassesForDayLiveData(String userId, String dayOfWeek);
    
    /**
     * Get unsynced classes (for sync operation)
     */
    @Query("SELECT * FROM classes WHERE userId = :userId AND synced = 0")
    List<ClassEntity> getUnsyncedClasses(String userId);
    
    /**
     * Mark class as synced
     */
    @Query("UPDATE classes SET synced = 1 WHERE id = :id")
    void markSynced(String id);
    
    /**
     * Mark all classes for user as synced
     */
    @Query("UPDATE classes SET synced = 1 WHERE userId = :userId")
    void markAllSynced(String userId);
    
    /**
     * Soft delete a class
     */
    @Query("UPDATE classes SET deleted = 1, synced = 0, updatedAt = :timestamp WHERE id = :id")
    void softDelete(String id, long timestamp);
    
    /**
     * Get count of classes for user
     */
    @Query("SELECT COUNT(*) FROM classes WHERE userId = :userId AND deleted = 0")
    int getCountForUser(String userId);
    
    /**
     * Get all classes including deleted (for sync)
     */
    @Query("SELECT * FROM classes WHERE userId = :userId")
    List<ClassEntity> getAllIncludingDeleted(String userId);
    
    /**
     * Hard delete classes that are marked as deleted and synced
     */
    @Query("DELETE FROM classes WHERE deleted = 1 AND synced = 1")
    void cleanupDeletedAndSynced();
}
