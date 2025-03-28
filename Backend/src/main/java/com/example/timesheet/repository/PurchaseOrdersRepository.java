package com.example.timesheet.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.timesheet.entity.PurchaseOrders;

@Repository
public interface PurchaseOrdersRepository extends JpaRepository<PurchaseOrders, Integer> {
        boolean existsByPoNumber(String poNumber);

        Optional<PurchaseOrders> findByProject_ProjectId(Integer projectId);

        @Query("SELECT po.value FROM PurchaseOrders po WHERE po.poId = :poId")
        BigDecimal getPOValue(@Param("poId") Integer poId);

        @Query("SELECT po.poUtilized FROM PurchaseOrders po WHERE po.poId = :poId")
        BigDecimal getPOUtilized(@Param("poId") Integer poId);

}