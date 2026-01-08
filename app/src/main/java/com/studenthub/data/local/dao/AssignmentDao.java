package com.studenthub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.studenthub.data.local.entity.AssignmentEntity;

import java.util.List;

/**
 * Data Access Object for AssignmentEntity.
 * Provides database operations for assignment management.
 */
@Dao
public interface AssignmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AssignmentEntity assignment);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AssignmentEntity> assignments);
    
    @Update
    void update(AssignmentEntity assignment);
    
    @Delete
    void delete(AssignmentEntity assignment);
    
    @Query("DELETE FROM assignments WHERE id = :id")
    void deleteById(String id);
    
    @Query("DELETE FROM assignments WHERE userId = :userId")
    void deleteAllForUser(String userId);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND deleted = 0 ORDER BY dueDate ASC")
    LiveData<List<AssignmentEntity>> getAllForUser(String userId);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND deleted = 0 ORDER BY dueDate ASC")
    List<AssignmentEntity> getAllForUserSync(String userId);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND deleted = 0 AND completed = 0 ORDER BY dueDate ASC")
    LiveData<List<AssignmentEntity>> getIncompleteForUser(String userId);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND deleted = 0 AND completed = 0 ORDER BY dueDate ASC")
    List<AssignmentEntity> getIncompleteForUserSync(String userId);
    
    @Query("SELECT * FROM assignments WHERE id = :id")
    AssignmentEntity getById(String id);
    
    @Query("SELECT * FROM assignments WHERE id = :id")
    LiveData<AssignmentEntity> getByIdLiveData(String id);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND deleted = 0 AND completed = 0 AND dueDate BETWEEN :startTime AND :endTime ORDER BY dueDate ASC")
    List<AssignmentEntity> getAssignmentsDueBetween(String userId, long startTime, long endTime);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND deleted = 0 AND completed = 0 AND dueDate BETWEEN :startTime AND :endTime ORDER BY dueDate ASC LIMIT :limit")
    List<AssignmentEntity> getUpcomingAssignments(String userId, long startTime, long endTime, int limit);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId AND synced = 0")
    List<AssignmentEntity> getUnsyncedAssignments(String userId);
    
    @Query("UPDATE assignments SET synced = 1 WHERE id = :id")
    void markSynced(String id);
    
    @Query("UPDATE assignments SET synced = 1 WHERE userId = :userId")
    void markAllSynced(String userId);
    
    @Query("UPDATE assignments SET completed = :completed, updatedAt = :timestamp, synced = 0 WHERE id = :id")
    void setCompleted(String id, boolean completed, long timestamp);
    
    @Query("UPDATE assignments SET deleted = 1, synced = 0, updatedAt = :timestamp WHERE id = :id")
    void softDelete(String id, long timestamp);
    
    @Query("UPDATE assignments SET lastReminderSent = :timestamp WHERE id = :id")
    void updateLastReminderSent(String id, long timestamp);
    
    @Query("SELECT COUNT(*) FROM assignments WHERE userId = :userId AND deleted = 0 AND completed = 0")
    int getIncompleteCountForUser(String userId);
    
    @Query("SELECT * FROM assignments WHERE userId = :userId")
    List<AssignmentEntity> getAllIncludingDeleted(String userId);
    
    @Query("DELETE FROM assignments WHERE deleted = 1 AND synced = 1")
    void cleanupDeletedAndSynced();
}
