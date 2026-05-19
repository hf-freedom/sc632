package com.project.model;

import com.project.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
public class Project {
    private String id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Phase> phases = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Milestone> milestones = new ArrayList<>();
    private List<Risk> risks = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private double overallProgress;

    public void calculateOverallProgress() {
        if (tasks.isEmpty()) {
            this.overallProgress = 0;
            return;
        }
        double totalWeight = 0;
        double completedWeight = 0;
        for (Task task : tasks) {
            totalWeight += task.getWeight();
            completedWeight += task.getWeight() * task.getProgress() / 100;
        }
        this.overallProgress = totalWeight > 0 ? (completedWeight / totalWeight) * 100 : 0;
    }

    public long getTotalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
