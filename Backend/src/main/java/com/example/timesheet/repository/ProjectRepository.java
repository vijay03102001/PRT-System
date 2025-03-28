package com.example.timesheet.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.timesheet.entity.ProjectDetails;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectDetails, Integer> {

        boolean existsByProjectName(String projectName);

        @Override
        @NonNull
        Optional<ProjectDetails> findById(@NonNull Integer projectId);

        @Query("SELECT p.projectBudget FROM ProjectDetails p WHERE p.projectId = :projectId")
        BigDecimal getProjectBudget(@Param("projectId") Integer projectId);

        @Query("SELECT COALESCE(SUM(a.allocatedCost), 0) FROM Allocations a WHERE a.project.projectId = ?1")
        BigDecimal findTotalAllocatedBudget(Integer projectId);

        // Query to find only active projects
        @Query("SELECT p FROM ProjectDetails p WHERE p.isActive = true")
        List<ProjectDetails> findActiveProjects();

        boolean existsByDesignation_DesignationIdAndIsActive(Integer designationId, Boolean isActive);

        @Query("SELECT COUNT(p) FROM ProjectDetails p WHERE p.designation.designationId = :designationId")
        long countByDesignationId(Integer designationId);

        @Query("SELECT COUNT(p) FROM ProjectDetails p WHERE p.designation.designationId = :designationId AND p.isActive = :isActive")
        long countByDesignationIdAndIsActive(@Param("designationId") Integer designationId, @Param("isActive") Boolean isActive);

}
