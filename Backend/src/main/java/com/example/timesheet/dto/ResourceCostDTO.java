package com.example.timesheet.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCostDTO {

    private Integer designationId;

    @NotBlank(message = "Designation name is required.")
    private String designationName;

    @NotNull(message = "Daily cost is required.")
    @DecimalMin(value = "0.01", inclusive = false, message = "Daily cost must be greater than 0.")
    private BigDecimal costPerHour;

    private ZonedDateTime createdAt;

    // List of DesignationAuditLogDTO for audit logs
    private List<DesignationAuditLogDTO> auditLogs = new ArrayList<>();

    // Nested DTO for Designation Audit Logs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignationAuditLogDTO {

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

    public Date getResourceId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResourceId'");
    }

    public Date getResourceName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResourceName'");
    }

    public Date getEmployeeId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEmployeeId'");
    }
}
