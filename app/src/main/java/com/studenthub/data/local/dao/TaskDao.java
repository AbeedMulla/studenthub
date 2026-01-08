package com.studenthub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.studenthub.data.local.entity.TaskEntity;

import java.util.List;

/**
 * Data Access Object for TaskEntity.
 * Provides database operations for task management.
 */
@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskEntity task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TaskEntity> tasks);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM tasks WHERE userId = :userId")
    void deleteAllForUser(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 ORDER BY completed ASC, createdAt DESC")
    LiveData<List<TaskEntity>> getAllForUser(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 ORDER BY completed ASC, createdAt DESC")
    List<TaskEntity> getAllForUserSync(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 AND completed = 0 ORDER BY createdAt DESC")
    LiveData<List<TaskEntity>> getIncompleteForUser(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 AND completed = 0 ORDER BY createdAt DESC")
    List<TaskEntity> getIncompleteForUserSync(String userId);

    @Query("SELECT * FROM tasks WHERE id = :id")
    TaskEntity getById(String id);

    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<TaskEntity> getByIdLiveData(String id);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 AND completed = 0 AND (dueDate IS NULL OR (dueDate >= :startTime AND dueDate < :endTime)) ORDER BY dueDate ASC")
    List<TaskEntity> getTasksForToday(String userId, long startTime, long endTime);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 AND completed = 0 ORDER BY createdAt DESC LIMIT :limit")
    List<TaskEntity> getRecentIncompleteTasks(String userId, int limit);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND synced = 0")
    List<TaskEntity> getUnsyncedTasks(String userId);

    @Query("UPDATE tasks SET synced = 1 WHERE id = :id")
    void markSynced(String id);

    @Query("UPDATE tasks SET synced = 1 WHERE userId = :userId")
    void markAllSynced(String userId);

    @Query("UPDATE tasks SET completed = :completed, updatedAt = :timestamp, synced = 0 WHERE id = :id")
    void setCompleted(String id, boolean completed, long timestamp);

    @Query("UPDATE tasks SET deleted = 1, synced = 0, updatedAt = :timestamp WHERE id = :id")
    void softDelete(String id, long timestamp);

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND deleted = 0 AND completed = 0")
    int getIncompleteCountForUser(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    List<TaskEntity> getAllIncludingDeleted(String userId);

    @Query("DELETE FROM tasks WHERE deleted = 1 AND synced = 1")
    void cleanupDeletedAndSynced();
}