package com.studenthub.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.studenthub.data.local.dao.AssignmentDao;
import com.studenthub.data.local.dao.ClassDao;
import com.studenthub.data.local.dao.TaskDao;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.data.local.entity.ClassEntity;
import com.studenthub.data.local.entity.TaskEntity;

/**
 * Room database for StudentHub app.
 * Provides offline-first storage for classes, assignments, and tasks.
 */
@Database(
    entities = {
        ClassEntity.class,
        AssignmentEntity.class,
        TaskEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "studenthub_db";
    private static volatile AppDatabase instance;
    
    // DAOs
    public abstract ClassDao classDao();
    public abstract AssignmentDao assignmentDao();
    public abstract TaskDao taskDao();
    
    /**
     * Get singleton instance of the database.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
    
    /**
     * Clear all data from the database.
     */
    public void clearAllData() {
        if (instance != null) {
            instance.clearAllTables();
        }
    }
}
