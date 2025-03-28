package com.example.timesheet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.timesheet.entity.DesignationAuditLog;

@Repository
public interface DesignationAuditLogRepository extends JpaRepository<DesignationAuditLog, Integer> {
        // Retrieve logs by designation ID
        List<DesignationAuditLog> findByDesignation_DesignationId(Integer designationId);

        List<DesignationAuditLog> findByDesignation_DesignationIdOrderByUpdatedAtDesc(Integer designationId);

        @Query("SELECT a FROM DesignationAuditLog a JOIN FETCH a.designation WHERE a.designation.designationId = :designationId")
        List<DesignationAuditLog> findByDesignationIdWithDesignation(@Param("designationId") Integer designationId);
}