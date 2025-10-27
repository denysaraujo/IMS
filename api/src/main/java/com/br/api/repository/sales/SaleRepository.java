package com.br.api.repository.sales;

import com.br.api.model.sales.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    Optional<Sale> findBySaleCode(String saleCode);
    
    List<Sale> findByStatus(String status);
    
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT s FROM Sale s WHERE s.totalAmount > :minAmount")
    List<Sale> findSalesWithAmountGreaterThan(@Param("minAmount") Double minAmount);
    
    @Query("SELECT COUNT(s), SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    Object[] getSalesSummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}