package com.example.timesheet.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timesheet.dto.ResourceCostDTO;
import com.example.timesheet.service.ResourceCostService;

import jakarta.validation.Valid;

@ControllerAdvice
@RestController
@RequestMapping("/api/designations")
public class ResourceCostController {

    private final ResourceCostService resourceCostService;

    public ResourceCostController(ResourceCostService resourceCostService) {
        this.resourceCostService = resourceCostService;
    }

    // Get all designations with audit logs
    @GetMapping
    public ResponseEntity<List<ResourceCostDTO>> getAllResourceCosts() {
        return ResponseEntity.ok(resourceCostService.getAllResourceCosts());
    }

    // Get all audit logs
    @GetMapping("/audit-logs")
    public ResponseEntity<List<ResourceCostDTO.DesignationAuditLogDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(resourceCostService.getAllAuditLogs());
    }

    // Get designation with audit logs
    @GetMapping("/{id}")
    public ResponseEntity<ResourceCostDTO> getResourceCostDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(resourceCostService.getResourceCostDetails(id));
    }

    // Create new designation
    @PostMapping
    public ResponseEntity<ResourceCostDTO> createResourceCost(@Valid @RequestBody ResourceCostDTO resourceCostDTO) {
        return ResponseEntity.ok(resourceCostService.createResourceCost(resourceCostDTO));
    }

    // Update designation
    @PutMapping("/{id}")
    public ResponseEntity<ResourceCostDTO> updateResourceCost(@PathVariable Integer id,
        @Valid @RequestBody ResourceCostDTO resourceCostDTO) {
        return ResponseEntity.ok(resourceCostService.updateResourceCost(id, resourceCostDTO));
    }

    // Delete designation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResourceCost(@PathVariable Integer id) {
        resourceCostService.deleteResourceCost(id);
        return ResponseEntity.noContent().build();
    }

    // Get audit logs for a designation
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<ResourceCostDTO.DesignationAuditLogDTO>> getAuditLogs(@PathVariable Integer id) {
        return ResponseEntity.ok(resourceCostService.getAllAuditLogsByDesignationId(id));
    }
}