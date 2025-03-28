package com.example.timesheet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.*;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "allocations", schema = "public")
public class Allocations {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Integer allocationId;

    @NotNull(message = "Resource ID is required")
    @Column(name = "resource_id")
    private Integer resourceId;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id")
    private Integer projectId;

    @NotNull(message = "PO ID is required")
    @Column(name = "po_id", nullable = false)
    private Integer poId;

    @NotNull(message = "Allocated hours is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated hours must be greater than 0")
    @DecimalMax(value = "2080.0", message = "Allocated hours cannot exceed 2080 (annual working hours)")
    @Digits(integer = 4, fraction = 2, message = "Allocated hours must have at most 4 digits and 2 decimal places")
    @Column(name = "allocated_hours")
    private BigDecimal allocatedHours;

    @NotNull(message = "Allocated cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated cost must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Allocated cost must have at most 10 digits and 2 decimal places")
    @Column(name = "allocated_cost")
    private BigDecimal allocatedCost;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "resource_id", referencedColumnName = "resource_id", insertable = false, updatable = false)
    private Resources resource;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", insertable = false, updatable = false)
    private ProjectDetails project;

    @ManyToOne
    @JoinColumn(name = "po_id", referencedColumnName = "po_id", insertable = false, updatable = false)
    private PurchaseOrders purchaseOrder;

    @OneToMany(mappedBy = "allocation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Timesheet> timesheets;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }

}