package com.example.timesheet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "designations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"resources", "designationAuditLogs"})
public class Designations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designation_id")
    private Integer designationId;

    @NotBlank(message = "Designation name is required")
    @Size(max = 255, message = "Designation name cannot exceed 255 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]+$", message = "Designation name can only contain letters, numbers, spaces, and hyphens")
    @Column(name = "designation_name", nullable = false, length = 255)
    private String designationName;

    @NotNull(message = "Daily cost is required")
    @DecimalMin(value = "0.01", message = "Daily cost must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Daily cost must have at most 10 digits and 2 decimal places")
    @Column(name = "cost_per_hour", precision = 10, scale = 2)
    private BigDecimal costPerHour;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "designation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DesignationAuditLog> designationAuditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "designation")
    private Set<Resources> resources;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}





