package com.example.timesheet.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timesheet.dto.ResourceCostDTO;
import com.example.timesheet.entity.DesignationAuditLog;
import com.example.timesheet.entity.Designations;
import com.example.timesheet.exception.BusinessValidationException;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.repository.DesignationAuditLogRepository;
import com.example.timesheet.repository.DesignationsRepository;
import com.example.timesheet.repository.ProjectRepository;

@Service
@Transactional
public class ResourceCostService {

    @Autowired
    private DesignationsRepository designationsRepository;

    @Autowired
    private DesignationAuditLogRepository auditLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<ResourceCostDTO> getAllResourceCosts() {
        List<Designations> designations = designationsRepository.findAll();
        return designations.stream()
                .map(designation -> {
                    List<DesignationAuditLog> auditLogs =
                            auditLogRepository.findByDesignation_DesignationIdOrderByUpdatedAtDesc(
                                    designation.getDesignationId());

                    ResourceCostDTO dto = new ResourceCostDTO();
                    dto.setDesignationId(designation.getDesignationId());
                    dto.setDesignationName(designation.getDesignationName());
                    dto.setCostPerHour(designation.getCostPerHour());
                    dto.setCreatedAt(designation.getCreatedAt());

                    List<ResourceCostDTO.DesignationAuditLogDTO> auditLogDTOs =
                            auditLogs.stream()
                                    .map(this::mapToAuditLogDTO)
                                    .collect(Collectors.toList());

                    dto.setAuditLogs(auditLogDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ResourceCostDTO.DesignationAuditLogDTO> getAllAuditLogs() {
        List<DesignationAuditLog> allAuditLogs = auditLogRepository.findAll();
        return allAuditLogs.stream()
                .map(this::mapToAuditLogDTO)
                .collect(Collectors.toList());
    }

    public ResourceCostDTO getResourceCostDetails(Integer designationId) {
        Designations designation = designationsRepository.findById(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + designationId));

        // Fetch audit logs from the database (populated by trigger)
        List<DesignationAuditLog> auditLogs = auditLogRepository.findByDesignation_DesignationIdOrderByUpdatedAtDesc(designationId);

        ResourceCostDTO resourceCost = new ResourceCostDTO();
        resourceCost.setDesignationId(designation.getDesignationId());
        resourceCost.setDesignationName(designation.getDesignationName());
        resourceCost.setCostPerHour(designation.getCostPerHour());
        resourceCost.setCreatedAt(designation.getCreatedAt());

        // Map audit logs to DTOs
        List<ResourceCostDTO.DesignationAuditLogDTO> auditLogDTOs = auditLogs.stream()
                .map(this::mapToAuditLogDTO)
                .collect(Collectors.toList());

        resourceCost.setAuditLogs(auditLogDTOs);
        return resourceCost;
    }

    @Transactional
    public ResourceCostDTO updateResourceCost(Integer designationId, ResourceCostDTO resourceCostDTO) {
        Designations designation = designationsRepository.findById(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + designationId));

        // Update designation details (trigger will handle audit logging)
        designation.setDesignationName(resourceCostDTO.getDesignationName());
        designation.setCostPerHour(resourceCostDTO.getCostPerHour());
        designationsRepository.save(designation);

        return getResourceCostDetails(designationId);
    }

    @Transactional
    public ResourceCostDTO createResourceCost(ResourceCostDTO resourceCostDTO) {
        Designations designation = new Designations();
        designation.setDesignationName(resourceCostDTO.getDesignationName());
        designation.setCostPerHour(resourceCostDTO.getCostPerHour());
        designation.setCreatedAt(ZonedDateTime.now());

        designation = designationsRepository.save(designation);
        return getResourceCostDetails(designation.getDesignationId());
    }

    private ResourceCostDTO.DesignationAuditLogDTO mapToAuditLogDTO(DesignationAuditLog auditLog) {
        ResourceCostDTO.DesignationAuditLogDTO dto = new ResourceCostDTO.DesignationAuditLogDTO();
        dto.setAuditId(auditLog.getAuditId());
        dto.setOldCost(auditLog.getOldCost());
        dto.setNewCost(auditLog.getNewCost());
        dto.setUpdatedByName(auditLog.getUpdatedByName());
        dto.setUpdatedByDesignation(auditLog.getUpdatedByDesignation());
        dto.setUpdatedAt(auditLog.getUpdatedAt());
        dto.setDesignationName(auditLog.getDesignation().getDesignationName());

        // Set designationId explicitly
        if (auditLog.getDesignation() != null) {
            dto.setDesignationId(auditLog.getDesignation().getDesignationId());
        }
        return dto;
    }

    @Transactional
    public void deleteResourceCost(Integer designationId) {
        // Check if the designation exists
        if (!designationsRepository.existsById(designationId)) {
            throw new ResourceNotFoundException("Designation not found with id: " + designationId);
        }

        // Check if any active project is using this designation
        boolean isUsedInActiveProject = projectRepository.existsByDesignation_DesignationIdAndIsActive(designationId, true);

        if (isUsedInActiveProject) {
            throw new BusinessValidationException("Cannot delete designation. It is assigned to an active project.");
        }

        // Proceed with deletion
        designationsRepository.deleteById(designationId);
    }

    public List<ResourceCostDTO.DesignationAuditLogDTO> getAllAuditLogsByDesignationId(Integer designationId) {
        List<DesignationAuditLog> auditLogs = auditLogRepository.findByDesignation_DesignationIdOrderByUpdatedAtDesc(designationId);
        return auditLogs.stream()
                .map(this::mapToAuditLogDTO)
                .collect(Collectors.toList());
    }

}