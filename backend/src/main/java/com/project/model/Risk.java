package com.project.model;

import com.project.enums.RiskLevel;
import com.project.enums.RiskStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Risk {
    private String id;
    private String projectId;
    private String relatedTaskId;
    private String title;
    private String description;
    private RiskLevel level = RiskLevel.LOW;
    private RiskStatus status = RiskStatus.OPEN;
    private List<RiskAction> actions = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
