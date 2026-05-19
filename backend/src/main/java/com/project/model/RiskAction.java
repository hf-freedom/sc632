package com.project.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskAction {
    private String id;
    private String riskId;
    private String action;
    private String handlerId;
    private String result;
    private LocalDateTime createdAt;
}
