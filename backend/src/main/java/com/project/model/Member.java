package com.project.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Member {
    private String id;
    private String name;
    private String email;
    private String role;
    private boolean active = true;
    private List<LeaveRecord> leaveRecords = new ArrayList<>();
}
