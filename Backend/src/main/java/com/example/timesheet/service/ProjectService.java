package com.example.timesheet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.timesheet.dto.ProjectDTO;
import com.example.timesheet.dto.PurchaseOrderDTO;
import com.example.timesheet.entity.ProjectDetails;
import com.example.timesheet.entity.PurchaseOrders;
import com.example.timesheet.exception.BusinessValidationException;
import com.example.timesheet.repository.ProjectRepository;
import com.example.timesheet.repository.PurchaseOrdersRepository;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PurchaseOrdersRepository purchaseOrdersRepository;

    // Create Project
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        if (projectRepository.existsByProjectName(projectDTO.getProjectName())) {
            throw new BusinessValidationException("Project name already exists");
        }

        ProjectDetails projectDetails = convertToEntity(projectDTO);
        ProjectDetails savedProject = projectRepository.save(projectDetails);

        return mapProjectToDTO(savedProject);
    }

    // Get All Projects
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapProjectToDTO)
                .collect(Collectors.toList());
    }

    // Get Active Projects
    public List<ProjectDTO> getActiveProjects() {
        return projectRepository.findActiveProjects().stream()
                .map(this::mapProjectToDTO)
                .collect(Collectors.toList());
    }

    // Get Project by ID
    public ProjectDTO getProjectDetails(Integer projectId) {
        ProjectDetails project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessValidationException("Project not found"));

        return mapProjectToDTO(project);
    }

    // Update Project
    public ProjectDTO updateProject(Integer projectId, ProjectDTO projectDTO) {
        ProjectDetails existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessValidationException("Project not found"));

        // Update project details
        existingProject.setProjectName(projectDTO.getProjectName());
        existingProject.setProjectBudget(projectDTO.getProjectBudget());
        existingProject.setProjectDescription(projectDTO.getProjectDescription());

        ProjectDetails updatedProject = projectRepository.save(existingProject);
        return mapProjectToDTO(updatedProject);
    }

    // Delete Project
    public void deleteProject(Integer projectId) {
        ProjectDetails project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessValidationException("Project not found"));

        projectRepository.delete(project);
    }

    // Add Purchase Order
    public PurchaseOrderDTO addPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO) {
        if (purchaseOrdersRepository.existsByPoNumber(purchaseOrderDTO.getPoNumber())) {
            throw new BusinessValidationException("Purchase Order number already exists");
        }

        ProjectDetails project = projectRepository.findById(purchaseOrderDTO.getProjectId())
                .orElseThrow(() -> new BusinessValidationException("Project not found"));

        PurchaseOrders purchaseOrder = convertPOToEntity(purchaseOrderDTO, project);

        // Budget validation
        BigDecimal currentTotalPO = project.getPurchaseOrders().stream()
                .map(PurchaseOrders::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentTotalPO.add(purchaseOrder.getValue()).compareTo(project.getProjectBudget()) > 0) {
            throw new BusinessValidationException("Purchase order would exceed project budget");
        }

        PurchaseOrders savedPO = purchaseOrdersRepository.save(purchaseOrder);
        return convertPOToDTO(savedPO);
    }

    // Get Purchase Orders by Project
    public List<PurchaseOrderDTO> getPurchaseOrdersByProject(Integer projectId) {
        ProjectDetails project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessValidationException("Project not found"));

        return project.getPurchaseOrders().stream()
                .map(this::convertPOToDTO)
                .collect(Collectors.toList());
    }

    // Get Purchase Order by ID
    public PurchaseOrderDTO getPurchaseOrderById(Integer poId) {
        PurchaseOrders purchaseOrder = purchaseOrdersRepository.findById(poId)
                .orElseThrow(() -> new BusinessValidationException("Purchase Order not found"));

        return convertPOToDTO(purchaseOrder);
    }

    // Update Purchase Order
    public PurchaseOrderDTO updatePurchaseOrder(Integer poId, PurchaseOrderDTO purchaseOrderDTO) {
        PurchaseOrders existingPO = purchaseOrdersRepository.findById(poId)
                .orElseThrow(() -> new BusinessValidationException("Purchase Order not found"));

        // Update purchase order details
        existingPO.setPoNumber(purchaseOrderDTO.getPoNumber());
        existingPO.setValue(purchaseOrderDTO.getValue());

        PurchaseOrders updatedPO = purchaseOrdersRepository.save(existingPO);
        return convertPOToDTO(updatedPO);
    }

    // Delete Purchase Order
    public void deletePurchaseOrder(Integer poId) {
        PurchaseOrders purchaseOrder = purchaseOrdersRepository.findById(poId)
                .orElseThrow(() -> new BusinessValidationException("Purchase Order not found"));

        purchaseOrdersRepository.delete(purchaseOrder);
    }

    // Mapping and Conversion Methods (remain the same as in previous implementation)
    private ProjectDetails convertToEntity(ProjectDTO dto) {
        ProjectDetails projectDetails = new ProjectDetails();
        projectDetails.setProjectName(dto.getProjectName());
        projectDetails.setProjectBudget(dto.getProjectBudget());
        projectDetails.setProjectDescription(dto.getProjectDescription());
        return projectDetails;
    }

    private ProjectDTO mapProjectToDTO(ProjectDetails project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectId(project.getProjectId());
        projectDTO.setProjectName(project.getProjectName());
        projectDTO.setProjectBudget(project.getProjectBudget());
        projectDTO.setProjectDescription(project.getProjectDescription());
        projectDTO.setCreatedAt(project.getCreatedAt());

        BigDecimal utilizedAmount = project.getPurchaseOrders().stream()
                .map(PurchaseOrders::getPoUtilized)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        projectDTO.setTotalPOs(project.getPurchaseOrders().size());
        projectDTO.setUtilizedAmount(utilizedAmount);
        projectDTO.setRemainingBalance(project.getProjectBudget().subtract(utilizedAmount));

        projectDTO.setPurchaseOrders(project.getPurchaseOrders().stream()
                .map(this::convertPOToDTO)
                .collect(Collectors.toList()));

        return projectDTO;
    }

    private PurchaseOrderDTO convertPOToDTO(PurchaseOrders purchaseOrder) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setPoId(purchaseOrder.getPoId());
        dto.setProjectId(purchaseOrder.getProject().getProjectId());
        dto.setPoNumber(purchaseOrder.getPoNumber());
        dto.setValue(purchaseOrder.getValue());
        dto.setCreatedAt(purchaseOrder.getCreatedAt());
        dto.setPoUtilized(purchaseOrder.getPoUtilized()); // Get utilization only for the specific PO
        dto.setPoBalance(purchaseOrder.getValue().subtract(purchaseOrder.getPoUtilized())); // Ensure balance is per PO
        if (purchaseOrder.getPoUtilized().compareTo(purchaseOrder.getValue()) > 0) {
            throw new BusinessValidationException("Purchase Order utilized amount cannot exceed its value.");
        }

        return dto;
    }

    private PurchaseOrders convertPOToEntity(PurchaseOrderDTO dto, ProjectDetails project) {
        PurchaseOrders purchaseOrder = new PurchaseOrders();
        purchaseOrder.setPoNumber(dto.getPoNumber());
        purchaseOrder.setValue(dto.getValue());
        purchaseOrder.setProject(project);
        return purchaseOrder;
    }

    public List<PurchaseOrderDTO> getAllPurchaseOrders() {
        List<PurchaseOrders> purchaseOrders = purchaseOrdersRepository.findAll();
        return purchaseOrders.stream()
                .map(this::convertPOToDTO)
                .collect(Collectors.toList());
    }
}
