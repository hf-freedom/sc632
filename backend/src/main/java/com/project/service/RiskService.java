package com.project.service;

import com.project.enums.RiskLevel;
import com.project.enums.RiskStatus;
import com.project.model.*;
import com.project.store.DataStore;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RiskService {

    public List<Risk> getRisksByProject(String projectId) {
        List<Risk> result = new ArrayList<>();
        for (Risk risk : DataStore.risks.values()) {
            if (projectId.equals(risk.getProjectId())) {
                result.add(risk);
            }
        }
        return result;
    }

    public Risk handleRisk(String riskId, String action, String handlerId, String result) {
        Risk risk = DataStore.risks.get(riskId);
        if (risk == null) return null;
        
        RiskAction riskAction = new RiskAction();
        riskAction.setId(UUID.randomUUID().toString());
        riskAction.setRiskId(riskId);
        riskAction.setAction(action);
        riskAction.setHandlerId(handlerId);
        riskAction.setResult(result);
        riskAction.setCreatedAt(LocalDateTime.now());
        
        risk.getActions().add(riskAction);
        risk.setUpdatedAt(LocalDateTime.now());
        
        recalculateRiskLevel(risk);
        
        return risk;
    }

    private void recalculateRiskLevel(Risk risk) {
        int actionCount = risk.getActions().size();
        if (actionCount >= 3) {
            risk.setLevel(RiskLevel.LOW);
            risk.setStatus(RiskStatus.CLOSED);
        } else if (actionCount >= 2) {
            risk.setLevel(RiskLevel.MEDIUM);
            risk.setStatus(RiskStatus.IN_PROGRESS);
        } else if (actionCount >= 1) {
            risk.setLevel(RiskLevel.MEDIUM);
            risk.setStatus(RiskStatus.IN_PROGRESS);
        }
    }

    public List<TodoItem> getTodoItems(String projectId) {
        List<TodoItem> result = new ArrayList<>();
        for (TodoItem todo : DataStore.todoItems.values()) {
            if (projectId.equals(todo.getProjectId())) {
                result.add(todo);
            }
        }
        return result;
    }

    public TodoItem completeTodo(String todoId) {
        TodoItem todo = DataStore.todoItems.get(todoId);
        if (todo == null) return null;
        todo.setCompleted(true);
        return todo;
    }
}
