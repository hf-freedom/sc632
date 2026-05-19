package com.project.service;

import com.project.enums.RiskStatus;
import com.project.enums.TaskStatus;
import com.project.model.*;
import com.project.store.DataStore;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class StatisticsService {

    public Statistics calculateStatistics() {
        Statistics stats = new Statistics();
        
        stats.setTotalProjects(DataStore.projects.size());
        
        int delayedProjects = 0;
        int totalTasks = 0;
        int completedTasks = 0;
        int totalRisks = 0;
        int closedRisks = 0;
        
        for (Project project : DataStore.projects.values()) {
            totalTasks += project.getTasks().size();
            for (Task task : project.getTasks()) {
                if (task.getStatus() == TaskStatus.COMPLETED) {
                    completedTasks++;
                }
            }
            
            totalRisks += project.getRisks().size();
            for (Risk risk : project.getRisks()) {
                if (risk.getStatus() == RiskStatus.CLOSED) {
                    closedRisks++;
                }
            }
            
            boolean isDelayed = project.getTasks().stream()
                .anyMatch(t -> t.getStatus() == TaskStatus.DELAYED ||
                    (t.getEndDate() != null && LocalDate.now().isAfter(t.getEndDate()) 
                        && t.getStatus() != TaskStatus.COMPLETED));
            if (isDelayed) {
                delayedProjects++;
            }
        }
        
        stats.setDelayedProjects(delayedProjects);
        stats.setTotalTasks(totalTasks);
        stats.setCompletedTasks(completedTasks);
        stats.setTotalRisks(totalRisks);
        stats.setClosedRisks(closedRisks);
        
        stats.setDelayRate(stats.getTotalProjects() > 0 ? 
            (double) delayedProjects / stats.getTotalProjects() * 100 : 0);
        stats.setTaskCompletionRate(totalTasks > 0 ? 
            (double) completedTasks / totalTasks * 100 : 0);
        stats.setRiskClosureRate(totalRisks > 0 ? 
            (double) closedRisks / totalRisks * 100 : 0);
        
        return stats;
    }
}
