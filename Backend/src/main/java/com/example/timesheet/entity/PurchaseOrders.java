package com.example.timesheet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"allocations", "project"})
@Entity
@Table(name = "purchase_orders", schema = "public")
public class PurchaseOrders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_id")
    private Integer poId;

    @NotBlank(message = "PO number is required")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "PO number must contain only letters, numbers, and hyphens")
    @Size(min = 3, max = 255, message = "PO number must be between 3 and 255 characters")
    @Column(name = "po_number", nullable = false, length = 255)
    private String poNumber;

    @NotNull(message = "PO value is required")
    @DecimalMin(value = "0.01", message = "PO value must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "PO value must have at most 10 digits and 2 decimal places")
    @Column(name = "value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "po_utilized", precision = 12, scale = 2)
    private BigDecimal poUtilized = BigDecimal.ZERO;

    @NotNull
    @Column(name = "po_balance", precision = 12, scale = 2)
    private BigDecimal poBalance;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder")
    private Set<Allocations> allocations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private ProjectDetails project;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}
