package com.project.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Milestone {
    private String id;
    private String projectId;
    private String name;
    private String description;
    private LocalDate plannedDate;
    private LocalDate actualDate;
    private boolean achieved = false;
    private List<String> dependentTaskIds = new ArrayList<>();
    private int delayDays = 0;
}
