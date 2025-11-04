package com.br.api.service.reports;

import com.br.api.model.reports.SalesReport;
import com.br.api.repository.sales.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SalesReportService {

    private final SaleRepository saleRepository;

    public SalesReportService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public SalesReport generateDailyReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        // CORREÇÃO: Usar o método getSalesSummary que agora existe
        Object[] summary = saleRepository.getSalesSummary(startOfDay, endOfDay);
        
        // CORREÇÃO: Tratar valores nulos e fazer cast seguro
        Long totalSales = summary[0] != null ? ((Number) summary[0]).longValue() : 0L;
        Double totalRevenue = summary[1] != null ? ((Number) summary[1]).doubleValue() : 0.0;
        Long totalItemsSold = summary[2] != null ? ((Number) summary[2]).longValue() : 0L;
        
        Double averageSaleValue = totalSales > 0 ? totalRevenue / totalSales : 0.0;
        
        return new SalesReport(date, totalSales, totalRevenue, averageSaleValue, totalItemsSold);
    }

    public SalesReport generateMonthlyReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();
        
        // Usar o método getSalesSummary
        Object[] summary = saleRepository.getSalesSummary(startDateTime, endDateTime);
        
        // Tratar valores nulos e fazer cast seguro
        Long totalSales = summary[0] != null ? ((Number) summary[0]).longValue() : 0L;
        Double totalRevenue = summary[1] != null ? ((Number) summary[1]).doubleValue() : 0.0;
        Long totalItemsSold = summary[2] != null ? ((Number) summary[2]).longValue() : 0L;
        
        Double averageSaleValue = totalSales > 0 ? totalRevenue / totalSales : 0.0;
        
        return new SalesReport(startDate, totalSales, totalRevenue, averageSaleValue, totalItemsSold);
    }

    // Adicionar método para relatório personalizado
    public SalesReport generateCustomReport(LocalDateTime startDate, LocalDateTime endDate) {
        Object[] summary = saleRepository.getSalesSummary(startDate, endDate);
        
        Long totalSales = summary[0] != null ? ((Number) summary[0]).longValue() : 0L;
        Double totalRevenue = summary[1] != null ? ((Number) summary[1]).doubleValue() : 0.0;
        Long totalItemsSold = summary[2] != null ? ((Number) summary[2]).longValue() : 0L;
        
        Double averageSaleValue = totalSales > 0 ? totalRevenue / totalSales : 0.0;
        
        return new SalesReport(startDate.toLocalDate(), totalSales, totalRevenue, averageSaleValue, totalItemsSold);
    }
}