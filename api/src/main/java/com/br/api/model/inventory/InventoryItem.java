package com.br.api.model.inventory;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private String productName;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer minStockLevel;

    @Column(nullable = false)
    private Integer maxStockLevel;

    @Column(nullable = false)
    private Double unitPrice;

    private String supplier;

    private String location; // Localização específica no estoque

    @Column(nullable = false)
    private String status; // AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    // Construtores
    public InventoryItem() {
        this.createdAt = LocalDateTime.now();
        this.status = "AVAILABLE";
        this.quantity = 0;
        this.minStockLevel = 5;
        this.maxStockLevel = 100;
    }

    public InventoryItem(String productCode, String productName, String category) {
        this();
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
        updateStatus();
    }

    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) { 
        this.minStockLevel = minStockLevel; 
        updateStatus();
    }

    public Integer getMaxStockLevel() { return maxStockLevel; }
    public void setMaxStockLevel(Integer maxStockLevel) { this.maxStockLevel = maxStockLevel; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    // Métodos de negócio
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateStatus();
    }

    private void updateStatus() {
        if (quantity <= 0) {
            this.status = "OUT_OF_STOCK";
        } else if (quantity <= minStockLevel) {
            this.status = "LOW_STOCK";
        } else {
            this.status = "AVAILABLE";
        }
    }

    public Double getTotalValue() {
        return quantity * unitPrice;
    }

    public Boolean isLowStock() {
        return quantity <= minStockLevel;
    }

    public Boolean isOutOfStock() {
        return quantity <= 0;
    }

    public void addStock(Integer quantityToAdd) {
        if (quantityToAdd > 0) {
            this.quantity += quantityToAdd;
            updateStatus();
        }
    }

    public void removeStock(Integer quantityToRemove) {
        if (quantityToRemove > 0 && quantityToRemove <= quantity) {
            this.quantity -= quantityToRemove;
            updateStatus();
        }
    }
}