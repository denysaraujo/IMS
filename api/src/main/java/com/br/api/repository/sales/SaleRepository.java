package com.br.api.repository.sales;

import com.br.api.model.sales.Sale;
import com.br.api.repository.reports.SalesReportDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Sale> findByCustomerId(Long customerId);
    List<Sale> findByStatus(String status);
    
    Optional<Sale> findBySaleCode(String saleCode);
     
    @Query("SELECT s FROM Sale s WHERE s.customer.name LIKE %:customerName%")
    List<Sale> findByCustomerNameContaining(@Param("customerName") String customerName);
    
    // QUERY CORRIGIDA - usando localdate (sem o pacote completo)
    @Query("SELECT new com.br.api.repository.reports.SalesReportDTO(" +
           "CAST(s.saleDate AS localdate), " +  // CORREÇÃO: localdate em vez de java.time.LocalDate
           "COUNT(s), " +
           "SUM(s.totalAmount), " +
           "AVG(s.totalAmount), " +
           "SUM(SIZE(s.items))) " +
           "FROM Sale s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(s.saleDate AS localdate) " +  // CORREÇÃO AQUI TAMBÉM
           "ORDER BY CAST(s.saleDate AS localdate)")    // CORREÇÃO AQUI TAMBÉM
    List<SalesReportDTO> findSalesReportByPeriod(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s), COALESCE(SUM(s.totalAmount), 0), COALESCE(SUM(SIZE(s.items)), 0) " +
           "FROM Sale s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Object[] getSalesSummary(@Param("startDate") LocalDateTime startDate, 
                           @Param("endDate") LocalDateTime endDate);
}