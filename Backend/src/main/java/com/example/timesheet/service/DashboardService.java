package com.example.timesheet.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.timesheet.dto.DashboardDTO;
import com.example.timesheet.entity.ProjectDetails;
import com.example.timesheet.entity.PurchaseOrders;
import com.example.timesheet.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    public DashboardDTO getDashboardSummary() {
        List<ProjectDetails> projects = projectRepository.findAll();

        BigDecimal totalBudget = projects.stream()
                .map(ProjectDetails::getProjectBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPOValue = projects.stream()
                .flatMap(project -> project.getPurchaseOrders().stream())
                .map(PurchaseOrders::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUtilized = projects.stream()
                .flatMap(project -> project.getPurchaseOrders().stream())
                .map(PurchaseOrders::getPoUtilized)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalBudget.subtract(totalUtilized);

        DashboardDTO dashboardDTO = new DashboardDTO();
        dashboardDTO.setTotalBudget(totalBudget);
        dashboardDTO.setTotalPOValue(totalPOValue);
        dashboardDTO.setTotalUtilized(totalUtilized);
        dashboardDTO.setTotalRemaining(totalRemaining);

        return dashboardDTO;
    }
}
