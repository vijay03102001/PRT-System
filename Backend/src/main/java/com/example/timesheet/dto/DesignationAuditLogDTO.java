package com.example.timesheet.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignationAuditLogDTO {
    private Integer auditId;

    @NotNull(message = "Old cost is required.")
    private BigDecimal oldCost;

    @NotNull(message = "New cost is required.")
    private BigDecimal newCost;

    private String updatedByName;
    private String updatedByDesignation;

    private ZonedDateTime updatedAt;
    private String designationName; // Simplified for displaying designation

    @NotNull(message = "Designation ID is required.")
    private Integer designationId; // Add designationId to maintain reference
}
