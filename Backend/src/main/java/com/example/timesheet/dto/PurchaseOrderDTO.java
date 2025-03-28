package com.example.timesheet.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PurchaseOrderDTO {
    private Integer poId;

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    @NotBlank(message = "PO number is required")
    @Pattern(regexp = "^[A-Za-z0-9-]+$")
    private String poNumber;

    @NotNull(message = "Value is required")
    @DecimalMin("0.01")
    private BigDecimal value;
    private BigDecimal poUtilized;
    private BigDecimal poBalance;

    private ZonedDateTime createdAt;

}


