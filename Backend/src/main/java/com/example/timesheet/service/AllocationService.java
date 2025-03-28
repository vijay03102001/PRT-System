package com.example.timesheet.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timesheet.dto.AllocationDTO;
import com.example.timesheet.entity.Allocations;
import com.example.timesheet.entity.ProjectDetails;
import com.example.timesheet.entity.Resources;
import com.example.timesheet.exception.BusinessValidationException;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.repository.AllocationRepository;
import com.example.timesheet.repository.ProjectRepository;
import com.example.timesheet.repository.PurchaseOrdersRepository;
import com.example.timesheet.repository.ResourceRepository;
import com.example.timesheet.repository.TimesheetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final ResourceRepository resourceRepository;
    private final ProjectRepository projectRepository;
    private final PurchaseOrdersRepository poRepository;
    private final TimesheetRepository timesheetRepository;

    @Transactional
    public AllocationDTO createAllocation(AllocationDTO allocationDTO) {
        // Validate resource, project, and PO existence
        validateAllocationInputs(allocationDTO);

        // Set allocated cost to PO value
        BigDecimal poValue = poRepository.getPOValue(allocationDTO.getPoId());
        allocationDTO.setAllocatedCost(poValue);

        // Create and save allocation
        Allocations allocation = convertToEntity(allocationDTO);
        allocation.setCreatedAt(ZonedDateTime.now());
        Allocations savedAllocation = allocationRepository.save(allocation);

        return convertToDTO(savedAllocation);
    }

    public List<AllocationDTO> getAllocations(Integer projectId, Integer poId) {
        List<Allocations> allocations = allocationRepository.findAllocationsByProjectIdAndPoId(projectId, poId);

        if (allocations.isEmpty()) {
            throw new ResourceNotFoundException("No allocations found for the given project and PO.");
        }

        return allocations.stream()
                .map(allocation -> {
                    AllocationDTO dto = convertToDTO(allocation);

                    // Get utilization data from timesheet
                    BigDecimal[] utilizationData = getTotalApprovedHoursAndCost(
                            allocation.getProjectId(),
                            allocation.getPoId(),
                            allocation.getResourceId()
                    );

                    dto.setTotalHoursWorked(utilizationData[0]);
                    dto.setTotalCost(utilizationData[1]);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllUtilization() {
        List<Allocations> allAllocations = allocationRepository.findAll();

        if (allAllocations.isEmpty()) {
            throw new ResourceNotFoundException("No allocations found in the system.");
        }

        return allAllocations.stream()
                .map(allocation -> {
                    AllocationDTO dto = convertToDTO(allocation);

                    // Get utilization data from timesheet
                    BigDecimal[] utilizationData = getTotalApprovedHoursAndCost(
                            allocation.getProjectId(),
                            allocation.getPoId(),
                            allocation.getResourceId()
                    );

                    dto.setTotalHoursWorked(utilizationData[0]);
                    dto.setTotalCost(utilizationData[1]);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal[] getTotalApprovedHoursAndCost(Integer projectId, Integer poId, Integer resourceId) {
        Object[] result = timesheetRepository.findTotalApprovedHoursAndCostByProjectPoAndResource(
                projectId, poId, resourceId
        );

        // Properly handle the Object array results
        BigDecimal totalHoursWorked = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        if (result != null && result.length == 2) {
            // Safely convert the first value (hours)
            if (result[0] != null) {
                if (result[0] instanceof BigDecimal) {
                    totalHoursWorked = (BigDecimal) result[0];
                } else if (result[0] instanceof Number) {
                    totalHoursWorked = new BigDecimal(result[0].toString());
                }
            }

            // Safely convert the second value (cost)
            if (result[1] != null) {
                if (result[1] instanceof BigDecimal) {
                    totalCost = (BigDecimal) result[1];
                } else if (result[1] instanceof Number) {
                    totalCost = new BigDecimal(result[1].toString());
                }
            }
        }

        return new BigDecimal[]{totalHoursWorked, totalCost};
    }

private void validateAllocationInputs(AllocationDTO allocationDTO) {
        // Validate resource exists and is associated with a project
        Resources resource = resourceRepository.findById(allocationDTO.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + allocationDTO.getResourceId()));

        if (resource.getProject() == null) {
            throw new BusinessValidationException("Resource is not associated with any project.");
        }

        // Validate project exists
        projectRepository.findById(allocationDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + allocationDTO.getProjectId()));

        // Validate PO exists
        poRepository.findById(allocationDTO.getPoId())
                .orElseThrow(() -> new ResourceNotFoundException("PO not found with ID: " + allocationDTO.getPoId()));
    }

    private Allocations convertToEntity(AllocationDTO dto) {
        Allocations allocation = new Allocations();
        allocation.setResourceId(dto.getResourceId());
        allocation.setProjectId(dto.getProjectId());
        allocation.setPoId(dto.getPoId());
        allocation.setAllocatedHours(dto.getAllocatedHours());
        allocation.setAllocatedCost(dto.getAllocatedCost());
        return allocation;
    }

    private AllocationDTO convertToDTO(Allocations entity) {
        AllocationDTO dto = new AllocationDTO();
        dto.setAllocationId(entity.getAllocationId());
        dto.setResourceId(entity.getResourceId());
        dto.setProjectId(entity.getProjectId());
        dto.setPoId(entity.getPoId());
        dto.setAllocatedHours(entity.getAllocatedHours());
        dto.setAllocatedCost(entity.getAllocatedCost());
        dto.setCreatedAt(entity.getCreatedAt());

        // Fetch resource details
        Resources resource = resourceRepository.findById(entity.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + entity.getResourceId()));
        dto.setResourceName(resource.getResourceName());
        dto.setDesignationName(resource.getDesignation().getDesignationName());

        // Fetch project details
        ProjectDetails project = projectRepository.findById(entity.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + entity.getProjectId()));
        dto.setProjectName(project.getProjectName());

        // Fetch PO details
        poRepository.findById(entity.getPoId())
                .ifPresent(po -> dto.setPoNumber(po.getPoNumber()));

        return dto;
    }
}