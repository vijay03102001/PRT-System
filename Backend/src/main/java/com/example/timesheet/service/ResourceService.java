package com.example.timesheet.service;

import com.example.timesheet.dto.*;
import com.example.timesheet.entity.Designations;
import com.example.timesheet.entity.Resources;
import com.example.timesheet.repository.ResourceRepository;
import com.example.timesheet.repository.DesignationsRepository;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.exception.BusinessValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final DesignationsRepository designationsRepository;

    public ResourceService(ResourceRepository resourceRepository, DesignationsRepository designationsRepository) {
        this.resourceRepository = resourceRepository;
        this.designationsRepository = designationsRepository;
    }

    public ResourceDTO createResource(ResourceDTO dto) {
        // Find designation ID by name if only the name is provided
        if (dto.getDesignationName() != null && dto.getDesignationId() == null) {
            Designations designation = designationsRepository
                    .findByDesignationName(dto.getDesignationName())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found with name: " + dto.getDesignationName()));
            dto.setDesignationId(designation.getDesignationId());
        }

        validateResource(dto);
        Resources resource = convertToEntity(dto);
        resource.setCreatedAt(ZonedDateTime.now());
        return convertToDTO(resourceRepository.save(resource));
    }

    public Optional<ResourceDTO> findById(Integer id) {
        return resourceRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<ResourceDTO> findAll() {
        return resourceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ResourceDTO updateResource(Integer id, ResourceDTO dto) {
        // Find designation ID by name if only the name is provided
        if (dto.getDesignationName() != null && dto.getDesignationId() == null) {
            Designations designation = designationsRepository
                    .findByDesignationName(dto.getDesignationName())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found with name: " + dto.getDesignationName()));
            dto.setDesignationId(designation.getDesignationId());
        }

        return resourceRepository.findById(id)
                .map(resource -> {
                    updateEntityFromDTO(resource, dto);
                    return convertToDTO(resourceRepository.save(resource));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    public void deleteResource(Integer id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
    }

    private void validateResource(ResourceDTO dto) {
        if (dto == null) {
            throw new BusinessValidationException("Resource DTO cannot be null");
        }

        if (dto.getResourceName() == null || dto.getResourceName().trim().isEmpty()) {
            throw new BusinessValidationException("Resource name is required");
        }

        if (dto.getEmployeeId() == null) {
            throw new BusinessValidationException("Employee ID is required");
        }

        if (dto.getDesignationId() == null) {
            throw new BusinessValidationException("Designation ID is required");
        }

        // Validate the existence of the designation ID using DesignationsRepository
        if (!designationsRepository.existsById(dto.getDesignationId())) {
            throw new ResourceNotFoundException("Designation not found");
        }
    }

    private Resources convertToEntity(ResourceDTO dto) {
        Resources resource = new Resources();
        resource.setResourceName(dto.getResourceName());
        resource.setEmployeeId(dto.getEmployeeId());
        resource.setContactDetails(dto.getContactDetails());
        resource.setDesignationId(dto.getDesignationId());
        return resource;
    }

    private ResourceDTO convertToDTO(Resources entity) {
        ResourceDTO dto = new ResourceDTO();
        dto.setResourceId(entity.getResourceId());
        dto.setResourceName(entity.getResourceName());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setContactDetails(entity.getContactDetails());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDesignationId(entity.getDesignationId());

        // Fetch designation name directly from the Designations repository
        if (entity.getDesignationId() != null) {
            Designations designation = designationsRepository.findById(entity.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found for ID: " + entity.getDesignationId()));
            dto.setDesignationName(designation.getDesignationName());
        }

        return dto;
    }

    private void updateEntityFromDTO(Resources resource, ResourceDTO dto) {
        resource.setResourceName(dto.getResourceName());
        resource.setEmployeeId(dto.getEmployeeId());
        resource.setContactDetails(dto.getContactDetails());
        resource.setDesignationId(dto.getDesignationId());
    }

    public boolean existsById(Integer id) {
        return resourceRepository.existsById(id);
    }

}
