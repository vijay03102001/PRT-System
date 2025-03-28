package com.example.timesheet.entity;

import java.time.ZonedDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "resources", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"designation", "allocations"})
public class Resources {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Integer resourceId;

    @NotBlank(message = "Resource name is required.")
    @Size(max = 255, message = "Resource name cannot exceed 255 characters.")
    @Column(name = "resource_name", nullable = false, length = 255)
    private String resourceName;

    @NotNull(message = "Employee ID is required.")
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "contact_details", columnDefinition = "text")
    private String contactDetails;

    @PastOrPresent(message = "Created date cannot be in the future.")
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Getter
    @NotNull(message = "Designation ID is required.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id", referencedColumnName = "designation_id", nullable = false)
    private Designations designation;

    // The designationId field is implicitly mapped through the relationship
    public Integer getDesignationId() {
        return designation != null ? designation.getDesignationId() : null;
    }

    public void setDesignationId(Integer designationId) {
        if (designation != null) {
            designation.setDesignationId(designationId);
        }
    }

    @OneToMany(mappedBy = "resource")
    private Set<Allocations> allocations;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectDetails project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    private PurchaseOrders purchaseOrder;
}
