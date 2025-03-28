package com.example.timesheet.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationDTO {

    @Null(message = "Allocation ID should not be provided")
    private Integer allocationId;

    @NotNull(message = "Resource ID is required")
    private Integer resourceId;

    private String resourceName;  // Resource name, can be fetched using resourceId in service

    private String designationName;  // Designation name, fetched using resourceId

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    private String projectName;

    @NotNull(message = "PO ID is required")
    private Integer poId;

    private String poNumber;

    @NotNull(message = "Allocated hours is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated hours must be greater than 0")
    @DecimalMax(value = "2080.0", inclusive = true, message = "Allocated hours cannot exceed 2080")
    private BigDecimal allocatedHours;

    @NotNull(message = "Allocated cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated cost must be greater than 0")
    private BigDecimal allocatedCost;  // Should be equal to the PO value

    private ZonedDateTime createdAt;

    // New fields for Resource Utilization
    private BigDecimal totalHoursWorked;  // Summed from Timesheet
    private BigDecimal totalCost;
}
