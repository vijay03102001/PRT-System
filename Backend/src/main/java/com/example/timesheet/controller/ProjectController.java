package com.example.timesheet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timesheet.dto.ProjectDTO;
import com.example.timesheet.dto.PurchaseOrderDTO;
import com.example.timesheet.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Project Endpoints
    @PostMapping("/projects")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        ProjectDTO createdProject = projectService.createProject(projectDTO);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getProjectDetails(projectId));
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Integer projectId,
                                                     @Valid @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.updateProject(projectId, projectDTO));
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    // Purchase Order Endpoints
    @GetMapping("/pos")
    public ResponseEntity<List<PurchaseOrderDTO>> getAllPurchaseOrders() {
        return ResponseEntity.ok(projectService.getAllPurchaseOrders());
    }

    @PostMapping("/pos")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        PurchaseOrderDTO createdPO = projectService.addPurchaseOrder(purchaseOrderDTO);
        return new ResponseEntity<>(createdPO, HttpStatus.CREATED);
    }

    @GetMapping("/pos/{poId}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrder(@PathVariable Integer poId) {
        return ResponseEntity.ok(projectService.getPurchaseOrderById(poId));
    }

    @PutMapping("/pos/{poId}")
    public ResponseEntity<PurchaseOrderDTO> updatePurchaseOrder(@PathVariable Integer poId,
                                                                 @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        return ResponseEntity.ok(projectService.updatePurchaseOrder(poId, purchaseOrderDTO));
    }

    @DeleteMapping("/pos/{poId}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Integer poId) {
        projectService.deletePurchaseOrder(poId);
        return ResponseEntity.noContent().build();
    }

    // Project-specific Purchase Order Endpoints
    @PostMapping("/projects/{projectId}/pos")
    public ResponseEntity<PurchaseOrderDTO> addProjectPurchaseOrder(@PathVariable Integer projectId,
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        purchaseOrderDTO.setProjectId(projectId);
        return new ResponseEntity<>(projectService.addPurchaseOrder(purchaseOrderDTO), HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/pos")
    public ResponseEntity<List<PurchaseOrderDTO>> getProjectPurchaseOrders(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getPurchaseOrdersByProject(projectId));
    }
}
