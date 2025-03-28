package com.example.timesheet.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Integer projectId;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 255)
    private String projectName;

    @NotNull(message = "Project budget is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal projectBudget;

    @Size(max = 1000)
    private String projectDescription;

    private ZonedDateTime createdAt;

    private Integer totalPOs;
    private BigDecimal utilizedAmount;
    private BigDecimal remainingBalance; // Remaining balance = projectBudget - utilizedAmount
    private List<PurchaseOrderDTO> purchaseOrders;
}
