package com.project.model;

import com.project.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Task {
    private String id;
    private String projectId;
    private String phaseId;
    private String name;
    private String description;
    private String assigneeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private TaskStatus status = TaskStatus.PENDING;
    private int progress = 0;
    private int weight = 1;
    private List<String> prerequisiteTaskIds = new ArrayList<>();
    private List<String> milestoneIds = new ArrayList<>();
    private int estimatedHours;
    private int actualHours;
}
