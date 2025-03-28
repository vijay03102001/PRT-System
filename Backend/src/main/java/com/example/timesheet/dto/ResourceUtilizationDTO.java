package com.example.timesheet.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUtilizationDTO {

    private Integer allocationId;

    @NotNull(message = "Resource ID is required")
    private Integer resourceId;

    @NotBlank(message = "Resource name is required")
    private String resourceName;  // Resource name, populated using resourceId

    @NotNull(message = "Designation ID is required")
    private Integer designationId;

    @NotBlank(message = "Designation name is required")
    private String designationName;  // Designation name, populated using resourceId

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotNull(message = "PO ID is required")
    private Integer poId;

    @NotBlank(message = "PO number is required")
    private String poNumber;

    @NotNull(message = "Allocated hours is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated hours must be greater than 0")
    private BigDecimal allocatedHours;  // From AllocationDTO

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Worked hours must be greater than or equal to 0")
    private BigDecimal hoursWorked;  // Summed from TimesheetDTO

    @NotNull(message = "Utilized amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Utilized amount must be greater than or equal to 0")
    private BigDecimal resourceUtilized;  // Total utilized amount (from PO)

    @NotNull(message = "Allocated cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated cost must be greater than 0")
    private BigDecimal allocatedCost;  // Should match PO value

    private BigDecimal remainingBalance;  // Calculated dynamically: allocatedCost - resourceUtilized
}
