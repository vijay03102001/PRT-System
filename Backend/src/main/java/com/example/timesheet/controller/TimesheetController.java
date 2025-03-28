package com.example.timesheet.controller;

import com.example.timesheet.dto.TimesheetDTO;
import com.example.timesheet.service.TimesheetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimesheetDTO> getTimesheetById(@PathVariable Integer id) {
        return ResponseEntity.ok(timesheetService.findById(id));
    }

    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<TimesheetDTO>> getTimesheetsByResourceId(@PathVariable Integer resourceId) {
        return ResponseEntity.ok(timesheetService.findByResourceId(resourceId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TimesheetDTO>> getTimesheetsByProjectId(@PathVariable Integer projectId) {
        return ResponseEntity.ok(timesheetService.findByProjectId(projectId));
    }

    @GetMapping("/purchase-orders/{poId}")
    public ResponseEntity<List<TimesheetDTO>> getTimesheetsByPoId(@PathVariable Integer poId) {
        return ResponseEntity.ok(timesheetService.findByPoId(poId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TimesheetDTO>> getTimesheetsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timesheetService.findByDateRange(startDate, endDate));
    }

    @GetMapping
    public ResponseEntity<List<TimesheetDTO>> getAllTimesheets() {
        return ResponseEntity.ok(timesheetService.getAllTimesheets());
    }

    @PostMapping
    public ResponseEntity<TimesheetDTO> createTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
        return ResponseEntity.ok(timesheetService.createTimesheet(timesheetDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimesheetDTO> updateTimesheet(
            @PathVariable Integer id,
            @RequestBody TimesheetDTO timesheetDTO) {
        return ResponseEntity.ok(timesheetService.updateTimesheet(id, timesheetDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimesheet(@PathVariable Integer id) {
        timesheetService.deleteTimesheet(id);
        return ResponseEntity.noContent().build();
    }
}