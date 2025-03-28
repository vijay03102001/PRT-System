package com.example.timesheet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDTO {

    private Integer timesheetId;

    @NotNull(message = "Resource ID is required")
    private Integer resourceId;

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    @NotNull(message = "PO ID is required")
    private Integer poId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.00", message = "Hours worked must be greater than or equal to 0")
    @DecimalMax(value = "999.99", message = "Hours worked must be less than 1000")
    private BigDecimal hoursWorked;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.00", message = "Cost must be greater than or equal to 0")
    private BigDecimal cost;

    @NotNull(message = "Status is required")
    private String status = "Pending"; // Default value

    private ZonedDateTime createdAt;

    // Additional fields for related entities
    private String resourceName;
    private String projectName;
    private String poNumber;
    private BigDecimal costPerHour;

    // Consider using enum for status
    public enum TimesheetStatus {
        Pending,
        Approved,
        Rejected
    }
}
