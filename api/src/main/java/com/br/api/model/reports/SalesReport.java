package com.br.api.model.reports;

import java.time.LocalDate;

public class SalesReport {
    private LocalDate period;
    private Long totalSales;
    private Double totalRevenue;
    private Double averageSaleValue;
    private Long totalItemsSold;

    // Construtores
    public SalesReport() {}

    public SalesReport(LocalDate period, Long totalSales, Double totalRevenue, 
                      Double averageSaleValue, Long totalItemsSold) {
        this.period = period;
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.averageSaleValue = averageSaleValue;
        this.totalItemsSold = totalItemsSold;
    }

    // Getters e Setters
    public LocalDate getPeriod() { return period; }
    public void setPeriod(LocalDate period) { this.period = period; }

    public Long getTotalSales() { return totalSales; }
    public void setTotalSales(Long totalSales) { this.totalSales = totalSales; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAverageSaleValue() { return averageSaleValue; }
    public void setAverageSaleValue(Double averageSaleValue) { this.averageSaleValue = averageSaleValue; }

    public Long getTotalItemsSold() { return totalItemsSold; }
    public void setTotalItemsSold(Long totalItemsSold) { this.totalItemsSold = totalItemsSold; }
}