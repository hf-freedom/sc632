package com.project.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private String id;
    private String projectId;
    private String memberId;
    private String title;
    private String message;
    private boolean read = false;
    private LocalDateTime createdAt;
}
