package com.br.api.repository.reports;

import java.time.LocalDate;

public class SalesReportDTO {
    private LocalDate period;
    private Long totalSales;
    private Double totalRevenue;
    private Double averageSale;
    private Long totalItemsSold;
    
    public SalesReportDTO(LocalDate period, Long totalSales, Double totalRevenue, 
                         Double averageSale, Long totalItemsSold) {
        this.period = period;
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.averageSale = averageSale;
        this.totalItemsSold = totalItemsSold;
    }
    
    // Getters e Setters
    public LocalDate getPeriod() { return period; }
    public void setPeriod(LocalDate period) { this.period = period; }
    public Long getTotalSales() { return totalSales; }
    public void setTotalSales(Long totalSales) { this.totalSales = totalSales; }
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    public Double getAverageSale() { return averageSale; }
    public void setAverageSale(Double averageSale) { this.averageSale = averageSale; }
    public Long getTotalItemsSold() { return totalItemsSold; }
    public void setTotalItemsSold(Long totalItemsSold) { this.totalItemsSold = totalItemsSold; }
    
    @Override
    public String toString() {
        return "SalesReportDTO{" +
                "period=" + period +
                ", totalSales=" + totalSales +
                ", totalRevenue=" + totalRevenue +
                ", averageSale=" + averageSale +
                ", totalItemsSold=" + totalItemsSold +
                '}';
    }
}