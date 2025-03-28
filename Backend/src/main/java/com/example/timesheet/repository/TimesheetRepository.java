package com.example.timesheet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.timesheet.entity.Timesheet;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Integer> {

    // Find all timesheets for a specific resource
    List<Timesheet> findByResourceId(Integer resourceId);

    // Find all timesheets for a specific project
    List<Timesheet> findByProjectId(Integer projectId);

    // Find all timesheets for a specific project
    List<Timesheet> findByPoId(Integer poId);

    // Find timesheets for a resource within a date range
    List<Timesheet> findByResourceIdAndWorkDateBetween(Integer resourceId, LocalDate startDate, LocalDate endDate);
    List<Timesheet> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

    // Find timesheets by status
    List<Timesheet> findByStatus(String status);

    List<Timesheet> findByResourceIdAndProjectId(Integer resourceId, Integer projectId);

    @Query("SELECT t FROM Timesheet t JOIN FETCH t.resource r JOIN FETCH r.designation WHERE t.timesheetId = :id")
    Optional<Timesheet> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT t FROM Timesheet t JOIN FETCH t.purchaseOrder po WHERE t.project.projectId = :projectId AND po.poId = :poId")
    List<Timesheet> findByProjectAndPO(@Param("projectId") Integer projectId, @Param("poId") Integer poId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0), COALESCE(SUM(t.cost), 0) " +
            "FROM Timesheet t " +
            "WHERE t.projectId = :projectId " +
            "AND t.poId = :poId " +
            "AND t.resourceId = :resourceId " +
            "AND t.status = 'Approved'")
    Object[] findTotalApprovedHoursAndCostByProjectPoAndResource(
            @Param("projectId") Integer projectId,
            @Param("poId") Integer poId,
            @Param("resourceId") Integer resourceId
    );
}
