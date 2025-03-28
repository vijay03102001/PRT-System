package com.example.timesheet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timesheet.dto.TimesheetDTO;
import com.example.timesheet.entity.Allocations;
import com.example.timesheet.entity.Designations;
import com.example.timesheet.entity.ProjectDetails;
import com.example.timesheet.entity.PurchaseOrders;
import com.example.timesheet.entity.Resources;
import com.example.timesheet.entity.Timesheet;
import com.example.timesheet.exception.BusinessValidationException;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.repository.AllocationRepository;
import com.example.timesheet.repository.ProjectRepository;
import com.example.timesheet.repository.PurchaseOrdersRepository;
import com.example.timesheet.repository.ResourceRepository;
import com.example.timesheet.repository.TimesheetRepository;

@Service
@Transactional
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ResourceRepository resourceRepository;
    private final ProjectRepository projectRepository;
    private final PurchaseOrdersRepository purchaseOrdersRepository;
    private final AllocationRepository allocationRepository;
    private boolean isTimesheetFrozen = false;
    private static final BigDecimal BUDGET_THRESHOLD = new BigDecimal("0.8");

    public TimesheetService(TimesheetRepository timesheetRepository,
                            ResourceRepository resourceRepository,
                            ProjectRepository projectRepository,
                            PurchaseOrdersRepository purchaseOrdersRepository,
                            AllocationRepository allocationRepository) {
        this.timesheetRepository = timesheetRepository;
        this.resourceRepository = resourceRepository;
        this.projectRepository = projectRepository;
        this.purchaseOrdersRepository = purchaseOrdersRepository;
        this.allocationRepository = allocationRepository;
    }

    public void setTimesheetFrozen(boolean frozen) {
        this.isTimesheetFrozen = frozen;
    }

    public boolean isTimesheetFrozen() {
        return isTimesheetFrozen;
    }

    public Timesheet saveTimesheet(Timesheet timesheet) {
        // Fetch allocation(s) based on projectId and poId
        List<Allocations> allocations = allocationRepository.findAllocationsByProjectIdAndPoId(
                timesheet.getProjectId(), timesheet.getPoId());

        if (allocations.isEmpty()) {
            throw new IllegalArgumentException("No allocation found for the given Project ID and PO ID.");
        }

        // Summing up the total allocated hours and cost from all allocations found
        BigDecimal totalAllocatedHours = allocations.stream()
                .map(Allocations::getAllocatedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAllocatedCost = allocations.stream()
                .map(Allocations::getAllocatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ensure hoursWorked does not exceed allocatedHours
        if (timesheet.getHoursWorked().compareTo(totalAllocatedHours) > 0) {
            throw new IllegalArgumentException("Hours worked exceed the allocated hours for this project and PO.");
        }

        // Calculate the cost before saving
        BigDecimal designationCost = timesheet.getResource().getDesignation().getCostPerHour();
        BigDecimal calculatedCost = timesheet.getHoursWorked().multiply(designationCost);

        // Ensure cost does not exceed allocatedCost
        if (calculatedCost.compareTo(totalAllocatedCost) > 0) {
            throw new IllegalArgumentException("Cost exceeds the allocated budget for this project and PO.");
        }

        timesheet.setCost(calculatedCost);

        return timesheetRepository.save(timesheet);
    }

    // Find Timesheet by ID
    public TimesheetDTO findById(Integer id) {
        return timesheetRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found"));
    }

    // Find Timesheets by Resource ID
    public List<TimesheetDTO> findByResourceId(Integer resourceId) {
        return timesheetRepository.findByResourceId(resourceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TimesheetDTO> findAll() {
        return timesheetRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Find Timesheets by Project ID
    public List<TimesheetDTO> findByProjectId(Integer projectId) {
        return timesheetRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Find Timesheets by PO ID
    public List<TimesheetDTO> findByPoId(Integer poId) {
        return timesheetRepository.findByPoId(poId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Find Timesheets by Date Range
    public List<TimesheetDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.findByWorkDateBetween(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get All Timesheets
    public List<TimesheetDTO> getAllTimesheets() {
        return timesheetRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Create Timesheet
    public TimesheetDTO createTimesheet(TimesheetDTO dto) {
        checkTimesheetFrozen();
        validateTimesheet(dto);

        Resources resource = getResourceWithValidation(dto.getResourceId());
        BigDecimal calculatedCost = calculateCost(resource, dto.getHoursWorked());
        dto.setCost(calculatedCost);

        Timesheet timesheet = convertToEntity(dto);
        timesheet.setCreatedAt(ZonedDateTime.now());
        timesheet.setStatus(Timesheet.TimesheetStatus.Pending);

        Timesheet savedTimesheet = timesheetRepository.save(timesheet);
        updateProjectAndPOBudgets(resource, calculatedCost);

        return convertToDTO(savedTimesheet);
    }

    // Update Timesheet
    public TimesheetDTO updateTimesheet(Integer id, TimesheetDTO dto) {
        checkTimesheetFrozen();

        // Find existing timesheet
        Timesheet existingTimesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found"));

        validateTimesheet(dto);

        Resources resource = getResourceWithValidation(dto.getResourceId());
        BigDecimal calculatedCost = calculateCost(resource, dto.getHoursWorked());
        dto.setCost(calculatedCost);

        // Update existing timesheet
        updateEntityFromDTO(existingTimesheet, dto);
        existingTimesheet.setCreatedAt(ZonedDateTime.now());
        existingTimesheet.setStatus(Timesheet.TimesheetStatus.Pending);

        Timesheet savedTimesheet = timesheetRepository.save(existingTimesheet);
        updateProjectAndPOBudgets(resource, calculatedCost);

        return convertToDTO(savedTimesheet);
    }

    // Delete Timesheet
    public void deleteTimesheet(Integer id) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found"));

        timesheetRepository.delete(timesheet);
    }

    // Existing methods from previous implementation
    private void checkTimesheetFrozen() {
        if (isTimesheetFrozen) {
            throw new BusinessValidationException("Timesheet is currently frozen");
        }
    }

    private void validateTimesheet(TimesheetDTO dto) {
        if (dto.getResourceId() == null || dto.getProjectId() == null) {
            throw new BusinessValidationException("Resource ID and Project ID are required");
        }
        if (!resourceRepository.existsById(dto.getResourceId())) {
            throw new ResourceNotFoundException("Resource not found with id: " + dto.getResourceId());
        }
        if (!projectRepository.existsById(dto.getProjectId())) {
            throw new ResourceNotFoundException("Project not found with id: " + dto.getProjectId());
        }
        if (dto.getHoursWorked() != null && (dto.getHoursWorked().doubleValue() < 0 ||
                dto.getHoursWorked().doubleValue() > 24)) {
            throw new BusinessValidationException("Hours worked must be between 0 and 24");
        }
        if (dto.getPoId() == null) {
            throw new BusinessValidationException("PO ID is required");
        }
    }

    private Timesheet convertToEntity(TimesheetDTO dto) {
        Timesheet timesheet = new Timesheet();
        timesheet.setResourceId(dto.getResourceId());
        timesheet.setProjectId(dto.getProjectId());
        timesheet.setPoId(dto.getPoId());
        timesheet.setWorkDate(dto.getWorkDate());
        timesheet.setHoursWorked(dto.getHoursWorked());
        timesheet.setCost(dto.getCost());
        if (dto.getStatus() != null) {
            timesheet.setStatus(Timesheet.TimesheetStatus.valueOf(dto.getStatus()));
        }
        // Link PO to Timesheet
        PurchaseOrders purchaseOrder = purchaseOrdersRepository.findById(dto.getPoId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + dto.getPoId()));
        timesheet.setPurchaseOrder(purchaseOrder);
        return timesheet;
    }

    private void updateEntityFromDTO(Timesheet timesheet, TimesheetDTO dto) {
        timesheet.setResourceId(dto.getResourceId());
        timesheet.setProjectId(dto.getProjectId());
        timesheet.setPoId(dto.getPoId());
        timesheet.setWorkDate(dto.getWorkDate());
        timesheet.setHoursWorked(dto.getHoursWorked());
        timesheet.setCost(dto.getCost());
        if (dto.getStatus() != null) {
            timesheet.setStatus(Timesheet.TimesheetStatus.valueOf(dto.getStatus()));
        }
    }

    // Existing methods from previous implementation remain the same
    private Resources getResourceWithValidation(Integer resourceId) {
        return resourceRepository.findResourceWithDesignationCost(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    private BigDecimal calculateCost(Resources resource, BigDecimal hoursWorked) {
        Designations designation = resource.getDesignation();
        if (designation == null || designation.getCostPerHour() == null) {
            throw new BusinessValidationException("Cost per hour not configured for resource");
        }
        return designation.getCostPerHour().multiply(hoursWorked);
    }

    private void updateProjectAndPOBudgets(Resources resource, BigDecimal cost) {
        ProjectDetails project = resource.getProject();
        if (project != null) {
            updateProjectBudget(project, cost);
        }

        PurchaseOrders po = resource.getPurchaseOrder();
        if (po != null) {
            updatePOBudget(po, cost);
        }
    }

    private void updateProjectBudget(ProjectDetails project, BigDecimal cost) {
        BigDecimal newUtilized = project.getUtilizedAmount().add(cost);
        project.setUtilizedAmount(newUtilized);
        project.setRemainingBalance(project.getProjectBudget().subtract(newUtilized).max(BigDecimal.ZERO));

        if (isOverThreshold(newUtilized, project.getProjectBudget())) {
            triggerAlarm("Project budget utilization has exceeded 80% for project: " + project.getProjectName());
        }
        projectRepository.save(project);
    }

    private void updatePOBudget(PurchaseOrders po, BigDecimal cost) {
        BigDecimal newUtilized = po.getPoUtilized().add(cost);
        po.setPoUtilized(newUtilized);
        po.setPoBalance(po.getValue().subtract(newUtilized).max(BigDecimal.ZERO));

        if (isOverThreshold(newUtilized, po.getValue())) {
            triggerAlarm("PO budget utilization has exceeded 80% for PO: " + po.getPoNumber());
        }
        purchaseOrdersRepository.save(po);
    }

    private boolean isOverThreshold(BigDecimal utilized, BigDecimal total) {
        return utilized.compareTo(total.multiply(BUDGET_THRESHOLD)) > 0;
    }

    private void triggerAlarm(String message) {
        // TODO: Replace with actual notification logic
        System.out.println("ALARM: " + message);
    }

    private TimesheetDTO convertToDTO(Timesheet entity) {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setTimesheetId(entity.getTimesheetId());
        dto.setResourceId(entity.getResourceId());
        dto.setProjectId(entity.getProjectId());
        dto.setPoId(entity.getPoId());
        dto.setWorkDate(entity.getWorkDate());
        dto.setHoursWorked(entity.getHoursWorked());
        dto.setCost(entity.getCost());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt());
        // Set related entity information if available
        if (entity.getResource() != null) {
            dto.setResourceName(entity.getResource().getResourceName());
        }
        if (entity.getProject() != null) {
            dto.setProjectName(entity.getProject().getProjectName());
        }
        if (entity.getPurchaseOrder() != null ) {
            dto.setPoNumber(entity.getPurchaseOrder().getPoNumber());
        }
        return dto;
    }

}