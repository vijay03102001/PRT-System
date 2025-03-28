package com.example.timesheet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timesheets", schema = "public")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timesheets_timesheet_id_seq")
    @SequenceGenerator(name = "timesheets_timesheet_id_seq", sequenceName = "timesheets_timesheet_id_seq", allocationSize = 1)
    @Column(name = "timesheet_id", nullable = false)
    private Integer timesheetId;

    @NotNull(message = "Resource ID is required")
    @Column(name = "resource_id", nullable = false)
    private Integer resourceId;

    @NotNull(message = "Allocation ID is required")
    @Column(name = "allocation_id", nullable = false)
    private Integer allocationId;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @NotNull(message = "PO ID is required")
    @Column(name = "po_id", nullable = false)
    private Integer poId;

    @NotNull(message = "Work date is required")
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.00", message = "Hours worked must be greater than or equal to 0")
    @DecimalMax(value = "24.00", message = "Hours worked must be realistic for a single day")
    @Column(name = "hours_worked", precision = 5, scale = 2, nullable = false)
    private BigDecimal hoursWorked;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.00", message = "Cost must be greater than or equal to 0")
    @Column(name = "cost", precision = 12, scale = 2, nullable = false)
    private BigDecimal cost;

    public enum TimesheetStatus {
        Pending,
        Approved,
        Rejected
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimesheetStatus status = TimesheetStatus.Pending; // Default value

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", referencedColumnName = "resource_id", insertable = false, updatable = false)
    private Resources resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", insertable = false, updatable = false)
    private ProjectDetails project;

    @ManyToOne
    @JoinColumn(name = "po_id", referencedColumnName = "po_id", insertable = false, updatable = false)
    private PurchaseOrders purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "allocation_id", referencedColumnName = "allocation_id", insertable = false, updatable = false)
    private Allocations allocation;

    @PrePersist
    @PreUpdate
    public void calculateCost() {
        if (resource != null && resource.getDesignation() != null) {
            BigDecimal designationCost = resource.getDesignation().getCostPerHour();
            if (designationCost != null && hoursWorked != null) {
                this.cost = hoursWorked.multiply(designationCost);
            }
        }
    }
}
