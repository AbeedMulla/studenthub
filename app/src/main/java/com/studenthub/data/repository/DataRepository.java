package com.studenthub.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.studenthub.data.local.dao.AssignmentDao;
import com.studenthub.data.local.dao.ClassDao;
import com.studenthub.data.local.dao.TaskDao;
import com.studenthub.data.local.database.AppDatabase;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.data.local.entity.ClassEntity;
import com.studenthub.data.local.entity.TaskEntity;
import com.studenthub.data.remote.FirestoreManager;
import com.studenthub.util.NetworkUtils;
import com.studenthub.util.PreferencesManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main repository for data operations.
 * Implements offline-first strategy with Firestore sync.
 * 
 * Sync Strategy (Last Write Wins):
 * 1. All writes go to local Room database first
 * 2. On network available + app start: push local changes, then pull remote
 * 3. Conflict resolution: compare updatedAt timestamps, keep newer
 * 4. Soft deletes with deleted flag for proper sync
 */
public class DataRepository {
    
    private static final String TAG = "DataRepository";
    
    private static DataRepository instance;
    
    private final ClassDao classDao;
    private final AssignmentDao assignmentDao;
    private final TaskDao taskDao;
    private final FirestoreManager firestoreManager;
    private final NetworkUtils networkUtils;
    private final ExecutorService executor;
    
    private DataRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        classDao = db.classDao();
        assignmentDao = db.assignmentDao();
        taskDao = db.taskDao();
        firestoreManager = FirestoreManager.getInstance();
        networkUtils = NetworkUtils.getInstance(context);
        executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Get current user ID or null.
     */
    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    // ========== CLASS OPERATIONS ==========
    
    public LiveData<List<ClassEntity>> getAllClasses() {
        String userId = getUserId();
        if (userId == null) return null;
        return classDao.getAllForUser(userId);
    }
    
    public LiveData<List<ClassEntity>> getClassesForDay(int dayOfWeek) {
        String userId = getUserId();
        if (userId == null) return null;
        return classDao.getClassesForDayLiveData(userId, String.valueOf(dayOfWeek));
    }
    
