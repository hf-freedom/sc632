package com.project.service;

import com.project.enums.TaskStatus;
import com.project.model.*;
import com.project.store.DataStore;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    public Project createProject(Project project) {
        project.setId(UUID.randomUUID().toString());
        project.setOverallProgress(0);
        DataStore.projects.put(project.getId(), project);
        return project;
    }

    public Project getProject(String id) {
        return DataStore.projects.get(id);
    }

    public List<Project> getAllProjects() {
        return new ArrayList<>(DataStore.projects.values());
    }

    public Project updateProjectDates(String projectId, LocalDate startDate, LocalDate endDate) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return null;
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        regenerateTimeline(project);
        return project;
    }

    private void regenerateTimeline(Project project) {
        long totalDays = ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate()) + 1;
        List<Phase> phases = project.getPhases();
        if (phases.isEmpty()) return;
        
        long daysPerPhase = totalDays / phases.size();
        LocalDate currentDate = project.getStartDate();
        
        for (int i = 0; i < phases.size(); i++) {
            Phase phase = phases.get(i);
            LocalDate phaseEnd = i == phases.size() - 1 ? project.getEndDate() 
                : currentDate.plusDays(daysPerPhase - 1);
            phase.setStartDate(currentDate);
            phase.setEndDate(phaseEnd);
            currentDate = phaseEnd.plusDays(1);
            
            List<Task> phaseTasks = project.getTasks().stream()
                .filter(t -> phase.getId().equals(t.getPhaseId()))
                .collect(Collectors.toList());
            if (!phaseTasks.isEmpty()) {
                long taskDays = ChronoUnit.DAYS.between(phase.getStartDate(), phase.getEndDate()) + 1;
                long daysPerTask = taskDays / phaseTasks.size();
                LocalDate taskDate = phase.getStartDate();
                for (int j = 0; j < phaseTasks.size(); j++) {
                    Task task = phaseTasks.get(j);
                    LocalDate taskEnd = j == phaseTasks.size() - 1 ? phase.getEndDate()
                        : taskDate.plusDays(daysPerTask - 1);
                    task.setStartDate(taskDate);
                    task.setEndDate(taskEnd);
                    taskDate = taskEnd.plusDays(1);
                }
            }
        }
    }

    public Phase addPhase(String projectId, Phase phase) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return null;
        phase.setId(UUID.randomUUID().toString());
        phase.setProjectId(projectId);
        phase.setOrder(project.getPhases().size());
        DataStore.phases.put(phase.getId(), phase);
        project.getPhases().add(phase);
        return phase;
    }

    public Phase updatePhaseDates(String phaseId, Map<String, String> dates) {
        Phase phase = DataStore.phases.get(phaseId);
        if (phase == null) return null;
        
        if (dates.containsKey("startDate") && dates.get("startDate") != null) {
            phase.setStartDate(LocalDate.parse(dates.get("startDate")));
        }
        if (dates.containsKey("endDate") && dates.get("endDate") != null) {
            phase.setEndDate(LocalDate.parse(dates.get("endDate")));
        }
        return phase;
    }

    public Task addTask(String projectId, Task task) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return null;
        task.setId(UUID.randomUUID().toString());
        task.setProjectId(projectId);
        task.setStatus(TaskStatus.PENDING);
        task.setProgress(0);
        DataStore.tasks.put(task.getId(), task);
        project.getTasks().add(task);
        project.calculateOverallProgress();
        return task;
    }

    public Task updateTaskProgress(String taskId, int progress, LocalDate actualStartDate, LocalDate actualEndDate) {
        Task task = DataStore.tasks.get(taskId);
        if (task == null) return null;
        
        if (!canStartTask(task)) {
            throw new IllegalStateException("前置任务未完成，无法开始此任务");
        }
        
        task.setProgress(progress);
        if (actualStartDate != null) task.setActualStartDate(actualStartDate);
        if (actualEndDate != null) task.setActualEndDate(actualEndDate);
        
        if (progress >= 100) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setActualEndDate(actualEndDate != null ? actualEndDate : LocalDate.now());
        } else if (progress > 0) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            if (task.getActualStartDate() == null) {
                task.setActualStartDate(LocalDate.now());
            }
        }
        
        if (task.getEndDate() != null && LocalDate.now().isAfter(task.getEndDate()) 
            && task.getStatus() != TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.DELAYED);
            evaluateMilestoneImpact(task);
            createRiskForDelayedTask(task);
        }
        
        Project project = DataStore.projects.get(task.getProjectId());
        if (project != null) {
            project.calculateOverallProgress();
        }
        
        return task;
    }

    private boolean canStartTask(Task task) {
        for (String prereqId : task.getPrerequisiteTaskIds()) {
            Task prereq = DataStore.tasks.get(prereqId);
            if (prereq == null || prereq.getStatus() != TaskStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    private void evaluateMilestoneImpact(Task delayedTask) {
        Project project = DataStore.projects.get(delayedTask.getProjectId());
        if (project == null) return;
        
        int delayDays = (int) ChronoUnit.DAYS.between(delayedTask.getEndDate(), LocalDate.now());
        
        for (Milestone milestone : project.getMilestones()) {
            if (milestone.getDependentTaskIds().contains(delayedTask.getId()) ||
                milestone.getDependentTaskIds().stream().anyMatch(taskId -> {
                    Task task = DataStore.tasks.get(taskId);
                    return task != null && task.getPrerequisiteTaskIds().contains(delayedTask.getId());
                })) {
                milestone.setDelayDays(Math.max(milestone.getDelayDays(), delayDays));
            }
        }
    }

    private void createRiskForDelayedTask(Task task) {
        Project project = DataStore.projects.get(task.getProjectId());
        if (project == null) return;
        
        Risk risk = new Risk();
        risk.setId(UUID.randomUUID().toString());
        risk.setProjectId(task.getProjectId());
        risk.setRelatedTaskId(task.getId());
        risk.setTitle("任务延期: " + task.getName());
        risk.setDescription("任务 \"" + task.getName() + "\" 已延期。计划完成日期: " + task.getEndDate());
        risk.setCreatedAt(LocalDateTime.now());
        risk.setUpdatedAt(LocalDateTime.now());
        
        int delayDays = (int) ChronoUnit.DAYS.between(task.getEndDate(), LocalDate.now());
        if (delayDays >= 7) {
            risk.setLevel(com.project.enums.RiskLevel.CRITICAL);
        } else if (delayDays >= 3) {
            risk.setLevel(com.project.enums.RiskLevel.HIGH);
        } else {
            risk.setLevel(com.project.enums.RiskLevel.MEDIUM);
        }
        
        DataStore.risks.put(risk.getId(), risk);
        project.getRisks().add(risk);
        
        if (risk.getLevel() == com.project.enums.RiskLevel.HIGH || 
            risk.getLevel() == com.project.enums.RiskLevel.CRITICAL) {
            createRiskTodo(risk);
        }
    }

    private void createRiskTodo(Risk risk) {
        TodoItem todo = new TodoItem();
        todo.setId(UUID.randomUUID().toString());
        todo.setProjectId(risk.getProjectId());
        todo.setRiskId(risk.getId());
        todo.setTitle("处理风险: " + risk.getTitle());
        todo.setDescription(risk.getDescription());
        todo.setCreatedAt(LocalDateTime.now());
        todo.setDueDate(LocalDateTime.now().plusDays(3));
        DataStore.todoItems.put(todo.getId(), todo);
    }

    public Milestone addMilestone(String projectId, Milestone milestone) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return null;
        milestone.setId(UUID.randomUUID().toString());
        milestone.setProjectId(projectId);
        DataStore.milestones.put(milestone.getId(), milestone);
        project.getMilestones().add(milestone);
        return milestone;
    }

    public Member addMember(String projectId, Member member) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return null;
        member.setId(UUID.randomUUID().toString());
        DataStore.members.put(member.getId(), member);
        project.getMembers().add(member);
        return member;
    }

    public void handleMemberLeave(String memberId) {
        Member member = DataStore.members.get(memberId);
        if (member == null) return;
        member.setActive(false);
        
        for (Project project : DataStore.projects.values()) {
            for (Task task : project.getTasks()) {
                if (memberId.equals(task.getAssigneeId()) && task.getStatus() != TaskStatus.COMPLETED) {
                    recalculateManpowerGap(project);
                }
            }
        }
    }

    public void addLeaveRecord(LeaveRecord record) {
        record.setId(UUID.randomUUID().toString());
        DataStore.leaveRecords.put(record.getId(), record);
        
        Member member = DataStore.members.get(record.getMemberId());
        if (member != null) {
            member.getLeaveRecords().add(record);
        }
        
        for (Project project : DataStore.projects.values()) {
            recalculateManpowerGap(project);
        }
    }

    private void recalculateManpowerGap(Project project) {
        int totalEstimatedHours = project.getTasks().stream()
            .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
            .mapToInt(Task::getEstimatedHours)
            .sum();
        
        long availableMembers = project.getMembers().stream()
            .filter(Member::isActive)
            .filter(m -> m.getLeaveRecords().isEmpty())
            .count();
        
        long onLeaveMembers = project.getMembers().stream()
            .filter(Member::isActive)
            .filter(m -> !m.getLeaveRecords().isEmpty())
            .count();
        
        long inactiveMembers = project.getMembers().stream()
            .filter(m -> !m.isActive())
            .count();
        
        int gapHours = 0;
        if (availableMembers > 0) {
            double avgHoursPerMember = totalEstimatedHours / (double) availableMembers;
            if (avgHoursPerMember > 160) {
                gapHours = totalEstimatedHours - (int) (availableMembers * 160);
            }
        }
        
        if ((availableMembers == 0 && totalEstimatedHours > 0) || gapHours > 0) {
            Risk risk = new Risk();
            risk.setId(UUID.randomUUID().toString());
            risk.setProjectId(project.getId());
            risk.setTitle("人力缺口警告");
            risk.setDescription(String.format(
                "项目存在人力缺口，总估计工时: %d小时，在职成员: %d人，请假中: %d人，已离职: %d人，缺口工时: %d小时",
                totalEstimatedHours, availableMembers, onLeaveMembers, inactiveMembers, Math.max(gapHours, totalEstimatedHours)
            ));
            risk.setLevel(com.project.enums.RiskLevel.HIGH);
            risk.setCreatedAt(LocalDateTime.now());
            risk.setUpdatedAt(LocalDateTime.now());
            DataStore.risks.put(risk.getId(), risk);
            project.getRisks().add(risk);
            createRiskTodo(risk);
        }
    }

    public Map<String, Object> getManpowerStats(String projectId) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return null;
        
        Map<String, Object> stats = new HashMap<>();
        
        int totalEstimatedHours = project.getTasks().stream()
            .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
            .mapToInt(Task::getEstimatedHours)
            .sum();
        
        int totalActualHours = project.getTasks().stream()
            .mapToInt(Task::getActualHours)
            .sum();
        
        long availableMembers = project.getMembers().stream()
            .filter(Member::isActive)
            .filter(m -> m.getLeaveRecords().isEmpty())
            .count();
        
        long onLeaveMembers = project.getMembers().stream()
            .filter(Member::isActive)
            .filter(m -> !m.getLeaveRecords().isEmpty())
            .count();
        
        long inactiveMembers = project.getMembers().stream()
            .filter(m -> !m.isActive())
            .count();
        
        stats.put("totalEstimatedHours", totalEstimatedHours);
        stats.put("totalActualHours", totalActualHours);
        stats.put("availableMembers", availableMembers);
        stats.put("onLeaveMembers", onLeaveMembers);
        stats.put("inactiveMembers", inactiveMembers);
        stats.put("totalMembers", project.getMembers().size());
        
        int gapHours = 0;
        if (availableMembers > 0) {
            double avgHoursPerMember = totalEstimatedHours / (double) availableMembers;
            if (avgHoursPerMember > 160) {
                gapHours = totalEstimatedHours - (int) (availableMembers * 160);
            }
        } else if (totalEstimatedHours > 0) {
            gapHours = totalEstimatedHours;
        }
        stats.put("gapHours", gapHours);
        stats.put("hasGap", gapHours > 0);
        
        List<Map<String, Object>> taskAssignments = new ArrayList<>();
        for (Task task : project.getTasks()) {
            Map<String, Object> assignment = new HashMap<>();
            assignment.put("taskId", task.getId());
            assignment.put("taskName", task.getName());
            assignment.put("assigneeId", task.getAssigneeId());
            assignment.put("assigneeName", "");
            assignment.put("estimatedHours", task.getEstimatedHours());
            assignment.put("status", task.getStatus());
            
            if (task.getAssigneeId() != null) {
                Member member = DataStore.members.get(task.getAssigneeId());
                if (member != null) {
                    assignment.put("assigneeName", member.getName());
                    assignment.put("memberActive", member.isActive());
                    assignment.put("onLeave", !member.getLeaveRecords().isEmpty());
                }
            }
            taskAssignments.add(assignment);
        }
        stats.put("taskAssignments", taskAssignments);
        
        List<Map<String, Object>> memberWorkload = new ArrayList<>();
        for (Member member : project.getMembers()) {
            Map<String, Object> workload = new HashMap<>();
            workload.put("memberId", member.getId());
            workload.put("memberName", member.getName());
            workload.put("active", member.isActive());
            workload.put("onLeave", !member.getLeaveRecords().isEmpty());
            
            int memberHours = project.getTasks().stream()
                .filter(t -> member.getId().equals(t.getAssigneeId()))
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .mapToInt(Task::getEstimatedHours)
                .sum();
            
            workload.put("assignedHours", memberHours);
            workload.put("workloadLevel", memberHours > 160 ? "OVERLOAD" : (memberHours > 80 ? "NORMAL" : "LOW"));
            memberWorkload.add(workload);
        }
        stats.put("memberWorkload", memberWorkload);
        
        return stats;
    }

    public List<LeaveRecord> getLeaveRecords(String projectId) {
        Project project = DataStore.projects.get(projectId);
        if (project == null) return new ArrayList<>();
        
        List<LeaveRecord> allRecords = new ArrayList<>();
        for (Member member : project.getMembers()) {
            allRecords.addAll(member.getLeaveRecords());
        }
        return allRecords;
    }
}
