package com.br.api.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.model.inventory.InventoryItem;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AlertService {
    
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    
    public List<InventoryItem> getLowStockAlerts() {
        return inventoryItemRepository.findByLowStockAlertTrue();
    }
    
    public List<InventoryItem> getExpiringProducts(LocalDate daysFromNow) {
        return inventoryItemRepository.findByExpirationDateBeforeAndExpirationDateNotNull(daysFromNow);
    }
    
    public AlertSummary getAlertSummary() {
        long lowStockCount = inventoryItemRepository.countByLowStockAlertTrue();
        long expiringCount = inventoryItemRepository.countByExpirationDateBefore(LocalDate.now().plusDays(7));
        
        return new AlertSummary(lowStockCount, expiringCount);
    }
}