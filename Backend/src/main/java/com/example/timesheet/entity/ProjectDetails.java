package com.example.timesheet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"allocations", "purchaseOrders"})
@Entity
@Table(name = "project_details", schema = "public")
public class ProjectDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 255, message = "Project name must be between 2 and 255 characters")
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @NotNull(message = "Project budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Project budget must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Project budget must have at most 10 digits and 2 decimal places")
    @Column(name = "project_budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectBudget;

    @Size(max = 1000, message = "Project description must not exceed 1000 characters")
    @Column(name = "project_description", columnDefinition = "text")
    private String projectDescription;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "utilized_amount", precision = 12, scale = 2)
    private BigDecimal utilizedAmount = BigDecimal.ZERO;

    @Column(name = "remaining_balance", precision = 12, scale = 2)
    private BigDecimal remainingBalance;

    @ManyToOne
    @JoinColumn(name = "designation_id", referencedColumnName = "designation_id")
    private Designations designation; // Designation object in ProjectDetails

    @OneToMany(mappedBy = "project")
    private Set<Allocations> allocations;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrders> purchaseOrders = new ArrayList<>();

    @NotNull(message = "isActive flag is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Default to true indicating the project is active

    @PrePersist
    @PreUpdate
    private void calculateRemainingBalance() {
        if (projectBudget != null && utilizedAmount != null) {
            this.remainingBalance = projectBudget.subtract(utilizedAmount);
        } else {
            this.remainingBalance = projectBudget; // Default if utilizedAmount is null
        }
    }
}
