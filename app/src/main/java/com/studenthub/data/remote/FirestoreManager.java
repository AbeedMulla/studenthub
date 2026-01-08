package com.studenthub.data.remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.studenthub.data.local.entity.AssignmentEntity;
import com.studenthub.data.local.entity.ClassEntity;
import com.studenthub.data.local.entity.TaskEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all Firestore operations for cloud synchronization.
 * 
 * Data Structure:
 * - users/{uid}/classes/{classId}
 * - users/{uid}/assignments/{assignmentId}
 * - users/{uid}/tasks/{taskId}
 * - users/{uid}/settings (single document)
 */
public class FirestoreManager {
    
    private static final String TAG = "FirestoreManager";
    
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_CLASSES = "classes";
    private static final String COLLECTION_ASSIGNMENTS = "assignments";
    private static final String COLLECTION_TASKS = "tasks";
    private static final String DOC_SETTINGS = "settings";
    
    private final FirebaseFirestore db;
    private static FirestoreManager instance;
    
    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }
    
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }
    
    /**
     * Get current user ID or null if not logged in.
     */
    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    // ========== CLASS OPERATIONS ==========
    
    /**
     * Save a class to Firestore.
     */
    public void saveClass(ClassEntity classEntity, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        Map<String, Object> data = classToMap(classEntity);
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CLASSES)
            .document(classEntity.getId())
            .set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Fetch all classes for the current user from Firestore.
     */
    public void fetchClasses(OnFetchClassesListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CLASSES)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<ClassEntity> classes = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    ClassEntity entity = mapToClass(doc);
                    if (entity != null) {
                        classes.add(entity);
                    }
                }
                listener.onSuccess(classes);
            })
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Delete a class from Firestore.
     */
    public void deleteClass(String classId, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CLASSES)
            .document(classId)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    // ========== ASSIGNMENT OPERATIONS ==========
    
    /**
     * Save an assignment to Firestore.
     */
    public void saveAssignment(AssignmentEntity assignment, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        Map<String, Object> data = assignmentToMap(assignment);
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ASSIGNMENTS)
            .document(assignment.getId())
            .set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Fetch all assignments for the current user from Firestore.
     */
    public void fetchAssignments(OnFetchAssignmentsListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ASSIGNMENTS)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<AssignmentEntity> assignments = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    AssignmentEntity entity = mapToAssignment(doc);
                    if (entity != null) {
                        assignments.add(entity);
                    }
                }
                listener.onSuccess(assignments);
            })
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Delete an assignment from Firestore.
     */
    public void deleteAssignment(String assignmentId, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ASSIGNMENTS)
            .document(assignmentId)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    // ========== TASK OPERATIONS ==========
    
    /**
     * Save a task to Firestore.
     */
    public void saveTask(TaskEntity task, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        Map<String, Object> data = taskToMap(task);
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_TASKS)
            .document(task.getId())
            .set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Fetch all tasks for the current user from Firestore.
     */
    public void fetchTasks(OnFetchTasksListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_TASKS)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<TaskEntity> tasks = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    TaskEntity entity = mapToTask(doc);
                    if (entity != null) {
                        tasks.add(entity);
                    }
                }
                listener.onSuccess(tasks);
            })
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Delete a task from Firestore.
     */
    public void deleteTask(String taskId, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_TASKS)
            .document(taskId)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    // ========== MAPPING HELPERS ==========
    
    private Map<String, Object> classToMap(ClassEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("userId", entity.getUserId());
        map.put("name", entity.getName());
        map.put("days", entity.getDays());
        map.put("startTime", entity.getStartTime());
        map.put("endTime", entity.getEndTime());
        map.put("building", entity.getBuilding());
        map.put("room", entity.getRoom());
        map.put("notes", entity.getNotes());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        map.put("deleted", entity.isDeleted());
        return map;
    }
    
    private ClassEntity mapToClass(DocumentSnapshot doc) {
        try {
            ClassEntity entity = new ClassEntity();
            entity.setId(doc.getString("id"));
            entity.setUserId(doc.getString("userId"));
            entity.setName(doc.getString("name"));
            entity.setDays(doc.getString("days"));
            entity.setStartTime(doc.getLong("startTime").intValue());
            entity.setEndTime(doc.getLong("endTime").intValue());
            entity.setBuilding(doc.getString("building"));
            entity.setRoom(doc.getString("room"));
            entity.setNotes(doc.getString("notes"));
            entity.setCreatedAt(doc.getLong("createdAt"));
            entity.setUpdatedAt(doc.getLong("updatedAt"));
            entity.setDeleted(Boolean.TRUE.equals(doc.getBoolean("deleted")));
            entity.setSynced(true);
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Error mapping class document", e);
            return null;
        }
    }
    
    private Map<String, Object> assignmentToMap(AssignmentEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("userId", entity.getUserId());
        map.put("title", entity.getTitle());
        map.put("course", entity.getCourse());
        map.put("dueDate", entity.getDueDate());
        map.put("priority", entity.getPriority());
        map.put("notes", entity.getNotes());
        map.put("completed", entity.isCompleted());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        map.put("deleted", entity.isDeleted());
        return map;
    }
    
    private AssignmentEntity mapToAssignment(DocumentSnapshot doc) {
        try {
            AssignmentEntity entity = new AssignmentEntity();
            entity.setId(doc.getString("id"));
            entity.setUserId(doc.getString("userId"));
            entity.setTitle(doc.getString("title"));
            entity.setCourse(doc.getString("course"));
            entity.setDueDate(doc.getLong("dueDate"));
            entity.setPriority(doc.getLong("priority").intValue());
            entity.setNotes(doc.getString("notes"));
            entity.setCompleted(Boolean.TRUE.equals(doc.getBoolean("completed")));
            entity.setCreatedAt(doc.getLong("createdAt"));
            entity.setUpdatedAt(doc.getLong("updatedAt"));
            entity.setDeleted(Boolean.TRUE.equals(doc.getBoolean("deleted")));
            entity.setSynced(true);
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Error mapping assignment document", e);
            return null;
        }
    }
    
    private Map<String, Object> taskToMap(TaskEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("userId", entity.getUserId());
        map.put("title", entity.getTitle());
        map.put("dueDate", entity.getDueDate());
        map.put("tags", entity.getTags());
        map.put("completed", entity.isCompleted());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        map.put("deleted", entity.isDeleted());
        return map;
    }
    
    private TaskEntity mapToTask(DocumentSnapshot doc) {
        try {
            TaskEntity entity = new TaskEntity();
            entity.setId(doc.getString("id"));
            entity.setUserId(doc.getString("userId"));
            entity.setTitle(doc.getString("title"));
            entity.setDueDate(doc.getLong("dueDate"));
            entity.setTags(doc.getString("tags"));
            entity.setCompleted(Boolean.TRUE.equals(doc.getBoolean("completed")));
            entity.setCreatedAt(doc.getLong("createdAt"));
            entity.setUpdatedAt(doc.getLong("updatedAt"));
            entity.setDeleted(Boolean.TRUE.equals(doc.getBoolean("deleted")));
            entity.setSynced(true);
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Error mapping task document", e);
            return null;
        }
    }
    
    // ========== LISTENERS ==========
    
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    public interface OnFetchClassesListener {
        void onSuccess(List<ClassEntity> classes);
        void onFailure(Exception e);
    }
    
    public interface OnFetchAssignmentsListener {
        void onSuccess(List<AssignmentEntity> assignments);
        void onFailure(Exception e);
    }
    
    public interface OnFetchTasksListener {
        void onSuccess(List<TaskEntity> tasks);
        void onFailure(Exception e);
    }
}
