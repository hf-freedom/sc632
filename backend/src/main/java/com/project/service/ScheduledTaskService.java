package com.project.service;

import com.project.enums.TaskStatus;
import com.project.model.*;
import com.project.store.DataStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ScheduledTaskService {

    @Scheduled(cron = "0 0 9 * * ?")
    public void scanUpcomingDelayedTasks() {
        LocalDate today = LocalDate.now();
        for (Task task : DataStore.tasks.values()) {
            if (task.getStatus() == TaskStatus.COMPLETED) continue;
            if (task.getEndDate() == null) continue;
            
            long daysUntilDue = ChronoUnit.DAYS.between(today, task.getEndDate());
            
            if (daysUntilDue <= 3 && daysUntilDue >= 0) {
                createNotification(task, "任务即将到期", 
                    "任务 \"" + task.getName() + "\" 将在 " + daysUntilDue + " 天后到期");
            } else if (daysUntilDue < 0) {
                task.setStatus(TaskStatus.DELAYED);
                createNotification(task, "任务已延期", 
                    "任务 \"" + task.getName() + "\" 已延期 " + Math.abs(daysUntilDue) + " 天");
            }
        }
    }

    private void createNotification(Task task, String title, String message) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setProjectId(task.getProjectId());
        notification.setMemberId(task.getAssigneeId());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        DataStore.notifications.put(notification.getId(), notification);
    }
}
