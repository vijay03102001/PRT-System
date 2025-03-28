package com.example.timesheet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.timesheet.entity.Designations;

@Repository
public interface DesignationsRepository extends JpaRepository<Designations, Integer> {
        Optional<Designations> findByDesignationName(String designationName);
        boolean existsByDesignationName(String designationName);
        Optional<Designations> findByDesignationNameIgnoreCase(String designationName);
}