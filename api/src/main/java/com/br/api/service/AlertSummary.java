package com.br.api.service;

public class AlertSummary {
    private Long lowStockAlerts;
    private Long expiringProducts;
    
    public AlertSummary(Long lowStockAlerts, Long expiringProducts) {
        this.lowStockAlerts = lowStockAlerts;
        this.expiringProducts = expiringProducts;
    }
    
    // CORREÇÃO: Adicionar getters e setters
    public Long getLowStockAlerts() {
        return lowStockAlerts;
    }
    
    public void setLowStockAlerts(Long lowStockAlerts) {
        this.lowStockAlerts = lowStockAlerts;
    }
    
    public Long getExpiringProducts() {
        return expiringProducts;
    }
    
    public void setExpiringProducts(Long expiringProducts) {
        this.expiringProducts = expiringProducts;
    }
}