package com.br.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.api.service.AlertService;
import com.br.api.service.AlertSummary;
import com.br.api.model.inventory.InventoryItem;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:4200")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockAlerts() {
        List<InventoryItem> alerts = alertService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<AlertSummary> getAlertSummary() {
        AlertSummary summary = alertService.getAlertSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/expiring")
    public ResponseEntity<List<InventoryItem>> getExpiringProducts(
            @RequestParam(defaultValue = "7") Integer days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        List<InventoryItem> expiring = alertService.getExpiringProducts(threshold);
        return ResponseEntity.ok(expiring);
    }
}