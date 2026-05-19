package com.project.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoItem {
    private String id;
    private String projectId;
    private String riskId;
    private String title;
    private String description;
    private String assigneeId;
    private boolean completed = false;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
}
