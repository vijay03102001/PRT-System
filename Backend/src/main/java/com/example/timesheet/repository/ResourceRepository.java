package com.example.timesheet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.timesheet.entity.Resources;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resources, Integer> {
// JpaRepository already provides the method existsById
// Custom query to fetch the resource along with its designation's costPerHour

    @Query("SELECT r FROM Resources r JOIN FETCH r.designation d WHERE r.resourceId = :resourceId")
    Optional<Resources> findResourceWithDesignationCost(@Param("resourceId") Integer resourceId);

    // Find resources by designation ID
    List<Resources> findByDesignationDesignationId(Integer designationId);

    @Query("SELECT r FROM Resources r WHERE r.project.projectId = :projectId")
    List<Resources> findByProjectId(@Param("projectId") Integer projectId);

}