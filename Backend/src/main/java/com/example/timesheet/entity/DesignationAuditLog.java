package com.example.timesheet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "designation_audit_logs", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"designation"}) // Prevent circular reference
public class DesignationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "designation_audit_logs_audit_id_seq")
    @SequenceGenerator(name = "designation_audit_logs_audit_id_seq", sequenceName = "designation_audit_logs_audit_id_seq", allocationSize = 1)
    @Column(name = "audit_id", nullable = false)
    private Integer auditId;

    @NotNull(message = "Old cost is required")
    @Column(name = "old_cost", precision = 12, scale = 2, nullable = false)
    private BigDecimal oldCost;

    @NotNull(message = "New cost is required")
    @Column(name = "new_cost", precision = 12, scale = 2, nullable = false)
    private BigDecimal newCost;

    @NotBlank(message = "Updated by name is required")
    @Size(max = 255, message = "Updated by name must not exceed 255 characters")
    @Column(name = "updated_by_name", nullable = false, length = 255)
    private String updatedByName;

    @NotBlank(message = "Updated by designation is required")
    @Size(max = 255, message = "Updated by designation must not exceed 255 characters")
    @Column(name = "updated_by_designation", nullable = false, length = 255)
    private String updatedByDesignation;

    @Column(name = "updated_at", nullable = false)
    @CreationTimestamp // Automatically sets the creation timestamp
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", referencedColumnName = "designation_id", nullable = false)
    private Designations designation;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
