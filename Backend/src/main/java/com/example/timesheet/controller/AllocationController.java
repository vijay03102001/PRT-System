package com.example.timesheet.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timesheet.dto.AllocationDTO;
import com.example.timesheet.exception.BusinessValidationException;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.service.AllocationService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    // Assign resources to projects
    @PostMapping("/allocation")
    public ResponseEntity<?> assignResourceToProject(@RequestBody AllocationDTO allocationDTO) {
        try {
            // Create a new allocation and return the created allocation DTO
            AllocationDTO createdAllocation = allocationService.createAllocation(allocationDTO);
            return new ResponseEntity<>(createdAllocation, HttpStatus.CREATED);
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            // Return a detailed error message for business logic and resource errors
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // General fallback for unexpected errors
            return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch resource allocation data
    @GetMapping("/utilization")
    public ResponseEntity<?> getAllocations(
            @RequestParam Integer projectId,
            @RequestParam Integer poId) {
        try {
            List<AllocationDTO> allocationData = allocationService.getAllocations(projectId, poId);
            return ResponseEntity.ok(allocationData);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch allocations: " + e.getMessage()));
        }
    }

    @GetMapping("/all-utilization")
    public ResponseEntity<?> getAllUtilization() {
        try {
            List<AllocationDTO> allUtilizationData = allocationService.getAllUtilization();
            return ResponseEntity.ok(allUtilizationData);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch all utilization data: " + e.getMessage()));
        }
    }
}
