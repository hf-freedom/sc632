package com.project.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRecord {
    private String id;
    private String memberId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
