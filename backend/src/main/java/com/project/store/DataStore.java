package com.project.store;

import com.project.model.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static final Map<String, Project> projects = new ConcurrentHashMap<>();
    public static final Map<String, Phase> phases = new ConcurrentHashMap<>();
    public static final Map<String, Task> tasks = new ConcurrentHashMap<>();
    public static final Map<String, Milestone> milestones = new ConcurrentHashMap<>();
    public static final Map<String, Risk> risks = new ConcurrentHashMap<>();
    public static final Map<String, Member> members = new ConcurrentHashMap<>();
    public static final Map<String, TodoItem> todoItems = new ConcurrentHashMap<>();
    public static final Map<String, Notification> notifications = new ConcurrentHashMap<>();
    public static final Map<String, LeaveRecord> leaveRecords = new ConcurrentHashMap<>();
}
