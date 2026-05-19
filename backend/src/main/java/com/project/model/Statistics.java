package com.project.model;

import lombok.Data;

@Data
public class Statistics {
    private double delayRate;
    private double taskCompletionRate;
    private double riskClosureRate;
    private int totalProjects;
    private int delayedProjects;
    private int totalTasks;
    private int completedTasks;
    private int totalRisks;
    private int closedRisks;
}
