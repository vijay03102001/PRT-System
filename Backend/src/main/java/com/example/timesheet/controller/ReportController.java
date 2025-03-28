package com.example.timesheet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timesheet.dto.ReportDTO;
import com.example.timesheet.service.ReportService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor

public class ReportController {
    @Autowired
    private final ReportService reportService;

    @GetMapping("/all")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{projectId}/{poId}")
    public ResponseEntity<ReportDTO> getReportByProjectAndPO(@PathVariable Integer projectId, @PathVariable Integer poId) {
        ReportDTO report = reportService.getReportByProjectAndPO(projectId, poId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> downloadExcelReport() {
        return reportService.exportReportsToExcel();
    }
}
