package com.example.timesheet.controller;

import com.example.timesheet.dto.DashboardDTO;
import com.example.timesheet.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardDTO getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }
}
