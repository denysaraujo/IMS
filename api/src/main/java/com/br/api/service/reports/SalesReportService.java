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
        
        Object[] summary = saleRepository.getSalesSummary(startOfDay, endOfDay);
        
        Long totalSales = (Long) summary[0];
        Double totalRevenue = (Double) summary[1];
        Double averageSaleValue = totalSales > 0 ? totalRevenue / totalSales : 0.0;
        
        // Aqui você pode adicionar lógica para contar itens vendidos
        Long totalItemsSold = 0L; // Implementar conforme necessidade
        
        return new SalesReport(date, totalSales, totalRevenue, averageSaleValue, totalItemsSold);
    }

    public SalesReport generateMonthlyReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();
        
        Object[] summary = saleRepository.getSalesSummary(startDateTime, endDateTime);
        
        Long totalSales = (Long) summary[0];
        Double totalRevenue = (Double) summary[1];
        Double averageSaleValue = totalSales > 0 ? totalRevenue / totalSales : 0.0;
        Long totalItemsSold = 0L;
        
        return new SalesReport(startDate, totalSales, totalRevenue, averageSaleValue, totalItemsSold);
    }
}