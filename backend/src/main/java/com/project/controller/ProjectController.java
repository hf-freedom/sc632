package com.project.controller;

import com.project.model.*;
import com.project.service.ProjectService;
import com.project.service.RiskService;
import com.project.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        return ResponseEntity.ok(projectService.createProject(project));
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable String id) {
        Project project = projectService.getProject(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}/dates")
    public ResponseEntity<Project> updateProjectDates(
            @PathVariable String id,
            @RequestBody Map<String, String> dates) {
        LocalDate startDate = LocalDate.parse(dates.get("startDate"));
        LocalDate endDate = LocalDate.parse(dates.get("endDate"));
        Project project = projectService.updateProjectDates(id, startDate, endDate);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{projectId}/phases")
    public ResponseEntity<Phase> addPhase(@PathVariable String projectId, @RequestBody Phase phase) {
        Phase created = projectService.addPhase(projectId, phase);
        if (created == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(created);
    }

    @PutMapping("/phases/{phaseId}")
    public ResponseEntity<Phase> updatePhaseDates(
            @PathVariable String phaseId,
            @RequestBody Map<String, String> dates) {
        Phase updated = projectService.updatePhaseDates(phaseId, dates);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<Task> addTask(@PathVariable String projectId, @RequestBody Task task) {
        Task created = projectService.addTask(projectId, task);
        if (created == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(created);
    }

    @PutMapping("/tasks/{taskId}/progress")
    public ResponseEntity<?> updateTaskProgress(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> progressData) {
        try {
            int progress = (int) progressData.get("progress");
            LocalDate actualStartDate = progressData.containsKey("actualStartDate") 
                ? LocalDate.parse((String) progressData.get("actualStartDate")) : null;
            LocalDate actualEndDate = progressData.containsKey("actualEndDate") 
                ? LocalDate.parse((String) progressData.get("actualEndDate")) : null;
            
            Task task = projectService.updateTaskProgress(taskId, progress, actualStartDate, actualEndDate);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(task);
        } catch (IllegalStateException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    @PostMapping("/{projectId}/milestones")
    public ResponseEntity<Milestone> addMilestone(@PathVariable String projectId, @RequestBody Milestone milestone) {
        Milestone created = projectService.addMilestone(projectId, milestone);
        if (created == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<Member> addMember(@PathVariable String projectId, @RequestBody Member member) {
        Member created = projectService.addMember(projectId, member);
        if (created == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(created);
    }

    @PostMapping("/members/{memberId}/leave")
    public ResponseEntity<Void> addLeaveRecord(@PathVariable String memberId, @RequestBody LeaveRecord record) {
        record.setMemberId(memberId);
        projectService.addLeaveRecord(record);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/members/{memberId}/deactivate")
    public ResponseEntity<Void> deactivateMember(@PathVariable String memberId) {
        projectService.handleMemberLeave(memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}/risks")
    public ResponseEntity<List<Risk>> getRisks(@PathVariable String projectId) {
        return ResponseEntity.ok(riskService.getRisksByProject(projectId));
    }

    @PostMapping("/risks/{riskId}/handle")
    public ResponseEntity<Risk> handleRisk(
            @PathVariable String riskId,
            @RequestBody Map<String, String> data) {
        String action = data.get("action");
        String handlerId = data.get("handlerId");
        String result = data.get("result");
        Risk risk = riskService.handleRisk(riskId, action, handlerId, result);
        if (risk == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(risk);
    }

    @GetMapping("/{projectId}/todos")
    public ResponseEntity<List<TodoItem>> getTodos(@PathVariable String projectId) {
        return ResponseEntity.ok(riskService.getTodoItems(projectId));
    }

    @PutMapping("/todos/{todoId}/complete")
    public ResponseEntity<TodoItem> completeTodo(@PathVariable String todoId) {
        TodoItem todo = riskService.completeTodo(todoId);
        if (todo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Statistics> getStatistics() {
        return ResponseEntity.ok(statisticsService.calculateStatistics());
    }

    @GetMapping("/{projectId}/manpower-stats")
    public ResponseEntity<Map<String, Object>> getManpowerStats(@PathVariable String projectId) {
        Map<String, Object> stats = projectService.getManpowerStats(projectId);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{projectId}/leave-records")
    public ResponseEntity<List<LeaveRecord>> getLeaveRecords(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getLeaveRecords(projectId));
    }
}
