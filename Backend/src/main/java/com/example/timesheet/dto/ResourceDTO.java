package com.example.timesheet.dto;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResourceDTO {
    private Integer resourceId;

    @NotBlank(message = "Resource name is required.")
    private String resourceName;

    @NotNull(message = "Employee ID is required.")
    private Integer employeeId;

    private String contactDetails;

    @PastOrPresent(message = "Created date cannot be in the future.")
    private ZonedDateTime createdAt;

    private String designationName; // Simplified for displaying designation

    private Integer designationId; // Keep the ID for reference if needed

}