    public void getClassesForDaySync(int dayOfWeek, OnDataCallback<List<ClassEntity>> callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError(new Exception("Not logged in"));
            return;
        }
        executor.execute(() -> {
            List<ClassEntity> classes = classDao.getClassesForDay(userId, String.valueOf(dayOfWeek));
            callback.onSuccess(classes);
        });
    }
    
    public LiveData<ClassEntity> getClassById(String id) {
        return classDao.getByIdLiveData(id);
    }
    
    public void saveClass(ClassEntity classEntity, OnCompleteCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onError(new Exception("Not logged in"));
            return;
        }
        
        classEntity.setUserId(userId);
        classEntity.markUpdated();
        
        executor.execute(() -> {
            // Save locally first
            classDao.insert(classEntity);
            
            // Try to sync if online
            if (networkUtils.checkConnection()) {
                firestoreManager.saveClass(classEntity, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> classDao.markSynced(classEntity.getId()));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        Log.w(TAG, "Cloud sync failed, will retry later", e);
                        if (callback != null) callback.onSuccess(); // Local save succeeded
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    public void deleteClass(String classId, OnCompleteCallback callback) {
        executor.execute(() -> {
            classDao.softDelete(classId, System.currentTimeMillis());
            
            if (networkUtils.checkConnection()) {
                firestoreManager.deleteClass(classId, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> classDao.deleteById(classId));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    // ========== ASSIGNMENT OPERATIONS ==========
    
    public LiveData<List<AssignmentEntity>> getAllAssignments() {
        String userId = getUserId();
        if (userId == null) return null;
        return assignmentDao.getAllForUser(userId);
    }
    
    public LiveData<List<AssignmentEntity>> getIncompleteAssignments() {
        String userId = getUserId();
        if (userId == null) return null;
        return assignmentDao.getIncompleteForUser(userId);
    }
    
    public void getUpcomingAssignments(int limit, OnDataCallback<List<AssignmentEntity>> callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError(new Exception("Not logged in"));
            return;
        }
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            long weekFromNow = now + (7 * 24 * 60 * 60 * 1000L);
            List<AssignmentEntity> assignments = assignmentDao.getUpcomingAssignments(userId, now, weekFromNow, limit);
            callback.onSuccess(assignments);
        });
    }
    
    public LiveData<AssignmentEntity> getAssignmentById(String id) {
        return assignmentDao.getByIdLiveData(id);
    }
    
    public void saveAssignment(AssignmentEntity assignment, OnCompleteCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onError(new Exception("Not logged in"));
            return;
        }
        
        assignment.setUserId(userId);
        assignment.markUpdated();
        
        executor.execute(() -> {
            assignmentDao.insert(assignment);
            
            if (networkUtils.checkConnection()) {
                firestoreManager.saveAssignment(assignment, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> assignmentDao.markSynced(assignment.getId()));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    public void setAssignmentCompleted(String id, boolean completed, OnCompleteCallback callback) {
        executor.execute(() -> {
            assignmentDao.setCompleted(id, completed, System.currentTimeMillis());
            
            AssignmentEntity assignment = assignmentDao.getById(id);
            if (assignment != null && networkUtils.checkConnection()) {
                firestoreManager.saveAssignment(assignment, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> assignmentDao.markSynced(id));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    public void deleteAssignment(String assignmentId, OnCompleteCallback callback) {
        executor.execute(() -> {
            assignmentDao.softDelete(assignmentId, System.currentTimeMillis());
            
            if (networkUtils.checkConnection()) {
                firestoreManager.deleteAssignment(assignmentId, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> assignmentDao.deleteById(assignmentId));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    // ========== TASK OPERATIONS ==========
    
    public LiveData<List<TaskEntity>> getAllTasks() {
        String userId = getUserId();
        if (userId == null) return null;
        return taskDao.getAllForUser(userId);
    }
    
    public LiveData<List<TaskEntity>> getIncompleteTasks() {
        String userId = getUserId();
        if (userId == null) return null;
        return taskDao.getIncompleteForUser(userId);
    }
    
    public void getTasksForToday(OnDataCallback<List<TaskEntity>> callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError(new Exception("Not logged in"));
            return;
        }
        executor.execute(() -> {
            long startOfDay = com.studenthub.util.DateTimeUtils.getStartOfDay(System.currentTimeMillis());
            long endOfDay = com.studenthub.util.DateTimeUtils.getEndOfDay(System.currentTimeMillis());
            List<TaskEntity> tasks = taskDao.getTasksForToday(userId, startOfDay, endOfDay);
            callback.onSuccess(tasks);
        });
    }
    
    public void getRecentTasks(int limit, OnDataCallback<List<TaskEntity>> callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError(new Exception("Not logged in"));
            return;
        }
        executor.execute(() -> {
            List<TaskEntity> tasks = taskDao.getRecentIncompleteTasks(userId, limit);
            callback.onSuccess(tasks);
        });
    }
    
    public LiveData<TaskEntity> getTaskById(String id) {
        return taskDao.getByIdLiveData(id);
    }
    
    public void saveTask(TaskEntity task, OnCompleteCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onError(new Exception("Not logged in"));
            return;
        }
        
        task.setUserId(userId);
        task.markUpdated();
        
        executor.execute(() -> {
            taskDao.insert(task);
            
            if (networkUtils.checkConnection()) {
                firestoreManager.saveTask(task, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> taskDao.markSynced(task.getId()));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    public void setTaskCompleted(String id, boolean completed, OnCompleteCallback callback) {
        executor.execute(() -> {
            taskDao.setCompleted(id, completed, System.currentTimeMillis());
            
            TaskEntity task = taskDao.getById(id);
            if (task != null && networkUtils.checkConnection()) {
                firestoreManager.saveTask(task, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> taskDao.markSynced(id));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    public void deleteTask(String taskId, OnCompleteCallback callback) {
        executor.execute(() -> {
            taskDao.softDelete(taskId, System.currentTimeMillis());
            
            if (networkUtils.checkConnection()) {
                firestoreManager.deleteTask(taskId, new FirestoreManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> taskDao.deleteById(taskId));
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onSuccess();
                    }
                });
            } else {
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    // ========== SYNC OPERATIONS ==========
    
    /**
     * Perform full sync: push local changes, then pull remote data.
     */
    public void sync(OnSyncCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError(new Exception("Not logged in"));
            return;
        }
        
        if (!networkUtils.checkConnection()) {
            callback.onError(new Exception("No internet connection"));
            return;
        }
        
        executor.execute(() -> {
            try {
                // Step 1: Push unsynced local changes
                pushLocalChanges(userId);
                
                // Step 2: Pull remote changes
                pullRemoteChanges(userId, callback);
                
            } catch (Exception e) {
                Log.e(TAG, "Sync error", e);
                callback.onError(e);
            }
        });
    }
    
    private void pushLocalChanges(String userId) {
        // Push unsynced classes
        List<ClassEntity> unsyncedClasses = classDao.getUnsyncedClasses(userId);
        for (ClassEntity c : unsyncedClasses) {
            if (c.isDeleted()) {
                firestoreManager.deleteClass(c.getId(), new FirestoreManager.OnCompleteListener() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(Exception e) {}
                });
            } else {
                firestoreManager.saveClass(c, new FirestoreManager.OnCompleteListener() {
                    @Override public void onSuccess() { classDao.markSynced(c.getId()); }
                    @Override public void onFailure(Exception e) {}
                });
            }
        }
        
        // Push unsynced assignments
        List<AssignmentEntity> unsyncedAssignments = assignmentDao.getUnsyncedAssignments(userId);
        for (AssignmentEntity a : unsyncedAssignments) {
            if (a.isDeleted()) {
                firestoreManager.deleteAssignment(a.getId(), new FirestoreManager.OnCompleteListener() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(Exception e) {}
                });
            } else {
                firestoreManager.saveAssignment(a, new FirestoreManager.OnCompleteListener() {
                    @Override public void onSuccess() { assignmentDao.markSynced(a.getId()); }
                    @Override public void onFailure(Exception e) {}
                });
            }
        }
        
        // Push unsynced tasks
        List<TaskEntity> unsyncedTasks = taskDao.getUnsyncedTasks(userId);
        for (TaskEntity t : unsyncedTasks) {
            if (t.isDeleted()) {
                firestoreManager.deleteTask(t.getId(), new FirestoreManager.OnCompleteListener() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(Exception e) {}
                });
            } else {
                firestoreManager.saveTask(t, new FirestoreManager.OnCompleteListener() {
                    @Override public void onSuccess() { taskDao.markSynced(t.getId()); }
                    @Override public void onFailure(Exception e) {}
                });
            }
        }
    }
    
    private void pullRemoteChanges(String userId, OnSyncCallback callback) {
        // Pull classes
        firestoreManager.fetchClasses(new FirestoreManager.OnFetchClassesListener() {
            @Override
            public void onSuccess(List<ClassEntity> remoteClasses) {
                executor.execute(() -> {
                    for (ClassEntity remote : remoteClasses) {
                        ClassEntity local = classDao.getById(remote.getId());
                        if (local == null || remote.getUpdatedAt() > local.getUpdatedAt()) {
                            classDao.insert(remote);
                        }
                    }
                });
                
                // Pull assignments
                firestoreManager.fetchAssignments(new FirestoreManager.OnFetchAssignmentsListener() {
                    @Override
                    public void onSuccess(List<AssignmentEntity> remoteAssignments) {
                        executor.execute(() -> {
                            for (AssignmentEntity remote : remoteAssignments) {
                                AssignmentEntity local = assignmentDao.getById(remote.getId());
                                if (local == null || remote.getUpdatedAt() > local.getUpdatedAt()) {
                                    assignmentDao.insert(remote);
                                }
                            }
                        });
                        
                        // Pull tasks
                        firestoreManager.fetchTasks(new FirestoreManager.OnFetchTasksListener() {
                            @Override
                            public void onSuccess(List<TaskEntity> remoteTasks) {
                                executor.execute(() -> {
                                    for (TaskEntity remote : remoteTasks) {
                                        TaskEntity local = taskDao.getById(remote.getId());
                                        if (local == null || remote.getUpdatedAt() > local.getUpdatedAt()) {
                                            taskDao.insert(remote);
                                        }
                                    }
                                    
                                    // Update last sync time
                                    PreferencesManager.getInstance().setLastSyncTime(System.currentTimeMillis());
                                    callback.onSuccess();
                                });
                            }
                            
                            @Override
                            public void onFailure(Exception e) {
                                callback.onError(e);
                            }
                        });
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        callback.onError(e);
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onError(e);
            }
        });
    }
    
    // ========== CALLBACKS ==========
    
    public interface OnCompleteCallback {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface OnDataCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }
    
    public interface OnSyncCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
