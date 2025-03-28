package com.example.timesheet.repository;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.timesheet.entity.Allocations;

@Repository
public interface AllocationRepository extends JpaRepository<Allocations, Integer> {
        boolean existsByResourceIdAndProjectId(Integer resourceId, Integer projectId);
        List<Allocations> findByResourceId(Integer resourceId);
        List<Allocations> findByProjectId(Integer projectId);
        List<Allocations> findByPoId(Integer poId);

        @Query("SELECT COALESCE(SUM(a.allocatedHours), 0) FROM Allocations a WHERE a.projectId = :projectId AND a.poId = :poId")
        BigDecimal calculateTotalAllocatedHours(Integer projectId, Integer poId);

        @Query("SELECT COALESCE(SUM(a.allocatedCost), 0) FROM Allocations a WHERE a.projectId = :projectId AND a.poId = :poId")
        BigDecimal calculateTotalAllocatedCost(Integer projectId, Integer poId);

        @Query("SELECT a FROM Allocations a WHERE a.projectId = :projectId AND a.poId = :poId")
        List<Allocations> findAllocationsByProjectIdAndPoId(@Param("projectId") Integer projectId, @Param("poId") Integer poId);
}