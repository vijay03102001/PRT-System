package com.example.timesheet.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDTO {
    private List<ProjectDTO> projects;
    private BigDecimal totalBudget;
    private BigDecimal totalPOValue;
    private BigDecimal totalUtilized;
    private BigDecimal totalRemaining;
}
