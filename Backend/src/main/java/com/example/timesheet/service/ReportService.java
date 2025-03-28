package com.example.timesheet.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.timesheet.dto.ProjectDTO;
import com.example.timesheet.dto.PurchaseOrderDTO;
import com.example.timesheet.dto.ReportDTO;
import com.example.timesheet.dto.ResourceCostDTO;
import com.example.timesheet.dto.ResourceDTO;
import com.example.timesheet.entity.ProjectDetails;
import com.example.timesheet.entity.PurchaseOrders;
import com.example.timesheet.entity.Resources;
import com.example.timesheet.exception.BusinessValidationException;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.repository.DesignationsRepository;
import com.example.timesheet.repository.ProjectRepository;
import com.example.timesheet.repository.PurchaseOrdersRepository;
import com.example.timesheet.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class
ReportService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;

    @Autowired
    private DesignationsRepository designationsRepository;

    public List<ReportDTO> getAllReports() {
        List<ProjectDetails> projects = projectRepository.findAll();

        return projects.stream().map(project -> {
            ReportDTO reportDTO = new ReportDTO();
            reportDTO.setProject(mapProjectToDTO(project));
            reportDTO.setPurchaseOrders(project.getPurchaseOrders().stream()
                    .map(this::convertPOToDTO)  // Use `this::methodName` to reference non-static methods
                    .collect(Collectors.toList()));

            // Correctly call `resourceRepository.findByProjectId`
            List<ResourceDTO> resourceDTOs = resourceRepository.findByProjectId(project.getProjectId()).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            reportDTO.setResources(resourceDTOs);
            return reportDTO;
        }).collect(Collectors.toList());
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

    private ResourceDTO convertToDTO(Resources resource) {
        ResourceDTO dto = new ResourceDTO();
        dto.setResourceId(resource.getResourceId());
        dto.setResourceName(resource.getResourceName());
        dto.setEmployeeId(resource.getEmployeeId());
        dto.setContactDetails(resource.getContactDetails());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setDesignationId(resource.getDesignationId());
        dto.setDesignationName(resource.getDesignation().getDesignationName());

        // Fetch designation name from repository
        if (resource.getDesignationId() != null) {
            designationsRepository.findById(resource.getDesignationId()).ifPresent(designation ->
                    dto.setDesignationName(designation.getDesignationName())
            );
        }

        return dto;
    }
    public ReportDTO getReportByProjectAndPO(Integer projectId, Integer poId) {
        // Fetch Project Details
        ProjectDetails project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Filter only the required PO
        PurchaseOrders purchaseOrder = purchaseOrdersRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + poId));

        if (!purchaseOrder.getProject().getProjectId().equals(projectId)) {
            throw new BusinessValidationException("PO does not belong to the given Project.");
        }

        // Convert Data
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setProject(mapProjectToDTO(project));
        reportDTO.setPurchaseOrders(List.of(convertPOToDTO(purchaseOrder)));

        // Fetch only resources associated with this project
        List<ResourceDTO> resourceDTOs = resourceRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        reportDTO.setResources(resourceDTOs);

        return reportDTO;
    }
    public ResponseEntity<byte[]> exportReportsToExcel() {
        Logger logger = Logger.getLogger(ReportService.class.getName());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reports");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Project ID", "Project Name", "Budget", "Utilized Amount", "Remaining Balance",
                    "PO ID", "PO Number", "PO Value", "PO Utilized", "PO Balance",
                    "Resource ID", "Resource Name", "Employee ID", "Designation"};

            // Set header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fetch Report Data in an optimized manner
            List<ReportDTO> reports = getAllReports();
            int rowNum = 1;

            for (ReportDTO report : reports) {
                for (PurchaseOrderDTO po : report.getPurchaseOrders()) {
                    for (ResourceCostDTO resource : report.getResources()) {
                        Row row = sheet.createRow(rowNum++);

                        row.createCell(0).setCellValue(report.getProject().getProjectId());
                        row.createCell(1).setCellValue(report.getProject().getProjectName());
                        row.createCell(2).setCellValue(report.getProject().getProjectBudget().doubleValue());
                        row.createCell(3).setCellValue(report.getProject().getUtilizedAmount().doubleValue());
                        row.createCell(4).setCellValue(report.getProject().getRemainingBalance().doubleValue());

                        row.createCell(5).setCellValue(po.getPoId());
                        row.createCell(6).setCellValue(po.getPoNumber());
                        row.createCell(7).setCellValue(po.getValue().doubleValue());
                        row.createCell(8).setCellValue(po.getPoUtilized().doubleValue());
                        row.createCell(9).setCellValue(po.getPoBalance().doubleValue());

                        row.createCell(10).setCellValue(resource.getResourceId());
                        row.createCell(11).setCellValue(resource.getResourceName());
                        row.createCell(12).setCellValue(resource.getEmployeeId());
                        row.createCell(13).setCellValue(resource.getDesignationName());
                    }
                }
            }

            // Auto-size columns, but limit the width for columns with long text (e.g., Project Name)
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                if (i == 1) { // Example: limit the width for the "Project Name" column
                    sheet.setColumnWidth(i, 30 * 256);  // Set column width to 30 characters (approx.)
                }
            }

            // Convert the workbook to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "report.xlsx");

            logger.info("Report export completed successfully.");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during report export: {0}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: {0}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}